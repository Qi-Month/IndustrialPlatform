package dev.celestiacraft.industrialplatform.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.client.screen.PlatformBuildScreen;
import dev.celestiacraft.industrialplatform.common.register.IPBlocks;

import java.util.List;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin {
	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return IndustrialPlatform.loadResource("jei_plugin");
	}

	@Override
	public void registerRecipes(@NotNull IRecipeRegistration registration) {
		ModJeiInfo.init(registration);

		ModJeiInfo.addJeiInfo(
				IPBlocks.INDUSTRIAL_PLATFORM.get()
						.asItem()
						.getDefaultInstance(),
				"platform"
		);
		if (IPBlocks.FLUID_POOL != null) {
			ModJeiInfo.addJeiInfo(
					IPBlocks.FLUID_POOL.get()
							.asItem()
							.getDefaultInstance(),
					"fluid_pool"
			);
		}
	}

	@Override
	public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(PlatformBuildScreen.class, new IGuiContainerHandler<>() {
			@Override
			public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull PlatformBuildScreen screen) {
				return screen.getExtraAreas();
			}
		});
	}
}