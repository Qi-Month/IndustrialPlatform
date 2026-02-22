package dev.celestiacraft.industrialplatform.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.block.pool.FluidPoolBlock;
import dev.celestiacraft.industrialplatform.block.pool.FluidPoolItem;
import dev.celestiacraft.industrialplatform.block.platform.PlatformBlock;
import dev.celestiacraft.industrialplatform.block.platform.PlatformItem;
import dev.celestiacraft.industrialplatform.api.ICheckModLoaded;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegister {
	private static final DeferredRegister<Block> BLOCKS;
	private static final DeferredRegister<Item> ITEMS;

	public static final Supplier<Block> INDUSTRIAL_PLATFORM;
	public static final Supplier<Block> FLUID_POOL;

	static {
		BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, IndustrialPlatform.MODID);
		ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IndustrialPlatform.MODID);

		INDUSTRIAL_PLATFORM = registerBlock("industrial_platform", PlatformBlock::new, PlatformItem::new);

		if (ICheckModLoaded.hasCreate()) {
			FLUID_POOL = registerBlock("fluid_pool", FluidPoolBlock::new, FluidPoolItem::new);
		} else {
			FLUID_POOL = null;
		}
	}

	public static void register(IEventBus event) {
		BLOCKS.register(event);
		ITEMS.register(event);
	}

	private static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> supplier, Function<T, Item> item) {
		Supplier<T> block = BLOCKS.register(name, supplier);

		ITEMS.register(name, () -> {
			return item.apply(block.get());
		});
		return block;
	}
}