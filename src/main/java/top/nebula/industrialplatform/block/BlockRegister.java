package top.nebula.industrialplatform.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.nebula.industrialplatform.IndustrialPlatform;
import top.nebula.industrialplatform.block.pool.FluidPoolBlock;
import top.nebula.industrialplatform.block.pool.FluidPoolItem;
import top.nebula.industrialplatform.block.platform.PlatformBlock;
import top.nebula.industrialplatform.block.platform.PlatformItem;
import top.nebula.industrialplatform.utils.ICheckModLoaded;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegister {
	private static final DeferredRegister<Block> BLOCKS;
	private static final DeferredRegister<Item> ITEMS;

	public static final Supplier<Block> INDUSTRIAL_PLATFORM;
	public static final Supplier<Block> FLUID_POOL;

	static {
		BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, IndustrialPlatform.MODID);
		ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, IndustrialPlatform.MODID);

		INDUSTRIAL_PLATFORM = registerBlock("industrial_platform", PlatformBlock::new, (block) -> {
			return new PlatformItem(block, new Item.Properties());
		});

		if (ICheckModLoaded.hasCreate()) {
			FLUID_POOL = registerBlock("fluid_pool", FluidPoolBlock::new, (block) -> {
				return new FluidPoolItem(block, new Item.Properties());
			});
		} else {
			FLUID_POOL = null;
		}
	}

	public static void register(IEventBus bus) {
		BLOCKS.register(bus);
		ITEMS.register(bus);
	}

	private static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> supplier, Function<T, Item> item) {
		Supplier<T> block = BLOCKS.register(name, supplier);

		ITEMS.register(name, () -> {
			return item.apply(block.get());
		});
		return block;
	}
}