package dev.celestiacraft.industrialplatform.common.item;

import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformProperties;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 填充调节器: 手持时滚滚轮就能改填充格数, 数值存在物品自己的组件里.
 * <p>
 * 站着滚动调"向上填充", 潜行滚动调"向下填充", 按住 Ctrl 一次 10 格, 按住 Alt 放行给快捷栏.
 */
public class FillAdjusterItem extends Item {
	private static final String UP_KEY = "UpFill";
	private static final String DOWN_KEY = "DownFill";

	public FillAdjusterItem() {
		super(new Item.Properties().stacksTo(1));
	}

	public static int getUpFill(ItemStack stack) {
		return readFill(stack, UP_KEY, CommonConfig.TOP_FILLING_DISTANCE.get());
	}

	public static int getDownFill(ItemStack stack) {
		return readFill(stack, DOWN_KEY, CommonConfig.BOTTOM_FILLING_DISTANCE.get());
	}

	private static int readFill(ItemStack stack, String key, int fallback) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

		if (!tag.contains(key)) {
			return Mth.clamp(fallback, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);
		}

		return Mth.clamp(tag.getInt(key), PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);
	}

	public static void setFills(ItemStack stack, int upFill, int downFill) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
			tag.putInt(UP_KEY, Mth.clamp(upFill, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE));
			tag.putInt(DOWN_KEY, Mth.clamp(downFill, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE));
		});
	}

	public static int step(boolean up) {
		return up ? 1 : -1;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		tooltip.add(Component.translatable("tooltip.industrial_platform.fill_adjuster.up", getUpFill(stack)).withStyle(ChatFormatting.AQUA));
		tooltip.add(Component.translatable("tooltip.industrial_platform.fill_adjuster.down", getDownFill(stack)).withStyle(ChatFormatting.AQUA));
		tooltip.add(Component.translatable("tooltip.industrial_platform.fill_adjuster.usage").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("tooltip.industrial_platform.fill_adjuster.alt").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("tooltip.industrial_platform.fill_adjuster.deploy").withStyle(ChatFormatting.GRAY));
	}
}