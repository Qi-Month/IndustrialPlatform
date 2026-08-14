package dev.celestiacraft.industrialplatform.event;

import dev.celestiacraft.industrialplatform.api.ICheckModLoaded;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.celestiacraft.industrialplatform.block.IPBlocks;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AddCreativeModeTabs {
	@SubscribeEvent
	public static void buildContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
			event.accept(IPBlocks.INDUSTRIAL_PLATFORM.get().asItem());
			if (ICheckModLoaded.hasCreate()) {
				event.accept(IPBlocks.FLUID_POOL.get().asItem());
			}
		}
	}
}