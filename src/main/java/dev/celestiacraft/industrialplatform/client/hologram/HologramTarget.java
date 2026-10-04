package dev.celestiacraft.industrialplatform.client.hologram;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.PlatformPreviewSettings;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.common.block.IPlatformController;
import dev.celestiacraft.industrialplatform.common.block.platform.PlatformBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class HologramTarget {
	private static final float OPEN_TICKS = 8.0F;
	private static final float CLOSE_TICKS = 4.0F;

	private static BlockPos pos;
	private static AABB bounds;
	private static PlatformSettings settings;
	private static boolean lockedOn;
	private static float progress;
	private static float previousProgress;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		previousProgress = progress;

		Minecraft minecraft = Minecraft.getInstance();
		BlockPos target = findTarget(minecraft);
		BlockState state = target != null ? minecraft.level.getBlockState(target) : null;
		lockedOn = state != null && state.getBlock() instanceof PlatformBlock;

		if (!lockedOn) {
			progress = Math.max(0.0F, progress - 1.0F / CLOSE_TICKS);
			if (progress == 0.0F) {
				pos = null;
			}
			return;
		}

		if (!target.equals(pos)) {
			pos = target;
			bounds = state.getShape(minecraft.level, target).bounds();
			progress = 0.0F;
			previousProgress = 0.0F;
		}

		PlatformSettings cached = PlatformPreviewSettings.get(pos);
		settings = cached != null ? cached : PlatformSettings.defaults(state.getValue(PlatformBlock.PLATFORM_MODE));
		progress = Math.min(1.0F, progress + 1.0F / OPEN_TICKS);
	}

	private static BlockPos findTarget(Minecraft minecraft) {
		if (minecraft.screen != null || minecraft.player == null || minecraft.level == null) {
			return null;
		}

		if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
			return null;
		}

		return IPlatformController.isHoldingAdjuster(minecraft.player) ? hit.getBlockPos() : null;
	}

	@SubscribeEvent
	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		pos = null;
		lockedOn = false;
		progress = 0.0F;
		previousProgress = 0.0F;
	}

	public static BlockPos getPos() {
		return pos;
	}

	public static boolean isLockedOn(BlockPos target) {
		return lockedOn && target.equals(pos);
	}

	public static float getProgress(float partialTick) {
		float remaining = 1.0F - Mth.lerp(partialTick, previousProgress, progress);
		return 1.0F - remaining * remaining * remaining;
	}

	public static AABB getBounds() {
		return bounds;
	}

	public static PlatformSettings getSettings() {
		return settings;
	}
}