package dev.celestiacraft.industrialplatform.client;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.block.state.properties.platform.PlatformProperties;
import dev.celestiacraft.industrialplatform.item.FillAdjusterItem;
import dev.celestiacraft.industrialplatform.network.IPNetwork;
import dev.celestiacraft.industrialplatform.network.packet.FillAdjustPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 手持填充调节器时, 鼠标滚轮改填充格数
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

		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof FillAdjusterItem)) {
			return;
		}

		double delta = event.getScrollDelta();
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