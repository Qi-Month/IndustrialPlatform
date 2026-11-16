package dev.celestiacraft.industrialplatform.datagen.tags;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class IPItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {
	public IPItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
		super(output, Registries.ITEM, provider, item -> item.builtInRegistryHolder().key(), IndustrialPlatform.MODID);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(IPTags.Items.STONE)
				.add(Items.DEEPSLATE)
				.add(Items.COBBLED_DEEPSLATE)
				.addTag(Tags.Items.COBBLESTONES);

		tag(IPTags.Items.DEEPSLATE)
				.add(Items.DEEPSLATE)
				.add(Items.COBBLED_DEEPSLATE);

		tag(IPTags.Items.PLATFORM_MATERIAL)
				.addTag(Tags.Items.STONES)
				.addTag(Tags.Items.COBBLESTONES)
				.add(Items.STONE)
				.add(Items.COBBLESTONE)
				.add(Items.DEEPSLATE)
				.add(Items.COBBLED_DEEPSLATE)
				.add(Items.ANDESITE)
				.add(Items.DIORITE)
				.add(Items.GRANITE);
	}
}