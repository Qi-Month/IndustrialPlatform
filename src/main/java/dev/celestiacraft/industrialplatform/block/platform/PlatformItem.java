package dev.celestiacraft.industrialplatform.block.platform;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import dev.celestiacraft.industrialplatform.api.ICheckModLoaded;

import java.util.List;

public class PlatformItem extends BlockItem {
	public PlatformItem(Block block, Properties properties) {
		super(block, properties);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		if (ICheckModLoaded.hasJei()) {
			return;
		}

		if (Screen.hasShiftDown()) {
			String translated = Component.translatable("tooltip.industrial_platform.industrial_platform").getString();
			for (String line : translated.split("\n")) {
				tooltip.add(Component.literal(line));
			}
		} else {
			tooltip.add(Component.translatable("tooltip.industrial_platform.industrial_platform.off"));
		}
	}
}