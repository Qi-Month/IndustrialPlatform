package dev.celestiacraft.industrialplatform.client.hologram;

import dev.celestiacraft.industrialplatform.client.BoundaryRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class HologramColors {
	private static final float MAX_CHANNEL = 255.0F;

	public static final int ACCENT = rgb(BoundaryRenderer.PREVIEW_R, BoundaryRenderer.PREVIEW_G, BoundaryRenderer.PREVIEW_B);
	public static final int GLOW = 0xCDE7FF;
	public static final int SHADE = 0x06101C;

	public static int withAlpha(int rgb, float alpha) {
		return ARGB.color(channel(alpha), ARGB.red(rgb), ARGB.green(rgb), ARGB.blue(rgb));
	}

	public static int rgb(float red, float green, float blue) {
		return ARGB.color(0, channel(red), channel(green), channel(blue));
	}

	private static int channel(float value) {
		return (int) (Mth.clamp(value, 0.0F, 1.0F) * MAX_CHANNEL);
	}
}