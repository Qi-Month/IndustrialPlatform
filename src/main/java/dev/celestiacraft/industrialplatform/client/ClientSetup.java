package dev.celestiacraft.industrialplatform.client;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.client.screen.PlatformBuildScreen;
import dev.celestiacraft.industrialplatform.client.screen.PlatformDesignerScreen;
import dev.celestiacraft.industrialplatform.common.register.IPMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class ClientSetup {
	@SubscribeEvent
	public static void onRegisterScreens(RegisterMenuScreensEvent event) {
		event.register(IPMenus.PLATFORM_BUILD.get(), PlatformBuildScreen::new);
		event.register(IPMenus.PLATFORM_DESIGNER.get(), PlatformDesignerScreen::new);
	}
}