package top.qm.industrialplatform.utils;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.qm.industrialplatform.block.BlockItemRegister;

public class AddCreativeModeTabs {
    public static void register(IEventBus event) {
        event.register(AddCreativeModeTabs.class);
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(BlockItemRegister.INDUSTRIAL_PLATFORM.get());
            if (CheckModLoaded.hasCreate()) {
                event.accept(BlockItemRegister.FLUID_POOL.get());
            }
        }
    }
}