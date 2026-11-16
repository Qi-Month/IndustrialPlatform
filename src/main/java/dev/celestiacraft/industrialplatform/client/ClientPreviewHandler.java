package dev.celestiacraft.industrialplatform.client;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.PlatformPreviewSettings;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.client.hologram.HologramOverlay;
import dev.celestiacraft.industrialplatform.client.hologram.HologramTarget;
import dev.celestiacraft.industrialplatform.client.hologram.HologramWorldRenderer;
import dev.celestiacraft.industrialplatform.client.preview.PlatformPreviewModel;
import dev.celestiacraft.industrialplatform.common.block.IPlatformController;
import dev.celestiacraft.industrialplatform.common.block.platform.PlatformBlock;
import dev.celestiacraft.industrialplatform.common.block.pool.FluidPoolBlock;
import dev.celestiacraft.industrialplatform.platform.PlatformLayout;
import dev.celestiacraft.industrialplatform.platform.blueprint.ClientBlueprintData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class ClientPreviewHandler {
	private static final int SCAN_RADIUS_XZ = 48;
	private static final int SCAN_INTERVAL_TICKS = 5;
	private static final int POOL_DEPTH = 54;
	private static final int BLOCKS_PER_TICK = 8192;
	private static final int SECTIONS_PER_TICK = 64;
	private static final Predicate<BlockState> SCAN_TARGET = state -> state.getBlock() instanceof IPlatformController || state.getBlock() instanceof FluidPoolBlock;
	private static final Map<BlockPos, Preview> TARGETS = new LinkedHashMap<>();
	private static final List<BoundaryRenderData.BoundaryEntry> VISIBLE = new ArrayList<>();
	private static ClientLevel activeLevel;
	private static Scan scan;
	private static long nextScanTick;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		LocalPlayer player = minecraft.player;
		if (level != activeLevel) {
			clear();
			activeLevel = level;
		}
		if (player == null || level == null) {
			return;
		}
		if (!IPlatformController.isHoldingAdjuster(player)) {
			clearTargets();
			return;
		}
		if (minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = hit.getBlockPos();
			if (level.hasChunkAt(pos) && SCAN_TARGET.test(level.getBlockState(pos))) {
				remember(pos);
			}
		}

		long tick = level.getGameTime();
		if (scan == null && tick >= nextScanTick) {
			scan = new Scan(player.blockPosition());
			nextScanTick = tick + SCAN_INTERVAL_TICKS;
		}
		if (scan != null && scan.step(level)) {
			scan = null;
		}
		refreshKnown(level, player, tick);
	}

	private static void remember(BlockPos pos) {
		if (!TARGETS.containsKey(pos)) {
			BlockPos immutable = pos.immutable();
			TARGETS.put(immutable, new Preview(immutable));
		}
	}

	private static void refreshKnown(ClientLevel level, LocalPlayer player, long tick) {
		VISIBLE.clear();
		BlockPos center = player.blockPosition();
		double playerX = player.getX();
		double playerZ = player.getZ();
		BoundaryRenderData.BoundaryEntry editing = BoundaryRenderData.getOverride();
		var iterator = TARGETS.values().iterator();
		while (iterator.hasNext()) {
			Preview preview = iterator.next();
			BlockPos pos = preview.pos;
			if (Math.abs((long) pos.getX() - center.getX()) > SCAN_RADIUS_XZ
					|| Math.abs((long) pos.getZ() - center.getZ()) > SCAN_RADIUS_XZ || !level.hasChunkAt(pos)) {
				iterator.remove();
				continue;
			}
			BlockState state = level.getBlockState(pos);
			if (!SCAN_TARGET.test(state)) {
				iterator.remove();
				continue;
			}
			PlatformSettings settings = PlatformPreviewSettings.get(pos);
			ClientBlueprintData.Entry blueprint = settings == null ? null : ClientBlueprintData.get(settings.blueprintId());
			preview.update(state, settings, blueprint);
			if (preview.entry != null && (editing == null || !editing.pos().equals(pos))
					&& playerX >= preview.bounds.minX && playerX <= preview.bounds.maxX
					&& playerZ >= preview.bounds.minZ && playerZ <= preview.bounds.maxZ) {
				VISIBLE.add(preview.entry);
			}
		}
		BoundaryRenderData.update(VISIBLE, tick);
	}

	private static final class Preview {
		private final BlockPos pos;
		private BlockState state;
		private PlatformSettings settings;
		private ClientBlueprintData.Entry blueprint;
		private BoundaryRenderData.BoundaryEntry entry;
		private AABB bounds;

		private Preview(BlockPos pos) {
			this.pos = pos;
		}

		private void update(BlockState state, PlatformSettings settings, ClientBlueprintData.Entry blueprint) {
			if (this.state == state && Objects.equals(this.settings, settings) && this.blueprint == blueprint) {
				return;
			}
			this.state = state;
			this.settings = settings;
			this.blueprint = blueprint;
			Block block = state.getBlock();
			BoundaryRenderData.BoundaryEntry updated = null;
			int chunkSize = PlatformLayout.BLOCKS_PER_CHUNK;
			if (block instanceof FluidPoolBlock) {
				updated = new BoundaryRenderData.BoundaryEntry(pos, chunkSize, chunkSize, 0, POOL_DEPTH);
			} else {
				PlatformSettings resolved = settings;
				if (resolved == null && block instanceof PlatformBlock) {
					resolved = PlatformSettings.defaults(state.getValue(PlatformBlock.PLATFORM_MODE));
				}
				if (resolved != null) {
					int size = block instanceof PlatformBlock ? resolved.mode().chunkSize() * chunkSize : chunkSize;
					updated = new BoundaryRenderData.BoundaryEntry(pos, blueprint == null ? size : blueprint.sizeX(),
							blueprint == null ? size : blueprint.sizeZ(), resolved.upFill(), resolved.downFill());
				}
			}
			if (!Objects.equals(entry, updated)) {
				entry = updated;
				bounds = updated == null ? null : BoundaryRenderer.boxOf(updated);
			}
		}
	}

	private static final class Scan {
		private final int minX;
		private final int minZ;
		private final int maxX;
		private final int maxZ;
		private final int minChunkX;
		private final int maxChunkX;
		private final int maxChunkZ;
		private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		private int chunkX;
		private int chunkZ;
		private int sectionIndex;
		private int cell;

		private Scan(BlockPos center) {
			minX = center.getX() - SCAN_RADIUS_XZ;
			minZ = center.getZ() - SCAN_RADIUS_XZ;
			maxX = center.getX() + SCAN_RADIUS_XZ;
			maxZ = center.getZ() + SCAN_RADIUS_XZ;
			minChunkX = Math.floorDiv(minX, 16);
			maxChunkX = Math.floorDiv(maxX, 16);
			maxChunkZ = Math.floorDiv(maxZ, 16);
			chunkX = minChunkX;
			chunkZ = Math.floorDiv(minZ, 16);
		}

		private boolean step(ClientLevel level) {
			int blockBudget = BLOCKS_PER_TICK;
			int sectionBudget = SECTIONS_PER_TICK;
			int minY = level.getMinY();
			int maxY = level.getMaxY();
			while (chunkZ <= maxChunkZ && blockBudget > 0 && sectionBudget > 0) {
				LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
				LevelChunkSection[] sections = chunk == null ? null : chunk.getSections();
				if (sections == null || sectionIndex >= sections.length) {
					advanceChunk();
					sectionBudget--;
					continue;
				}
				LevelChunkSection section = sections[sectionIndex];
				if (cell == 0) {
					sectionBudget--;
					if (section.hasOnlyAir() || !section.maybeHas(SCAN_TARGET)) {
						sectionIndex++;
						continue;
					}
				}
				int originX = chunkX * 16;
				int originY = level.getSectionYFromSectionIndex(sectionIndex) * 16;
				int originZ = chunkZ * 16;
				while (cell < 4096 && blockBudget-- > 0) {
					int x = cell & 15;
					int z = cell >> 4 & 15;
					int y = cell >> 8;
					cell++;
					int worldX = originX + x;
					int worldZ = originZ + z;
					int worldY = originY + y;
					if (worldX < minX || worldX > maxX || worldZ < minZ || worldZ > maxZ || worldY < minY || worldY > maxY) {
						continue;
					}
					if (SCAN_TARGET.test(section.getBlockState(x, y, z))) {
						remember(pos.set(worldX, worldY, worldZ));
					}
				}
				if (cell >= 4096) {
					sectionIndex++;
					cell = 0;
				}
			}
			return chunkZ > maxChunkZ;
		}

		private void advanceChunk() {
			sectionIndex = 0;
			cell = 0;
			if (++chunkX > maxChunkX) {
				chunkX = minChunkX;
				chunkZ++;
			}
		}
	}

	private static void clearTargets() {
		scan = null;
		nextScanTick = 0;
		TARGETS.clear();
		VISIBLE.clear();
		BoundaryRenderData.clear();
	}

	@SubscribeEvent
	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		clear();
	}

	public static void invalidateResources() {
		clearTargets();
		PlatformPreviewModel.invalidateResources();
		HologramTarget.clear();
		HologramWorldRenderer.clear();
		HologramOverlay.clear();
	}

	public static void clear() {
		activeLevel = null;
		BoundaryRenderData.clearOverride();
		PlatformPreviewSettings.clear();
		invalidateResources();
	}
}
