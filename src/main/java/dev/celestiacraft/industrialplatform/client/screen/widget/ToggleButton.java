package dev.celestiacraft.industrialplatform.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class ToggleButton extends Button {
	private final BooleanSupplier selected;

	public ToggleButton(int x, int y, int width, int height, Component message, OnPress onPress, BooleanSupplier selected, @Nullable Tooltip tooltip) {
		super(x, y, width, height, message, onPress, DEFAULT_NARRATION);

		this.selected = selected;

		if (tooltip != null) {
			setTooltip(tooltip);
		}
	}

	@Override
	protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();

		graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
		graphics.blitSprite(SPRITES.get(active, isSelected() || isHoveredOrFocused()), getX(), getY(), getWidth(), getHeight());
		graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

		renderString(graphics, minecraft.font, getFGColor() | Mth.ceil(alpha * 255.0F) << 24);
	}

	/**
	 * 选中且可用时才用高亮贴图
	 */
	private boolean isSelected() {
		return selected.getAsBoolean();
	}
}