package dev.celestiacraft.industrialplatform.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
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
			this.setTooltip(tooltip);
		}
	}

	@Override
	protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();

		graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		graphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.textureY());
		graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

		this.renderString(graphics, minecraft.font, this.getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
	}

	private int textureY() {
		int index = 1;

		if (!this.active) {
			index = 0;
		} else if (this.selected.getAsBoolean() || this.isHoveredOrFocused()) {
			index = 2;
		}

		return TEXTURE_Y_OFFSET + index * TEXTURE_HEIGHT;
	}
}