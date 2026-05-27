package dev.celestiacraft.industrialplatform.datagen.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class IPLootTableProvider extends LootTableProvider {
	public IPLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
		super(
				output,
				Set.of(),
				List.of(new SubProviderEntry(IPBlockLoot::new, LootContextParamSets.BLOCK))
		);
	}
}