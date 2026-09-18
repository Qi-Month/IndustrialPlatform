package dev.celestiacraft.industrialplatform.client;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformProperties;
import dev.celestiacraft.industrialplatform.common.item.FillAdjusterItem;
import dev.celestiacraft.industrialplatform.network.IPNetwork;
import dev.celestiacraft.industrialplatform.network.packet.FillAdjustPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 手持填充调节器时, 鼠标滚轮改填充格数
 */
@EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
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

		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof FillAdjusterItem)) {
			return;
		}

		double delta = event.getScrollDeltaY();
		if (delta == 0.0D) {
			return;
		}

		int step = (Screen.hasControlDown() ? 10 : 1) * (delta > 0.0D ? 1 : -1);
		boolean adjustingDown = player.isShiftKeyDown();

		int upFill = FillAdjusterItem.getUpFill(stack);
		int downFill = FillAdjusterItem.getDownFill(stack);

		if (adjustingDown) {
			downFill = Mth.clamp(downFill + step, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);
		} else {
			upFill = Mth.clamp(upFill + step, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);
		}

		// 客户端先改一份, 提示立刻能看到; 服务端那份由数据包写
		FillAdjusterItem.setFills(stack, upFill, downFill);
		IPNetwork.sendToServer(new FillAdjustPacket(upFill, downFill));

		player.displayClientMessage(Component.translatable("message.industrial_platform.fill_values", upFill, downFill), true);

		event.setCanceled(true);
	}
}