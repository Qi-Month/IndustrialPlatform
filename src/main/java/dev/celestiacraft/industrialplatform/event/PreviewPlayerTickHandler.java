package dev.celestiacraft.industrialplatform.event;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPreviewReactive;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.common.block.IPlatformController;
import dev.celestiacraft.industrialplatform.common.block.platform.PlatformBlock;
import dev.celestiacraft.industrialplatform.common.item.FillAdjusterItem;
import dev.celestiacraft.industrialplatform.data.PlatformSettingsStorage;
import dev.celestiacraft.industrialplatform.network.IPNetwork;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsClearPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@EventBusSubscriber(modid = IndustrialPlatform.MODID)
public class PreviewPlayerTickHandler {
	private static final int SCAN_RADIUS = 15;
	private static final int SCAN_INTERVAL_TICKS = 5;
	private static final Predicate<BlockState> PREVIEW_BLOCK = state -> isPreviewBlock(state.getBlock());
	private static final Map<UUID, PreviewState> PLAYERS = new HashMap<>();

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		if (player.tickCount % SCAN_INTERVAL_TICKS != 0) {
			return;
		}
		PreviewState preview = PLAYERS.computeIfAbsent(player.getUUID(), key -> new PreviewState());

		if (IPlatformController.isHoldingAdjuster(player)) {
			scanNearby(player.serverLevel(), player, preview);
			return;
		}

