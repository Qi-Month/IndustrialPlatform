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
		private static TagKey<Item> createTag(String namespace, String name) {
			return ItemTags.create(ResourceLocation.fromNamespaceAndPath(namespace, name));
		}

		public static TagKey<Item>
				STONE,
				WRENCH,
				DEEPSLATE,
		/**
		 * 可以用于搭建平台的材料, 搭建界面的槽位接受这个物品标签
		 */
		PLATFORM_MATERIAL;

		static {
			STONE = createTag("forge", "stone");
			WRENCH = createTag("forge", "tools/wrench");
			DEEPSLATE = createTag("forge", "deepslate");
			PLATFORM_MATERIAL = createTag(IndustrialPlatform.MODID, "platform_material");
		}
	}

	public static class Blocks {
		private static TagKey<Block> createTag(String namespace, String name) {
			return BlockTags.create(ResourceLocation.fromNamespaceAndPath(namespace, name));
		}

		public static TagKey<Block>
				NO_DROP_BLOCKS,
				DEEPSLATE,
				CONCRETE;

		static {
			NO_DROP_BLOCKS = createTag(IndustrialPlatform.MODID, "no_drop_blocks");
			DEEPSLATE = createTag("forge", "deepslate");
			CONCRETE = createTag(IndustrialPlatform.MODID, "concretes");
		}
	}
}