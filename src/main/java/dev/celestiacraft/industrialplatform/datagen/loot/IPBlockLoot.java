package dev.celestiacraft.industrialplatform.datagen.loot;

import dev.celestiacraft.industrialplatform.block.BlockRegister;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Set;

public class IPBlockLoot extends BlockLootSubProvider {
	public IPBlockLoot() {
		super(Set.of(), FeatureFlags.REGISTRY.allFlags(), new HashMap<>());
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
				.map(RegistryObject::get)::iterator;
	}
}