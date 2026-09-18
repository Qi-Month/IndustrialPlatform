package dev.celestiacraft.industrialplatform.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.PlatformPreviewSettings;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.common.block.IPlatformController;
import dev.celestiacraft.industrialplatform.platform.blueprint.ClientBlueprintData;
import dev.celestiacraft.industrialplatform.common.block.platform.PlatformBlock;
import dev.celestiacraft.industrialplatform.common.block.pool.FluidPoolBlock;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformProperties;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import dev.celestiacraft.industrialplatform.api.ItemMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class ClientPreviewHandler {
	private static final int SCAN_RADIUS_XZ = 48;
	/**
	 * 流体池结构向下延伸的格数
	 */
	private static final int POOL_DEPTH = 54;
	private static final ExecutorService SCAN_EXECUTOR = Executors.newSingleThreadExecutor((runnable) -> {
		Thread thread = new Thread(runnable, "IP-BoundaryScan");
		thread.setDaemon(true);
		return thread;
	});
	private static final AtomicBoolean scanning = new AtomicBoolean(false);

	private static boolean isPreviewTrigger(ItemStack stack) {
		return ItemMatcher.matches(stack, CommonConfig.ADJUSTER);
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		Level level = mc.level;

		if (player == null || level == null) {
			PlatformPreviewSettings.clear();
			return;
		}

		long tick = level.getGameTime();

		if (!isPreviewTrigger(player.getMainHandItem()) && !isPreviewTrigger(player.getOffhandItem())) {
			if (!BoundaryRenderData.getEntries().isEmpty()) {
				BoundaryRenderData.clear();
			}
			return;
		}

		if (tick % 5 != 0) {
			if (BoundaryRenderData.isExpired(tick)) {
				BoundaryRenderData.clear();
			}
			return;
		}

		if (scanning.get()) {
			return;
		}

		double playerX = player.getX();
		double playerZ = player.getZ();
		int centerX = player.blockPosition().getX();
		int centerZ = player.blockPosition().getZ();
		int minY = level.getMinBuildHeight();
		int maxY = level.getMaxBuildHeight() - 1;

		BoundaryRenderData.BoundaryEntry editing = BoundaryRenderData.getOverride();

		scanning.set(true);
		CompletableFuture.runAsync(() -> {
			try {
				List<BoundaryRenderData.BoundaryEntry> newEntries = new CopyOnWriteArrayList<>();

				for (BlockPos pos : BlockPos.betweenClosed(
						new BlockPos(centerX - SCAN_RADIUS_XZ, minY, centerZ - SCAN_RADIUS_XZ),
						new BlockPos(centerX + SCAN_RADIUS_XZ, maxY, centerZ + SCAN_RADIUS_XZ)
				)) {
					BlockState state = level.getBlockState(pos);
					Block block = state.getBlock();

					// 界面里正在编辑的那个由界面自己画, 避免重复
					if (editing != null && editing.pos().equals(pos)) {
						continue;
					}

					if (block instanceof IPlatformController) {
						PlatformSettings settings = PlatformPreviewSettings.get(pos);
						boolean floating;
						int sizeX;
						int sizeZ;
						int upFill = -1;
						int downFill = -1;

						if (settings != null) {
							int[] size = previewSize(settings, block instanceof PlatformBlock ? state : null);
							sizeX = size[0];
							sizeZ = size[1];
							floating = settings.isFloating();
							upFill = settings.upFill();
							downFill = settings.downFill();
						} else if (block instanceof PlatformBlock) {
							PlatformMode mode = state.getValue(PlatformProperties.PLATFORM_MODE);
							sizeX = mode.isHeavy() ? 48 : 16;
							sizeZ = sizeX;
							floating = state.getValue(PlatformProperties.FLOATING);
						} else {
							// 设计台没有方块状态可读, 等设置同步过来再画
							continue;
						}

						if (isPlayerInBoundary(playerX, playerZ, pos, sizeX, sizeZ)) {
							newEntries.add(new BoundaryRenderData.BoundaryEntry(pos.immutable(), sizeX, sizeZ, floating, upFill, downFill));
						}
					} else if (block instanceof FluidPoolBlock) {
						if (isPlayerInBoundary(playerX, playerZ, pos, 16, 16)) {
							// 流体池往下 54 格
							newEntries.add(new BoundaryRenderData.BoundaryEntry(pos.immutable(), 16, 16, true, 0, POOL_DEPTH));
						}
					}
				}

				mc.execute(() -> {
					BoundaryRenderData.update(new ArrayList<>(newEntries), level.getGameTime());
				});
			} catch (Exception e) {
				IndustrialPlatform.LOGGER.debug("Boundary scan interrupted", e);
			} finally {
				scanning.set(false);
			}
		}, SCAN_EXECUTOR);
	}


	/**
	 * 预览尺寸: 设计台用蓝图尺寸, 内置平台按模式(16 / 48)
	 */
	private static int[] previewSize(PlatformSettings settings, BlockState state) {
		String blueprintId = settings.blueprintId();
		if (blueprintId != null && !blueprintId.isEmpty()) {
			ClientBlueprintData.Entry entry = ClientBlueprintData.get(blueprintId);
			if (entry != null) {
				return new int[]{entry.sizeX(), entry.sizeZ()};
			}
		}

		if (state != null) {
			PlatformMode mode = state.getValue(PlatformProperties.PLATFORM_MODE);
			int size = mode.isHeavy() ? 48 : 16;
			return new int[]{size, size};
		}

		return new int[]{16, 16};
	}

	private static boolean isPlayerInBoundary(double playerX, double playerZ, BlockPos blockPos, int sizeX, int sizeZ) {
		int chunkSize = 16;
		int chunkX = Math.floorDiv(blockPos.getX(), chunkSize) * chunkSize;
		int chunkZ = Math.floorDiv(blockPos.getZ(), chunkSize) * chunkSize;

		double minX = chunkX - ((Math.max(1, (sizeX + 15) / 16) - 1) / 2) * (double) chunkSize;
		double minZ = chunkZ - ((Math.max(1, (sizeZ + 15) / 16) - 1) / 2) * (double) chunkSize;
		double maxX = minX + Math.max(1, sizeX);
		double maxZ = minZ + Math.max(1, sizeZ);

		return playerX >= minX && playerX <= maxX && playerZ >= minZ && playerZ <= maxZ;
	}
}