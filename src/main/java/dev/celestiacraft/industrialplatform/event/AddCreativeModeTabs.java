package dev.celestiacraft.industrialplatform.event;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.ICheckModLoaded;
import dev.celestiacraft.industrialplatform.common.register.IPBlocks;
import dev.celestiacraft.industrialplatform.common.register.IPItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = IndustrialPlatform.MODID)
public class AddCreativeModeTabs {
	@SubscribeEvent
	public static void buildContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
			event.accept(IPBlocks.INDUSTRIAL_PLATFORM.get().asItem());
			event.accept(IPBlocks.PLATFORM_BUILDER.get().asItem());
			event.accept(IPItems.FILL_ADJUSTER.get());

			if (ICheckModLoaded.hasCreate()) {
				event.accept(IPBlocks.FLUID_POOL.get().asItem());
			}
		}
	}
}