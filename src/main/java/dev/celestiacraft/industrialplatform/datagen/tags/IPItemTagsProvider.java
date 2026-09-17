package dev.celestiacraft.industrialplatform.datagen.tags;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class IPItemTagsProvider extends ItemTagsProvider {
	public IPItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, BlockTagsProvider blockTags, @Nullable ExistingFileHelper helper) {
		super(output, provider, blockTags.contentsGetter(), IndustrialPlatform.MODID, helper);
	}

	@Override
	protected void addTags(HolderLookup.@NotNull Provider provider) {
		tag(IPTags.Items.STONE)
				.add(Items.DEEPSLATE)
				.add(Items.COBBLED_DEEPSLATE)
				.addTag(Tags.Items.COBBLESTONE);

		tag(IPTags.Items.DEEPSLATE)
				.add(Items.DEEPSLATE)
				.add(Items.COBBLED_DEEPSLATE);

		// 可以放进平台搭建界面材料槽的物品
		tag(IPTags.Items.PLATFORM_MATERIAL)
				.addTag(Tags.Items.STONE)
				.addTag(Tags.Items.COBBLESTONE)
				.add(Items.STONE)
				.add(Items.COBBLESTONE)
				.add(Items.DEEPSLATE)
				.add(Items.COBBLED_DEEPSLATE)
				.add(Items.ANDESITE)
				.add(Items.DIORITE)
				.add(Items.GRANITE);
	}
}