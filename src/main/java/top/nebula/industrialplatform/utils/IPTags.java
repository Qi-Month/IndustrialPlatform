package top.nebula.industrialplatform.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class IPTags {
	public static class Items {
		public static TagKey<Item> createTag(String namespace, String name) {
			return ItemTags.create(ResourceLocation.fromNamespaceAndPath(namespace, name));
		}

		public static TagKey<Item> STONE;
		public static TagKey<Item> WRENCH;

		static {
			STONE = createTag("forge", "stone");
			WRENCH = createTag("forge", "tools/wrench");
		}
	}
}