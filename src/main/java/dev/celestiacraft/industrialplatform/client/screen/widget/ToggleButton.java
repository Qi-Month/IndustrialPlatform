package dev.celestiacraft.industrialplatform.client.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class ToggleButton extends Button {
	private final BooleanSupplier selected;

	public ToggleButton(int x, int y, int width, int height, Component message, OnPress onPress, BooleanSupplier selected, @Nullable Tooltip tooltip) {
		super(x, y, width, height, message, onPress, DEFAULT_NARRATION);

		this.selected = selected;
		setOverrideRenderHighlightedSprite(() -> isSelected() || isHoveredOrFocused());

		if (tooltip != null) {
			setTooltip(tooltip);
		}
	}

	@Override
	protected void extractContents(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		extractDefaultSprite(graphics);
		extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
	}

	/**
	 * 选中且可用时才用高亮贴图
	 */
	private boolean isSelected() {
		return selected.getAsBoolean();
	}
}