package dev.celestiacraft.industrialplatform.common.register;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.block.builder.PlatformBuilderBlock;
import dev.celestiacraft.industrialplatform.common.block.builder.PlatformBuilderItem;
import dev.celestiacraft.industrialplatform.common.block.platform.PlatformBlock;
import dev.celestiacraft.industrialplatform.common.block.platform.PlatformItem;
import dev.celestiacraft.industrialplatform.common.block.pool.FluidPoolBlock;
import dev.celestiacraft.industrialplatform.common.block.pool.FluidPoolItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class IPBlocks {
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(IndustrialPlatform.MODID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IndustrialPlatform.MODID);

	public static final DeferredBlock<Block> INDUSTRIAL_PLATFORM = registerBlock("industrial_platform", PlatformBlock::new, PlatformItem::new,
			() -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_BRICKS).noOcclusion());
	public static final DeferredBlock<Block> PLATFORM_BUILDER = registerBlock("platform_builder", PlatformBuilderBlock::new, PlatformBuilderItem::new,
			() -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_TILES));
	public static final DeferredBlock<Block> FLUID_POOL = registerBlock("fluid_pool", FluidPoolBlock::new, FluidPoolItem::new,
			() -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_BRICKS).noOcclusion());

	public static void register(IEventBus event) {
		BLOCKS.register(event);
		ITEMS.register(event);
	}

	private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> factory,
			BiFunction<T, Item.Properties, Item> itemFactory, Supplier<BlockBehaviour.Properties> properties) {
		DeferredBlock<T> block = BLOCKS.registerBlock(name, factory, properties);
		ITEMS.registerItem(name, itemProperties -> itemFactory.apply(block.get(), itemProperties.useBlockDescriptionPrefix()));
		return block;
	}
}
