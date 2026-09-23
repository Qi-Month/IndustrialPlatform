package dev.celestiacraft.industrialplatform.common.block;

import dev.celestiacraft.industrialplatform.api.ItemMatcher;
import dev.celestiacraft.industrialplatform.common.menu.PlatformBuildMenu;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 平台控制器: 右键能打开搭建界面的方块都实现这个接口
 */
public interface IPlatformController {
	/**
	 * 打开搭建界面
	 */
	static void openBuilder(ServerPlayer serverPlayer, BlockPos platformPos) {
		serverPlayer.openMenu(new SimpleMenuProvider((windowId, inventory, player) -> {
			return new PlatformBuildMenu(windowId, inventory, platformPos);
		}, Component.translatable("menu.industrial_platform.platform_build")), buffer -> buffer.writeBlockPos(platformPos));
	}

	static boolean isAdjuster(ItemStack stack) {
		return ItemMatcher.matches(stack, CommonConfig.ADJUSTER);
	}

	static boolean isHoldingAdjuster(Player player) {
		return isAdjuster(player.getMainHandItem()) || isAdjuster(player.getOffhandItem());
	}
}