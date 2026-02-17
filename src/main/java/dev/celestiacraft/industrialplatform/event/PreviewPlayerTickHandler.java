package dev.celestiacraft.industrialplatform.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import dev.celestiacraft.industrialplatform.api.ItemMatcher;
import dev.celestiacraft.industrialplatform.api.IPreviewReactive;

@EventBusSubscriber(modid = IndustrialPlatform.MODID)
public class PreviewPlayerTickHandler {
	private static boolean isPreviewTrigger(ItemStack stack) {
		return ItemMatcher.matches(stack, CommonConfig.ADJUSTER) || ItemMatcher.matches(stack, CommonConfig.TRIGGER_BLOCK);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		Level level = player.level();

		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		if (!isPreviewTrigger(player.getMainHandItem()) && !isPreviewTrigger(player.getOffhandItem())) {
			return;
		}
		if (player.tickCount % 5 != 0) {
			return;
		}

		BlockPos center = player.blockPosition();

		for (BlockPos pos : BlockPos.betweenClosed(
				center.offset(-15, -15, -15),
				center.offset(15, 15, 15)
		)) {
			BlockState state = level.getBlockState(pos);
			Block block = state.getBlock();

			if (block instanceof IPreviewReactive reactive) {
				reactive.onPreviewHover(serverLevel, pos, state);
			}
		}
	}
}