package top.nebula.industrialplatform.block.custom.platform;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class PlatformItem extends BlockItem {
	public PlatformItem(Block block, Properties properties) {
		super(block, properties);
	}

	// 添加Tooltip
	@OnlyIn(Dist.CLIENT)
	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		if (CheckModLoaded.hasJei()) {
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