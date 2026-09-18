package dev.celestiacraft.industrialplatform.common.register;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.item.FillAdjusterItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IPItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IndustrialPlatform.MODID);

	public static final RegistryObject<Item> FILL_ADJUSTER;

	static {
		FILL_ADJUSTER = ITEMS.register("fill_adjuster", FillAdjusterItem::new);
	}

	public static void register(IEventBus bus) {
		ITEMS.register(bus);
	}
}