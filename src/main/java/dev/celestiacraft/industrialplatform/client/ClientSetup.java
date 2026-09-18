package dev.celestiacraft.industrialplatform.client;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.client.screen.PlatformBuildScreen;
import dev.celestiacraft.industrialplatform.client.screen.PlatformDesignerScreen;
import dev.celestiacraft.industrialplatform.common.register.IPMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MenuScreens.register(IPMenus.PLATFORM_BUILD.get(), PlatformBuildScreen::new);
		MenuScreens.register(IPMenus.PLATFORM_DESIGNER.get(), PlatformDesignerScreen::new);
		});
	}
}