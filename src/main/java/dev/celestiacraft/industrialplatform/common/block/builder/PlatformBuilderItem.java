package dev.celestiacraft.industrialplatform.common.block.builder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class PlatformBuilderItem extends BlockItem {
	public PlatformBuilderItem(Block block) {
		super(block, new Properties());
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		if (Screen.hasShiftDown()) {
			String translated = Component.translatable("tooltip.industrial_platform.platform_builder").getString();
			for (String line : translated.split("\n")) {
				tooltip.add(Component.literal(line));
			}
		} else {
			tooltip.add(Component.translatable("tooltip.industrial_platform.platform_builder.off"));
		}
	}
}