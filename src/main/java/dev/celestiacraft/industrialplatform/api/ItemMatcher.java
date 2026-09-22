package dev.celestiacraft.industrialplatform.api;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemMatcher {
	private static final Map<ForgeConfigSpec.ConfigValue<List<? extends String>>, MatchRules> CACHE = new ConcurrentHashMap<>();

	public static boolean matches(ItemStack stack, ForgeConfigSpec.ConfigValue<List<? extends String>> configValue) {
		if (stack.isEmpty()) {
			return false;
		}
		List<? extends String> entries = configValue.get();
		MatchRules rules = CACHE.get(configValue);
		if (rules == null || !rules.entries().equals(entries)) {
			rules = parse(entries);
			CACHE.put(configValue, rules);
		}

		for (int index = 0; index < rules.tags().size(); index++) {
			if (stack.is(rules.tags().get(index))) {
				return true;
			}
		}
		for (int index = 0; index < rules.itemIds().size(); index++) {
			if (stack.is(BuiltInRegistries.ITEM.get(rules.itemIds().get(index)))) {
				return true;
			}
		}
		return false;
	}

	private static MatchRules parse(List<? extends String> entries) {
		List<? extends String> snapshot = List.copyOf(entries);
		List<TagKey<Item>> tags = new ArrayList<>();
		List<ResourceLocation> itemIds = new ArrayList<>();
		for (String entry : snapshot) {
			boolean isTag = entry.startsWith("#");
			ResourceLocation location = ResourceLocation.tryParse(isTag ? entry.substring(1) : entry);
			if (location == null) {
				continue;
			}
			if (isTag) {
				tags.add(ItemTags.create(location));
			} else {
				itemIds.add(location);
			}
		}
		return new MatchRules(snapshot, List.copyOf(tags), List.copyOf(itemIds));
	}

	private record MatchRules(List<? extends String> entries, List<TagKey<Item>> tags, List<ResourceLocation> itemIds) {
	}
}
