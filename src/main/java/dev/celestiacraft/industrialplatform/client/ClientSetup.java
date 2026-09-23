package dev.celestiacraft.industrialplatform.client;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.client.hologram.HologramOverlay;
import dev.celestiacraft.industrialplatform.client.screen.PlatformBuildScreen;
import dev.celestiacraft.industrialplatform.client.screen.PlatformDesignerScreen;
import dev.celestiacraft.industrialplatform.common.register.IPMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class ClientSetup {
	@SubscribeEvent
	public static void onRegisterScreens(RegisterMenuScreensEvent event) {
		event.register(IPMenus.PLATFORM_BUILD.get(), PlatformBuildScreen::new);
		event.register(IPMenus.PLATFORM_DESIGNER.get(), PlatformDesignerScreen::new);
	}

	@SubscribeEvent
	public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
		event.registerBelow(VanillaGuiLayers.CROSSHAIR, HologramOverlay.ID, HologramOverlay::render);
	}
}