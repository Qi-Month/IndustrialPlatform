package dev.celestiacraft.industrialplatform.api;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class IPTags {
	public static class Items {
		public static TagKey<Item> createTag(String namespace, String name) {
			return ItemTags.create(ResourceLocation.fromNamespaceAndPath(namespace, name));
		}

		public static TagKey<Item>
				STONE,
				WRENCH,
				DEEPSLATE;

		static {
			STONE = createTag("forge", "stone");
			WRENCH = createTag("forge", "tools/wrench");
			DEEPSLATE = createTag("forge", "deepslate");
		}
	}

	public static class Blocks {
		private static TagKey<Block> createTag(String namespace, String name) {
			return BlockTags.create(ResourceLocation.fromNamespaceAndPath(namespace, name));
		}

		public static TagKey<Block>
				NO_DROP_BLOCKS,
				DEEPSLATE;

		static {
			NO_DROP_BLOCKS = createTag(IndustrialPlatform.MODID, "no_drop_blocks");
			DEEPSLATE = createTag("forge", "deepslate");
		}
	}
}