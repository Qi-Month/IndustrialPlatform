package dev.celestiacraft.industrialplatform.event;

import dev.celestiacraft.industrialplatform.api.ICheckModLoaded;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.block.BlockRegister;

@EventBusSubscriber(modid = IndustrialPlatform.MODID)
public class AddCreativeModeTabs {
	@SubscribeEvent
	public static void buildContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
			event.accept(BlockRegister.INDUSTRIAL_PLATFORM.get().asItem());
			if (ICheckModLoaded.hasCreate()) {
				event.accept(BlockRegister.FLUID_POOL.get().asItem());
			}
		}
	}
}