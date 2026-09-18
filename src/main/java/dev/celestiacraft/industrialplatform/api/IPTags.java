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
				DEEPSLATE,
				PLATFORM_MATERIAL;

		static {
			STONE = createTag("c", "stone");
			WRENCH = createTag("c", "tools/wrench");
			DEEPSLATE = createTag("c", "deepslate");
			PLATFORM_MATERIAL = createTag(IndustrialPlatform.MODID, "platform_material");
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
			DEEPSLATE = createTag("c", "deepslate");
		}
	}
}