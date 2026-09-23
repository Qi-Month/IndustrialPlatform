package dev.celestiacraft.industrialplatform.client.preview;

import dev.celestiacraft.industrialplatform.client.hologram.HologramColors;
import net.minecraft.client.gui.GuiGraphics;

public enum PreviewPanelStyle {
	GUI {
		@Override
		public void drawFrame(GuiGraphics graphics, int left, int top, int right, int bottom, int viewLeft, int viewTop, int viewRight, int viewBottom) {
			graphics.fill(left, top, right, bottom, GUI_BORDER);
			graphics.fill(left + 1, top + 1, right - 1, bottom - 1, GUI_PANEL);
			graphics.fill(left + 1, top + 1, right - 2, top + 2, GUI_HIGHLIGHT);
			graphics.fill(left + 1, top + 1, left + 2, bottom - 2, GUI_HIGHLIGHT);
			graphics.fill(left + 2, bottom - 2, right - 1, bottom - 1, GUI_SHADOW);
			graphics.fill(right - 2, top + 2, right - 1, bottom - 1, GUI_SHADOW);

			graphics.fill(viewLeft - 1, viewTop - 1, viewRight, viewBottom, GUI_VIEW_SHADOW);
			graphics.fill(viewLeft, viewTop, viewRight + 1, viewBottom + 1, GUI_HIGHLIGHT);
			graphics.fill(viewLeft, viewTop, viewRight, viewBottom, GUI_VIEW);
		}

		@Override
		public int textColor() {
			return GUI_TEXT;
		}
	},
	HOLOGRAM {
		@Override
		public void drawFrame(GuiGraphics graphics, int left, int top, int right, int bottom, int viewLeft, int viewTop, int viewRight, int viewBottom) {
			graphics.fill(left, top, right, bottom, HOLOGRAM_PANEL);
			graphics.renderOutline(left, top, right - left, bottom - top, HOLOGRAM_EDGE);
			drawCorners(graphics, left, top, right, bottom);

			graphics.fill(viewLeft, viewTop, viewRight, viewBottom, HOLOGRAM_VIEW);
			graphics.renderOutline(viewLeft - 1, viewTop - 1, viewRight - viewLeft + 2, viewBottom - viewTop + 2, HOLOGRAM_EDGE);
		}

		@Override
		public int textColor() {
			return HOLOGRAM_TEXT;
		}
	};

	private static final int GUI_BORDER = 0xFF000000;
	private static final int GUI_PANEL = 0xFFC6C6C6;
	private static final int GUI_HIGHLIGHT = 0xFFFFFFFF;
	private static final int GUI_SHADOW = 0xFF555555;
	private static final int GUI_VIEW_SHADOW = 0xFF373737;
	private static final int GUI_VIEW = 0xFF262626;
	private static final int GUI_TEXT = 0xFF404040;

	private static final int HOLOGRAM_PANEL = HologramColors.withAlpha(HologramColors.SHADE, 0.72F);
	private static final int HOLOGRAM_VIEW = HologramColors.withAlpha(HologramColors.SHADE, 0.55F);
	private static final int HOLOGRAM_EDGE = HologramColors.withAlpha(HologramColors.ACCENT, 0.55F);
	private static final int HOLOGRAM_CORNER = HologramColors.withAlpha(HologramColors.GLOW, 0.95F);
	private static final int HOLOGRAM_TEXT = HologramColors.withAlpha(HologramColors.GLOW, 1.0F);
	private static final int CORNER_LENGTH = 8;
	private static final int CORNER_THICKNESS = 2;

	public abstract void drawFrame(GuiGraphics graphics, int left, int top, int right, int bottom, int viewLeft, int viewTop, int viewRight, int viewBottom);

	public abstract int textColor();

	private static void drawCorners(GuiGraphics graphics, int left, int top, int right, int bottom) {
		graphics.fill(left, top, left + CORNER_LENGTH, top + CORNER_THICKNESS, HOLOGRAM_CORNER);
		graphics.fill(left, top + CORNER_THICKNESS, left + CORNER_THICKNESS, top + CORNER_LENGTH, HOLOGRAM_CORNER);
		graphics.fill(right - CORNER_LENGTH, top, right, top + CORNER_THICKNESS, HOLOGRAM_CORNER);
		graphics.fill(right - CORNER_THICKNESS, top + CORNER_THICKNESS, right, top + CORNER_LENGTH, HOLOGRAM_CORNER);
		graphics.fill(left, bottom - CORNER_THICKNESS, left + CORNER_LENGTH, bottom, HOLOGRAM_CORNER);
		graphics.fill(left, bottom - CORNER_LENGTH, left + CORNER_THICKNESS, bottom - CORNER_THICKNESS, HOLOGRAM_CORNER);
		graphics.fill(right - CORNER_LENGTH, bottom - CORNER_THICKNESS, right, bottom, HOLOGRAM_CORNER);
		graphics.fill(right - CORNER_THICKNESS, bottom - CORNER_LENGTH, right, bottom - CORNER_THICKNESS, HOLOGRAM_CORNER);
	}
}