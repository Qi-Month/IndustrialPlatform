package dev.celestiacraft.industrialplatform.datagen.tags;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPTags;
import dev.celestiacraft.industrialplatform.common.register.IPBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class IPBlockTagsProvider extends BlockTagsProvider {
	public IPBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper helper) {
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
				.addTag(Tags.Blocks.SAND)
				.addTag(Tags.Blocks.SANDSTONE)
				.addTag(Tags.Blocks.NETHERRACK)
				.addOptionalTag(Tags.Blocks.ORES)
				.addOptionalTag(Tags.Blocks.STONE)
				.addOptionalTag(Tags.Blocks.COBBLESTONE)
				.addOptionalTag(IPTags.Blocks.DEEPSLATE);

		tag(Tags.Blocks.NEEDS_WOOD_TOOL)
				.add(IPBlocks.INDUSTRIAL_PLATFORM.get())
				.add(IPBlocks.PLATFORM_BUILDER.get())
				.add(IPBlocks.FLUID_POOL.get());

		tag(BlockTags.MINEABLE_WITH_PICKAXE)
				.add(IPBlocks.INDUSTRIAL_PLATFORM.get())
				.add(IPBlocks.PLATFORM_BUILDER.get())
				.add(IPBlocks.FLUID_POOL.get());
	}
}