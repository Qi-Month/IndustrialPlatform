package top.qm.industrialplatform.block;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.qm.industrialplatform.IndustrialPlatform;
import top.qm.industrialplatform.block.custom.IndustrialPlatformBlock;

public class BlockRegister {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, IndustrialPlatform.MOD_ID);

    public static final RegistryObject<Block> INDUSTRIAL_PLATFORM;

    public static void register(IEventBus event) {
        BLOCKS.register(event);
    }

    static {
        INDUSTRIAL_PLATFORM = BLOCKS.register("industrial_platform", IndustrialPlatformBlock::new);
    }
}