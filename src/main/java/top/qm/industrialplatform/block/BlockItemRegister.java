package top.qm.industrialplatform.block;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.qm.industrialplatform.IndustrialPlatform;
import top.qm.industrialplatform.block.custom.platform.IndustrialPlatformItem;

public class BlockItemRegister {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IndustrialPlatform.MODID);

    public static final RegistryObject<Item> INDUSTRIAL_PLATFORM;

    public static void register(IEventBus event) {
        ITEMS.register(event);
    }

    static {
        INDUSTRIAL_PLATFORM = ITEMS.register("industrial_platform", () -> {
            return new IndustrialPlatformItem(BlockRegister.INDUSTRIAL_PLATFORM.get(), new Item.Properties());
        });
    }
}