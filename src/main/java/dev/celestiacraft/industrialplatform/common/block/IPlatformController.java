package dev.celestiacraft.industrialplatform.common.block;

import dev.celestiacraft.industrialplatform.common.menu.PlatformBuildMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkHooks;

/**
 * 平台控制器: 右键能打开搭建界面的方块都实现这个接口
 */
public interface IPlatformController {
	/**
	 * 打开搭建界面
	 */
	static void openBuilder(ServerPlayer serverPlayer, BlockPos platformPos) {
		NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((windowId, inventory, player) -> {
			return new PlatformBuildMenu(windowId, inventory, platformPos);
		}, Component.translatable("menu.industrial_platform.platform_build")), platformPos);
	}
}