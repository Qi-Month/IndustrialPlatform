package dev.celestiacraft.industrialplatform.client.screen.widget;

import dev.celestiacraft.industrialplatform.client.preview.PlatformPreviewRenderer;
import dev.celestiacraft.industrialplatform.client.preview.PreviewPanelStyle;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlatformPreviewWidget extends AbstractWidget implements AutoCloseable {
	private final PlatformPreviewRenderer renderer;
	private final BlockPos platformPos;

	public PlatformPreviewWidget(BlockPos platformPos, int x, int y, int width, int height) {
		super(x, y, width, height, PlatformPreviewRenderer.TITLE);
		this.platformPos = platformPos;
		renderer = new PlatformPreviewRenderer(width, height, PreviewPanelStyle.GUI);
	}

	public void update(PlatformMode mode, int upFill, int downFill) {
		renderer.update(platformPos, mode, upFill, downFill);
	}

	@Override
	protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderer.render(graphics, getX(), getY());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	@Override
	public @Nullable ComponentPath nextFocusPath(@NotNull FocusNavigationEvent event) {
		return null;
	}

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, getMessage());
	}

	@Override
	public void close() {
		renderer.close();
	}
}
