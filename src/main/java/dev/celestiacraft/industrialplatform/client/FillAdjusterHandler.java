package dev.celestiacraft.industrialplatform.client;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.item.FillAdjusterItem;
import dev.celestiacraft.industrialplatform.network.IPNetwork;
import dev.celestiacraft.industrialplatform.network.packet.FillAdjustPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 手持填充调节器且潜行时, 鼠标滚轮改当前选中那一项的填充格数
 * <p>
 * 站立时这里完全不拦截, 滚轮照原版切快捷栏
 */
@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class FillAdjusterHandler {
	@SubscribeEvent
	public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;

		if (player == null || minecraft.screen != null) {
			return;
		}

		// 按住 Alt 时不拦截滚轮, 正常切快捷栏
		if (Screen.hasAltDown()) {
			return;
		}

		// 站立时滚轮保持原版逻辑(切快捷栏), 只有潜行才轮到调节器
		if (!player.isShiftKeyDown()) {
			return;
		}

		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof FillAdjusterItem)) {
			return;
		}

		double delta = event.getScrollDelta();
		if (delta == 0.0D) {
			return;
		}

		int step = (Screen.hasControlDown() ? 10 : 1) * (delta > 0.0D ? 1 : -1);
		FillAdjusterItem.Target target = FillAdjusterItem.getTarget(stack);
		int value = FillAdjusterItem.adjust(stack, target, step);

		// 客户端先改一份, 提示立刻能看到; 服务端那份由数据包写
		IPNetwork.sendToServer(new FillAdjustPacket(FillAdjusterItem.getUpFill(stack), FillAdjusterItem.getDownFill(stack)));

		player.displayClientMessage(Component.translatable("message.industrial_platform.fill_values", target.displayName(), value).withStyle(ChatFormatting.AQUA), true);

		event.setCanceled(true);
	}
}