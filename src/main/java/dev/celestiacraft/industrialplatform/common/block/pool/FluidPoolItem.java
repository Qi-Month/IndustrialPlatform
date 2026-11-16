package dev.celestiacraft.industrialplatform.common.block.pool;

import dev.celestiacraft.industrialplatform.api.IPLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FluidPoolItem extends BlockItem {
	public FluidPoolItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
		IPLogic.appendShiftTooltip(flag, tooltip, "tooltip.industrial_platform.fluid_pool", "tooltip.industrial_platform.industrial_platform.off");
	}
}
