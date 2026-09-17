package dev.celestiacraft.industrialplatform.block;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.block.designer.PlatformDesignerBlock;
import dev.celestiacraft.industrialplatform.block.designer.PlatformDesignerItem;
import dev.celestiacraft.industrialplatform.block.platform.PlatformBlock;
import dev.celestiacraft.industrialplatform.block.platform.PlatformItem;
import dev.celestiacraft.industrialplatform.block.pool.FluidPoolBlock;
import dev.celestiacraft.industrialplatform.block.pool.FluidPoolItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

public class IPBlocks {
	public static final DeferredRegister<Block> BLOCKS;
	public static final DeferredRegister<Item> ITEMS;

	public static final RegistryObject<Block> INDUSTRIAL_PLATFORM;
	public static final RegistryObject<Block> PLATFORM_DESIGNER;
	public static final RegistryObject<Block> FLUID_POOL;

	static {
		BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, IndustrialPlatform.MODID);
		ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IndustrialPlatform.MODID);

		INDUSTRIAL_PLATFORM = registerBlock("industrial_platform", PlatformBlock::new, PlatformItem::new);

		PLATFORM_DESIGNER = registerBlock("platform_designer", PlatformDesignerBlock::new, PlatformDesignerItem::new);

		FLUID_POOL = registerBlock("fluid_pool", FluidPoolBlock::new, FluidPoolItem::new);
	}

	public static void register(IEventBus event) {
		BLOCKS.register(event);
		ITEMS.register(event);
	}

	private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> supplier, Function<T, Item> item) {
		RegistryObject<T> block = BLOCKS.register(name, supplier);

		ITEMS.register(name, () -> {
			return item.apply(block.get());
		});
		return block;
	}
}