package dev.celestiacraft.industrialplatform.common.register;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.item.FillAdjusterItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IPItems {
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IndustrialPlatform.MODID);

	public static final DeferredItem<Item> FILL_ADJUSTER;

	static {
		FILL_ADJUSTER = ITEMS.register("fill_adjuster", FillAdjusterItem::new);
	}

	public static void register(IEventBus bus) {
		ITEMS.register(bus);
	}
}