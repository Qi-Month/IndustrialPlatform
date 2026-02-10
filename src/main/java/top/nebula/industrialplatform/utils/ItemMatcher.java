package top.nebula.industrialplatform.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ItemMatcher {
	public static boolean matches(ItemStack stack, ForgeConfigSpec.ConfigValue<List<? extends String>> configValue) {
		if (stack.isEmpty()) {
			return false;
		}
		List<? extends String> entries = configValue.get();

		for (String entry : entries) {
			if (entry.startsWith("#")) {
				String tagId = entry.substring(1);
				ResourceLocation loc = ResourceLocation.tryParse(tagId);

				if (loc != null) {
					TagKey<Item> tag = ItemTags.create(loc);
					if (stack.is(tag)) {
						return true;
					}
				}
			} else {
				ResourceLocation location = ResourceLocation.tryParse(entry);
				if (location != null && stack.getItem().equals(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(location))) {
					return true;
				}
			}
		}
		return false;
	}
}