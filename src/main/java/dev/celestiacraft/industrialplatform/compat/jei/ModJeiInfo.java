package dev.celestiacraft.industrialplatform.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;

import java.util.List;

public class ModJeiInfo {
	private static IRecipeRegistration registration;

	static MutableComponent setTran(String key) {
		String tranKey = String.format("jei.info.%s.%s", IndustrialPlatform.MODID, key);
		return Component.translatable(tranKey);
	}

	public static void init(IRecipeRegistration registration) {
		ModJeiInfo.registration = registration;
	}

	public static void addJeiInfo(ItemStack item, String key) {
		if (registration != null && !item.isEmpty()) {
			registration.addIngredientInfo(item, VanillaTypes.ITEM_STACK, setTran(key));
		}
	}

	public static void addJeiInfo(TagKey<Item> tag, String key) {
		if (registration == null) {
			return;
		}

		List<ItemStack> stacks = BuiltInRegistries.ITEM.getTag(tag)
				.stream()
				.flatMap(HolderSet.ListBacked::stream)
				.map(ItemStack::new)
				.toList();

		if (stacks.isEmpty()) {
			return;
		}

		registration.addIngredientInfo(stacks, VanillaTypes.ITEM_STACK, setTran(key));
	}
}