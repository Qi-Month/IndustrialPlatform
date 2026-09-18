package dev.celestiacraft.industrialplatform.common.register;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.menu.PlatformBuildMenu;
import dev.celestiacraft.industrialplatform.common.menu.PlatformDesignerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IPMenus {
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, IndustrialPlatform.MODID);

	public static final RegistryObject<MenuType<PlatformBuildMenu>> PLATFORM_BUILD;
	public static final RegistryObject<MenuType<PlatformDesignerMenu>> PLATFORM_DESIGNER;

	static {
		PLATFORM_BUILD = MENUS.register("platform_build", () -> {
			return IForgeMenuType.create(PlatformBuildMenu::new);
		});

		PLATFORM_DESIGNER = MENUS.register("platform_designer", () -> {
			return IForgeMenuType.create(PlatformDesignerMenu::new);
		});
	}

	public static void register(IEventBus bus) {
		MENUS.register(bus);
	}
}