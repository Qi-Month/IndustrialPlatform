package dev.celestiacraft.industrialplatform.datagen.tags;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPTags;
import dev.celestiacraft.industrialplatform.block.BlockRegister;
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
				.addTag(Tags.Blocks.SANDS)
				.addTag(Tags.Blocks.SANDSTONE_BLOCKS)
				.addTag(Tags.Blocks.NETHERRACKS)
				.addOptionalTag(Tags.Blocks.ORES)
				.addOptionalTag(Tags.Blocks.STONES)
				.addOptionalTag(Tags.Blocks.COBBLESTONES)
				.addOptionalTag(IPTags.Blocks.DEEPSLATE);

		tag(Tags.Blocks.NEEDS_WOOD_TOOL)
				.add(BlockRegister.INDUSTRIAL_PLATFORM.get())
				.add(BlockRegister.FLUID_POOL.get());

		tag(BlockTags.MINEABLE_WITH_PICKAXE)
				.add(BlockRegister.INDUSTRIAL_PLATFORM.get())
				.add(BlockRegister.FLUID_POOL.get());
	}
}