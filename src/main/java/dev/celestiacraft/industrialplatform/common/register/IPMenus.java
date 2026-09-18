package dev.celestiacraft.industrialplatform.common.register;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.menu.PlatformBuildMenu;
import dev.celestiacraft.industrialplatform.common.menu.PlatformDesignerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IPMenus {
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, IndustrialPlatform.MODID);

	public static final DeferredHolder<MenuType<?>, MenuType<PlatformBuildMenu>> PLATFORM_BUILD;
	public static final DeferredHolder<MenuType<?>, MenuType<PlatformDesignerMenu>> PLATFORM_DESIGNER;

	static {
		PLATFORM_BUILD = MENUS.register("platform_build", () -> {
			return IMenuTypeExtension.create(PlatformBuildMenu::new);
		});
		PLATFORM_DESIGNER = MENUS.register("platform_designer", () -> {
			return IMenuTypeExtension.create(PlatformDesignerMenu::new);
		});
	}

	public static void register(IEventBus bus) {
		MENUS.register(bus);
	}
}