package dev.celestiacraft.industrialplatform.event;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPreviewReactive;
import dev.celestiacraft.industrialplatform.api.ItemMatcher;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.common.block.IPlatformController;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import dev.celestiacraft.industrialplatform.data.PlatformSettingsStorage;
import dev.celestiacraft.industrialplatform.network.IPNetwork;
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

	private static boolean isPreviewTrigger(ItemStack stack) {
		return ItemMatcher.matches(stack, CommonConfig.ADJUSTER);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		if (player.tickCount % SCAN_INTERVAL_TICKS != 0) {
			return;
		}
		if (!isPreviewTrigger(player.getMainHandItem()) && !isPreviewTrigger(player.getOffhandItem())) {
			return;
		}

		PreviewState preview = PLAYERS.computeIfAbsent(player.getUUID(), key -> new PreviewState());
		scanNearby(player.serverLevel(), player, preview);
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
								if (block instanceof IPlatformController) {
									if (storage == null) {
										storage = PlatformSettingsStorage.get(level);
									}
									syncSettings(storage, player, pos, preview.synced);
								}
							}
						}
					}
				}
			}
		}
	}

	private static void syncSettings(PlatformSettingsStorage storage, ServerPlayer player, BlockPos pos, Map<BlockPos, PlatformSettings> synced) {
		PlatformSettings settings = storage.get(pos).orElse(null);
		if (settings == null || settings.equals(synced.get(pos))) {
			return;
		}

		BlockPos immutable = pos.immutable();
		synced.put(immutable, settings);
		IPNetwork.sendToPlayer(player, new PlatformSettingsSyncPacket(immutable, settings.mode(), settings.upFill(), settings.downFill(), settings.blueprintId()));
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