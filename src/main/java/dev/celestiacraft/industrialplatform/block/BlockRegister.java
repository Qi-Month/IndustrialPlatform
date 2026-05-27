package dev.celestiacraft.industrialplatform.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.block.pool.FluidPoolBlock;
import dev.celestiacraft.industrialplatform.block.pool.FluidPoolItem;
import dev.celestiacraft.industrialplatform.block.platform.PlatformBlock;
import dev.celestiacraft.industrialplatform.block.platform.PlatformItem;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegister {
	public static final DeferredRegister<Block> BLOCKS;
	public static final DeferredRegister<Item> ITEMS;

	public static final Supplier<Block> INDUSTRIAL_PLATFORM;
	public static final Supplier<Block> FLUID_POOL;

	static {
		BLOCKS = DeferredRegister.createBlocks(IndustrialPlatform.MODID);
		ITEMS = DeferredRegister.createItems(IndustrialPlatform.MODID);

		INDUSTRIAL_PLATFORM = registerBlock("industrial_platform", PlatformBlock::new, (block) -> {
			return new PlatformItem(block, new Item.Properties());
		});

		FLUID_POOL = registerBlock("fluid_pool", FluidPoolBlock::new, (block) -> {
			return new FluidPoolItem(block, new Item.Properties());
		});
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