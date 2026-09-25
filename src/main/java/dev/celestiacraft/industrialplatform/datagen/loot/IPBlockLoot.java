package dev.celestiacraft.industrialplatform.datagen.loot;

import dev.celestiacraft.industrialplatform.common.register.IPBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class IPBlockLoot extends BlockLootSubProvider {
	public IPBlockLoot(HolderLookup.Provider registries) {
		super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
	}

	@Override
	protected void generate() {
		dropSelf(IPBlocks.INDUSTRIAL_PLATFORM.get());
		dropSelf(IPBlocks.PLATFORM_BUILDER.get());
		dropSelf(IPBlocks.FLUID_POOL.get());
	}

	@Override
	protected @NotNull Iterable<Block> getKnownBlocks() {
		return IPBlocks.BLOCKS.getEntries()
				.stream()
				.map(Supplier::get)
				.collect(Collectors.toSet());
	}
}