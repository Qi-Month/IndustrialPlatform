package dev.celestiacraft.industrialplatform.datagen.loot;

import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

public class IPLootTableProvider extends LootTableProvider {
	public IPLootTableProvider() {
		super(Set.of(), List.of(new SubProviderEntry(IPBlockLoot::new, LootContextParamSets.BLOCK)));
	}
}