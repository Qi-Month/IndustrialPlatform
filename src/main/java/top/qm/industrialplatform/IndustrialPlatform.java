package top.qm.industrialplatform;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.qm.industrialplatform.block.BlockItemRegister;
import top.qm.industrialplatform.block.BlockRegister;

@Mod(IndustrialPlatform.MOD_ID)
public class IndustrialPlatform {
    public static final String MOD_ID = "industrial_platform";

    public IndustrialPlatform(FMLJavaModLoadingContext context) {
        IEventBus event = context.getModEventBus();

        BlockRegister.register(event);
        BlockItemRegister.register(event);


        AddCreativeModeTabs.register(event);
    }
}