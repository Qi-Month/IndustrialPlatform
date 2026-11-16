package dev.celestiacraft.industrialplatform.datagen.tags;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPTags;
import dev.celestiacraft.industrialplatform.common.register.IPBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IPBlockTagsProvider extends BlockTagsProvider {
	public IPBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
		super(output, provider, IndustrialPlatform.MODID);
	}

	@Override
	protected void addTags(HolderLookup.@NotNull Provider provider) {
		tag(IPTags.Blocks.NO_DROP_BLOCKS)
				.addAll(List.of(BlockItemIds.STONE.block(), BlockItemIds.GRANITE.block(), BlockItemIds.DIORITE.block(),
						BlockItemIds.ANDESITE.block(), BlockItemIds.COBBLESTONE.block(), BlockItemIds.COBBLED_DEEPSLATE.block(),
						BlockItemIds.TUFF.block(), BlockItemIds.CALCITE.block(), BlockItemIds.DIRT.block(), BlockItemIds.GRASS_BLOCK.block(),
						BlockItemIds.GRAVEL.block(), BlockItemIds.END_STONE.block(), BlockItemIds.DEEPSLATE_TILES.block()))
				.addTag(Tags.BlockItems.SANDS.block())
				.addTag(Tags.BlockItems.SANDSTONE_BLOCKS.block())
				.addTag(Tags.BlockItems.NETHERRACKS.block())
				.addTag(IPTags.Blocks.CONCRETE)
				.addOptionalTag(Tags.BlockItems.ORES.block())
				.addOptionalTag(Tags.BlockItems.STONES.block())
				.addOptionalTag(Tags.BlockItems.COBBLESTONES.block())
				.addOptionalTag(IPTags.Blocks.DEEPSLATE);

		List<ResourceKey<Block>> controllers = List.of(IPBlocks.INDUSTRIAL_PLATFORM.key(),
				IPBlocks.PLATFORM_BUILDER.key(), IPBlocks.FLUID_POOL.key());
		tag(Tags.Blocks.NEEDS_WOOD_TOOL).addAll(controllers);
		tag(BlockTags.MINEABLE_WITH_PICKAXE).addAll(controllers);

		TagAppender<Block> concrete = tag(IPTags.Blocks.CONCRETE);
		BlockItemIds.CONCRETE.forEach(id -> concrete.add(id.block()));
	}
}