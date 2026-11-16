package dev.celestiacraft.industrialplatform.client;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.client.hologram.HologramOverlay;
import dev.celestiacraft.industrialplatform.client.hologram.HologramRenderTypes;
import dev.celestiacraft.industrialplatform.client.preview.PlatformPreviewRenderer;
import dev.celestiacraft.industrialplatform.client.screen.PlatformBuildScreen;
import dev.celestiacraft.industrialplatform.client.screen.PlatformBuilderScreen;
import dev.celestiacraft.industrialplatform.common.register.IPMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class ClientSetup {
	@SubscribeEvent
	public static void onRegisterScreens(RegisterMenuScreensEvent event) {
		event.register(IPMenus.PLATFORM_BUILD.get(), PlatformBuildScreen::new);
		event.register(IPMenus.PLATFORM_BUILDER.get(), PlatformBuilderScreen::new);
	}

	@SubscribeEvent
	public static void onRegisterPictureRenderers(RegisterPictureInPictureRenderersEvent event) {
		event.register(PlatformPreviewRenderer.State.class, PlatformPreviewRenderer.PictureRenderer::new);
	}

	@SubscribeEvent
	public static void onRegisterPipelines(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(HologramRenderTypes.FACES_PIPELINE);
	}

	@SubscribeEvent
	public static void onResourcesLoaded(ClientResourceLoadFinishedEvent event) {
		ClientPreviewHandler.invalidateResources();
	}

	@SubscribeEvent
	public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
		event.registerBelow(VanillaGuiLayers.CROSSHAIR, HologramOverlay.ID, HologramOverlay::render);
	}
}