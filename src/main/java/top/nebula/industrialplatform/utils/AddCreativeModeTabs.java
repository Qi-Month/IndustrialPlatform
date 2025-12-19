package top.nebula.industrialplatform.utils;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.nebula.industrialplatform.block.BlockRegister;

public class AddCreativeModeTabs {
	public static void register(IEventBus event) {
		event.register(AddCreativeModeTabs.class);
	}

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