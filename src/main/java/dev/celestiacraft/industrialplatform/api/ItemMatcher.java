package dev.celestiacraft.industrialplatform.api;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ItemMatcher {
	public static boolean matches(ItemStack stack, ModConfigSpec.ConfigValue<List<? extends String>> configValue) {
		if (stack.isEmpty()) {
			return false;
		}
		List<? extends String> entries = configValue.get();

		for (String entry : entries) {
			if (!entry.isEmpty() && entry.charAt(0) == '#') {
				String tagId = entry.substring(1);
				ResourceLocation loc = ResourceLocation.tryParse(tagId);
				if (loc != null) {
					TagKey<Item> tag = ItemTags.create(loc);
					if (stack.is(tag)) return true;
				}
			} else {
				ResourceLocation loc = ResourceLocation.tryParse(entry);
				if (loc != null && stack.getItem().equals(BuiltInRegistries.ITEM.get(loc))) {
					return true;
				}
			}
		}
		return false;
	}
}