		// 没拿调节器时, 只把"正看着的那个平台方块"的设置同步过去, Jade 要显示填充格数
		syncLookedAt(player.serverLevel(), player, preview);
	}

	private static boolean isPreviewBlock(Block block) {
		return block instanceof IPreviewReactive || block instanceof IPlatformController;
	}

	private static void scanNearby(ServerLevel level, ServerPlayer player, PreviewState preview) {
		BlockPos center = player.blockPosition();
		int minX = center.getX() - SCAN_RADIUS;
		int maxX = center.getX() + SCAN_RADIUS;
		int minY = Math.max(center.getY() - SCAN_RADIUS, level.getMinBuildHeight());
		int maxY = Math.min(center.getY() + SCAN_RADIUS, level.getMaxBuildHeight() - 1);
		int minZ = center.getZ() - SCAN_RADIUS;
		int maxZ = center.getZ() + SCAN_RADIUS;
		if (minY > maxY) {
			return;
		}

		int minChunkX = SectionPos.blockToSectionCoord(minX);
		int maxChunkX = SectionPos.blockToSectionCoord(maxX);
		int minChunkZ = SectionPos.blockToSectionCoord(minZ);
		int maxChunkZ = SectionPos.blockToSectionCoord(maxZ);
		int minSectionY = SectionPos.blockToSectionCoord(minY);
		int maxSectionY = SectionPos.blockToSectionCoord(maxY);
		int levelMinSection = level.getMinSection();
		ServerChunkCache chunks = level.getChunkSource();
		PlatformSettingsStorage storage = null;
		BlockPos.MutableBlockPos pos = preview.pos;
		BlockPos nearest = null;
		double nearestDistance = Double.MAX_VALUE;

		for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			int originX = SectionPos.sectionToBlockCoord(chunkX);
			int fromX = Math.max(0, minX - originX);
			int toX = Math.min(SectionPos.SECTION_MAX_INDEX, maxX - originX);
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				LevelChunk chunk = chunks.getChunkNow(chunkX, chunkZ);
				if (chunk == null) {
					continue;
				}

				int originZ = SectionPos.sectionToBlockCoord(chunkZ);
				int fromZ = Math.max(0, minZ - originZ);
				int toZ = Math.min(SectionPos.SECTION_MAX_INDEX, maxZ - originZ);
				for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
					LevelChunkSection section = chunk.getSection(sectionY - levelMinSection);
					if (section.hasOnlyAir() || !section.maybeHas(PREVIEW_BLOCK)) {
						continue;
					}

					int originY = SectionPos.sectionToBlockCoord(sectionY);
					int fromY = Math.max(0, minY - originY);
					int toY = Math.min(SectionPos.SECTION_MAX_INDEX, maxY - originY);
					for (int y = fromY; y <= toY; y++) {
						int blockY = originY + y;
						for (int z = fromZ; z <= toZ; z++) {
							int blockZ = originZ + z;
							for (int x = fromX; x <= toX; x++) {
								BlockState state = section.getBlockState(x, y, z);
								Block block = state.getBlock();
								if (!isPreviewBlock(block)) {
									continue;
								}

								pos.set(originX + x, blockY, blockZ);
								if (block instanceof IPreviewReactive reactive) {
									reactive.onPreviewHover(level, pos, state);
								}
								if (block instanceof PlatformBlock) {
									double distance = player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
									if (distance < nearestDistance) {
										nearestDistance = distance;
										nearest = pos.immutable();
									}
								}
								if (block instanceof IPlatformController) {
									if (storage == null) {
										storage = PlatformSettingsStorage.get(level);
									}
									syncSettings(storage, player, pos, state, preview.synced);
								}
							}
						}
					}
				}
			}
		}

		if (nearest != null && storage != null) {
			pushHeldFills(level, player, storage, nearest, preview);
		}
	}

	/**
	 * 手持调节器时, 把手上那两个填充格数推给离玩家最近的那个平台方块
	 * <p>
	 * 只有数值真的变了才写存档, 免得每 5 tick 就把存档标脏一次
	 */
	private static void pushHeldFills(ServerLevel level, ServerPlayer player, PlatformSettingsStorage storage, BlockPos pos, PreviewState preview) {
		ItemStack held = FillAdjusterItem.findHeld(player);
		if (held.isEmpty()) {
			return;
		}

		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof PlatformBlock)) {
			// 扫描到写入之间被挖掉了
			return;
		}

		PlatformSettings current = storage.get(pos).orElse(null);
		PlatformSettings updated = (current == null ? PlatformSettings.defaults(state.getValue(PlatformBlock.PLATFORM_MODE)) : current)
				.withFill(FillAdjusterItem.getUpFill(held), FillAdjusterItem.getDownFill(held));

		if (updated.equals(current)) {
			return;
		}

		storage.put(pos, updated);

		// 顺手把这个新值同步给客户端, 区块边界预览立刻就能跟上
		syncSettings(storage, player, pos, state, preview.synced);
	}

	/**
	 * 玩家看着哪个平台方块, 就把那个方块的设置同步给他(Jade 面板要用)
	 * <p>
	 * 射线比客户端的手长一点没关系, 顶多多同步一个方块
	 */
	private static void syncLookedAt(ServerLevel level, ServerPlayer player, PreviewState preview) {
		HitResult hit = player.pick(player.blockInteractionRange() + 1.0D, 0.0F, false);
		if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
			return;
		}

		BlockPos pos = blockHit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof PlatformBlock)) {
			return;
		}

		syncSettings(PlatformSettingsStorage.get(level), player, pos, state, preview.synced);
	}

	private static void syncSettings(PlatformSettingsStorage storage, ServerPlayer player, BlockPos pos, BlockState state, Map<BlockPos, PlatformSettings> synced) {
		PlatformSettings settings = storage.get(pos).orElse(null);
		if (settings == null && state.getBlock() instanceof PlatformBlock) {
			settings = PlatformSettings.defaults(state.getValue(PlatformBlock.PLATFORM_MODE));
		}
		if (settings == null || settings.equals(synced.get(pos))) {
			return;
		}

		BlockPos immutable = pos.immutable();
		synced.put(immutable, settings);
		IPNetwork.sendToPlayer(player, new PlatformSettingsSyncPacket(immutable, settings.mode(), settings.upFill(), settings.downFill(), settings.blueprintId()));
	}

	public static void onControllerRemoved(ServerLevel level, BlockPos pos) {
		PLAYERS.values().forEach(preview -> preview.synced.remove(pos));
		IPNetwork.sendToTracking(level, pos, new PlatformSettingsClearPacket(pos.immutable()));
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		PLAYERS.remove(event.getEntity().getUUID());
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		PLAYERS.remove(event.getEntity().getUUID());
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		PLAYERS.remove(event.getEntity().getUUID());
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		PLAYERS.clear();
	}

	private static class PreviewState {
		private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		private final Map<BlockPos, PlatformSettings> synced = new HashMap<>();
	}
}