package dev.celestiacraft.industrialplatform.datagen.tags;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.references.BlockItemIds;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IPItemTagsProvider extends TagsProvider<Item> {
	public IPItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
		super(output, Registries.ITEM, provider, IndustrialPlatform.MODID);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(IPTags.Items.STONE)
				.addAll(List.of(BlockItemIds.DEEPSLATE.item(), BlockItemIds.COBBLED_DEEPSLATE.item()))
				.addTag(Tags.BlockItems.COBBLESTONES.item());

		tag(IPTags.Items.DEEPSLATE)
				.addAll(List.of(BlockItemIds.DEEPSLATE.item(), BlockItemIds.COBBLED_DEEPSLATE.item()));

		tag(IPTags.Items.PLATFORM_MATERIAL)
				.addTag(Tags.BlockItems.STONES.item())
				.addTag(Tags.BlockItems.COBBLESTONES.item())
				.addAll(List.of(BlockItemIds.STONE.item(), BlockItemIds.COBBLESTONE.item(), BlockItemIds.DEEPSLATE.item(),
						BlockItemIds.COBBLED_DEEPSLATE.item(), BlockItemIds.ANDESITE.item(), BlockItemIds.DIORITE.item(), BlockItemIds.GRANITE.item()));
	}
}