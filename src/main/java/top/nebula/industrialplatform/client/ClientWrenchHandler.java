package top.nebula.industrialplatform.client;

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
import top.nebula.industrialplatform.IndustrialPlatform;
import top.nebula.industrialplatform.block.platform.PlatformBlock;
import top.nebula.industrialplatform.block.pool.FluidPoolBlock;
import top.nebula.industrialplatform.block.state.properties.platform.PlatformMode;
import top.nebula.industrialplatform.block.state.properties.platform.PlatformProperties;
import top.nebula.industrialplatform.config.CommonConfig;
import top.nebula.industrialplatform.utils.ItemMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class ClientWrenchHandler {

	private static final int SCAN_RADIUS_XZ = 48;
	private static final ExecutorService SCAN_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
		Thread thread = new Thread(r, "IP-BoundaryScan");
		thread.setDaemon(true);
		return thread;
	});
	private static final AtomicBoolean scanning = new AtomicBoolean(false);

	private static boolean isAdjuster(ItemStack stack) {
		return ItemMatcher.matches(stack, CommonConfig.ADJUSTER);
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		Level level = mc.level;

		if (player == null || level == null) {
			return;
		}

		long tick = level.getGameTime();

		if (!isAdjuster(player.getMainHandItem())) {
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

					if (block instanceof PlatformBlock) {
						PlatformMode mode = state.getValue(PlatformProperties.PLATFORM_MODE);
						boolean extended = mode == PlatformMode.INDUSTRIAL_HEAVY || mode == PlatformMode.CHECKERBOARD_HEAVY;
						boolean floating = state.getValue(PlatformProperties.FLOATING);

						if (isPlayerInBoundary(playerX, playerZ, pos, extended)) {
							newEntries.add(new BoundaryRenderData.BoundaryEntry(pos.immutable(), extended, floating));
						}
					} else if (block instanceof FluidPoolBlock) {
						if (isPlayerInBoundary(playerX, playerZ, pos, false)) {
							newEntries.add(new BoundaryRenderData.BoundaryEntry(pos.immutable(), false, true));
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

	private static boolean isPlayerInBoundary(double playerX, double playerZ, BlockPos blockPos, boolean extended) {
		int finX = (int) Math.floor(blockPos.getX() / 16.0) * 16;
		int finZ = (int) Math.floor(blockPos.getZ() / 16.0) * 16;

		double minX, minZ, maxX, maxZ;
		if (extended) {
			minX = finX - 16;
			minZ = finZ - 16;
			maxX = finX + 32;
			maxZ = finZ + 32;
		} else {
			minX = finX;
			minZ = finZ;
			maxX = finX + 16;
			maxZ = finZ + 16;
		}

		return playerX >= minX && playerX <= maxX && playerZ >= minZ && playerZ <= maxZ;
	}
}