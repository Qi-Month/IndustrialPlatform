package dev.celestiacraft.industrialplatform.datagen.tags;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPTags;
import dev.celestiacraft.industrialplatform.common.register.IPBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class IPBlockTagsProvider extends BlockTagsProvider {
	public IPBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, net.neoforged.neoforge.common.data.ExistingFileHelper helper) {
		super(output, provider, IndustrialPlatform.MODID, helper);
	}

	@Override
	protected void addTags(HolderLookup.@NotNull Provider provider) {
		tag(IPTags.Blocks.NO_DROP_BLOCKS)
				.add(Blocks.STONE)
				.add(Blocks.GRANITE)
				.add(Blocks.DIORITE)
				.add(Blocks.ANDESITE)
				.add(Blocks.COBBLESTONE)
				.add(Blocks.COBBLED_DEEPSLATE)
				.add(Blocks.TUFF)
				.add(Blocks.CALCITE)
				.add(Blocks.DIRT)
				.add(Blocks.GRASS_BLOCK)
				.add(Blocks.GRAVEL)
				.add(Blocks.END_STONE)
				.add(Blocks.DEEPSLATE_TILES)
				.addTag(Tags.Blocks.SANDS)
				.addTag(Tags.Blocks.SANDSTONE_BLOCKS)
				.addTag(Tags.Blocks.NETHERRACKS)
				.addTag(IPTags.Blocks.CONCRETE)
				.addOptionalTag(Tags.Blocks.ORES)
				.addOptionalTag(Tags.Blocks.STONES)
				.addOptionalTag(Tags.Blocks.COBBLESTONES)
				.addOptionalTag(IPTags.Blocks.DEEPSLATE);

		tag(Tags.Blocks.NEEDS_WOOD_TOOL)
				.add(IPBlocks.INDUSTRIAL_PLATFORM.get())
				.add(IPBlocks.PLATFORM_BUILDER.get())
				.add(IPBlocks.FLUID_POOL.get());

		tag(BlockTags.MINEABLE_WITH_PICKAXE)
				.add(IPBlocks.INDUSTRIAL_PLATFORM.get())
				.add(IPBlocks.PLATFORM_BUILDER.get())
				.add(IPBlocks.FLUID_POOL.get());

		tag(IPTags.Blocks.CONCRETE)
				.add(Blocks.BLACK_CONCRETE)
				.add(Blocks.GRAY_CONCRETE)
				.add(Blocks.LIGHT_GRAY_CONCRETE)
				.add(Blocks.WHITE_CONCRETE)
				.add(Blocks.PINK_CONCRETE)
				.add(Blocks.RED_CONCRETE)
				.add(Blocks.ORANGE_CONCRETE)
				.add(Blocks.YELLOW_CONCRETE)
				.add(Blocks.GREEN_CONCRETE)
				.add(Blocks.CYAN_CONCRETE)
				.add(Blocks.LIME_CONCRETE)
				.add(Blocks.BLUE_CONCRETE)
				.add(Blocks.LIGHT_BLUE_CONCRETE)
				.add(Blocks.PURPLE_CONCRETE)
				.add(Blocks.MAGENTA_CONCRETE)
				.add(Blocks.BROWN_CONCRETE);
	}
}