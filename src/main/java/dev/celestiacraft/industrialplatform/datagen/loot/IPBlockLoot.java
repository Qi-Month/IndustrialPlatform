package dev.celestiacraft.industrialplatform.datagen.loot;

import dev.celestiacraft.industrialplatform.block.BlockRegister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class IPBlockLoot extends BlockLootSubProvider {
	protected IPBlockLoot(Set<Item> explosionResistant, FeatureFlagSet enabledFeatures, Map<ResourceKey<LootTable>, LootTable.Builder> map, HolderLookup.Provider registries) {
		super(explosionResistant, enabledFeatures, map, registries);
	}

	@Override
	protected void generate() {
		dropSelf(BlockRegister.INDUSTRIAL_PLATFORM.get());
		dropSelf(BlockRegister.FLUID_POOL.get());
	}

	@Override
	protected @NotNull Iterable<Block> getKnownBlocks() {
		return BlockRegister.BLOCKS.getEntries()
				.stream()
				.map(Supplier::get)
				.collect(Collectors.toSet());
	}
}