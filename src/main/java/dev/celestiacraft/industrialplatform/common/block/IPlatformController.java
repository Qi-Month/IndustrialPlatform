package dev.celestiacraft.industrialplatform.common.block;

import dev.celestiacraft.industrialplatform.api.ItemMatcher;
import dev.celestiacraft.industrialplatform.common.item.FillAdjusterItem;
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

	/**
	 * 是不是"平台调节器"
	 * <p>
	 * 填充调节器是在代码里硬绑的内置调节器: 手持它就能右键切换平台类型并显示区块边界预览,
	 * 不需要把它写进配置。配置里的 adjuster 列表只是给扳手之类的额外物品留的口子。
	 */
	static boolean isAdjuster(ItemStack stack) {
		return stack.getItem() instanceof FillAdjusterItem || ItemMatcher.matches(stack, CommonConfig.ADJUSTER);
	}

	static boolean isHoldingAdjuster(Player player) {
		return isAdjuster(player.getMainHandItem()) || isAdjuster(player.getOffhandItem());
	}
}