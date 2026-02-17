package dev.celestiacraft.industrialplatform.event;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.celestiacraft.industrialplatform.block.BlockRegister;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AddCreativeModeTabs {
	@SubscribeEvent
	public static void buildContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(BlockRegister.INDUSTRIAL_PLATFORM.get().asItem());
			if (BlockRegister.FLUID_POOL != null) {
				event.accept(BlockRegister.FLUID_POOL.get().asItem());
			}
		}
	}
}