package top.nebula.industrialplatform.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.nebula.industrialplatform.IndustrialPlatform;
import top.nebula.industrialplatform.block.BlockRegister;

import static top.nebula.industrialplatform.compat.jei.ModJeiInfo.*;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin {
	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return IndustrialPlatform.loadResource("jei_plugin");
	}

	@Override
	public void registerRecipes(@NotNull IRecipeRegistration registration) {
		init(registration);

		addJeiInfo(BlockRegister.INDUSTRIAL_PLATFORM.get()
						.asItem()
						.getDefaultInstance(),
				"platform"
		);
		if (BlockRegister.FLUID_POOL != null) {
			addJeiInfo(BlockRegister.FLUID_POOL.get()
							.asItem()
							.getDefaultInstance(),
					"fluid_pool"
			);
		}
	}
}