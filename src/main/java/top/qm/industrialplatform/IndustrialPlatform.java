package top.qm.industrialplatform;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.qm.industrialplatform.block.BlockItemRegister;
import top.qm.industrialplatform.block.BlockRegister;
import top.qm.industrialplatform.utils.AddCreativeModeTabs;

@Mod(IndustrialPlatform.MODID)
public class IndustrialPlatform {
    public static final String MODID = "industrial_platform";

    public static ResourceLocation loadResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public IndustrialPlatform(FMLJavaModLoadingContext context) {
        IEventBus event = context.getModEventBus();

        BlockRegister.register(event);
        BlockItemRegister.register(event);

        AddCreativeModeTabs.register(event);
    }
}