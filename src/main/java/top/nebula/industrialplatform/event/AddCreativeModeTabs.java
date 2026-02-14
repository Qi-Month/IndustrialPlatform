package top.nebula.industrialplatform.event;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import top.nebula.industrialplatform.IndustrialPlatform;
import top.nebula.industrialplatform.block.BlockRegister;

@EventBusSubscriber(modid = IndustrialPlatform.MODID)
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