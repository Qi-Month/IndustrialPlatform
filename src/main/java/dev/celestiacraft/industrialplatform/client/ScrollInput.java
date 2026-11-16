package dev.celestiacraft.industrialplatform.client;

import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformProperties;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;

public class ScrollInput {
	private final ScrollWheelHandler wheel = new ScrollWheelHandler();
	private Area area;

	public enum Area {
		BLUEPRINTS,
		MATERIALS,
		UP_FILL,
		DOWN_FILL
	}

	public int scroll(Area area, double delta) {
		if (this.area != area) {
			reset();
			this.area = area;
		}
		return wheel.onMouseScroll(0.0D, delta).y;
	}

	public void reset() {
		area = null;
		wheel.onMouseScroll(0.0D, 0.0D);
	}

	public static int fillStep(int delta, int multiplier) {
		return Math.clamp((long) delta * multiplier, -PlatformProperties.MAX_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);
	}

	public static boolean isOverControls(EditBox field, Button minus, Button plus, double mouseX, double mouseY) {
		return field != null && minus != null && plus != null
				&& (field.isMouseOver(mouseX, mouseY) || minus.isMouseOver(mouseX, mouseY) || plus.isMouseOver(mouseX, mouseY));
	}
}
