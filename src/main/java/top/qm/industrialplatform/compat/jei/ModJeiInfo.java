package top.qm.industrialplatform.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import top.qm.industrialplatform.IndustrialPlatform;

import java.util.List;
import java.util.Objects;

public class ModJeiInfo {
	private static IRecipeRegistration registration;

	static MutableComponent setTran(String key) {
		return Component.translatable(String.format("jei.info.%s.%s", IndustrialPlatform.MODID, key));
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

		List<ItemStack> stacks = Objects.requireNonNull(ForgeRegistries.ITEMS.tags())
				.getTag(tag)
				.stream()
				.map(ItemStack::new)
				.toList();

		if (stacks.isEmpty()) {
			return;
		}

		registration.addIngredientInfo(stacks, VanillaTypes.ITEM_STACK, setTran(key));
	}
}