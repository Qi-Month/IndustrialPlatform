package dev.celestiacraft.industrialplatform.client.hologram;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.client.preview.PlatformPreviewRenderer;
import dev.celestiacraft.industrialplatform.client.preview.PreviewPanelStyle;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class HologramOverlay {
	public static final String ID = "platform_hologram";

	private static final int PANEL_WIDTH = 120;
	private static final int PANEL_HEIGHT = 150;
	private static final int LEADER_RISE = 24;
	private static final int LEADER_RUN = 36;
	private static final int SCREEN_MARGIN = 6;
	private static final int JOIN_INSET = 12;

	private static final float LEADER_PHASE = 0.4F;
	private static final float CORE_WIDTH = 1.0F;
	private static final float GLOW_WIDTH = 3.0F;
	private static final float MIN_MITER_SQUARED = 1.0F;
	private static final float ANCHOR_RADIUS = 2.0F;
	private static final float ANCHOR_GLOW_RADIUS = 4.0F;
	private static final float NODE_RADIUS = 1.5F;

	private static final long SWEEP_PERIOD_MS = 2400L;
	private static final int SWEEP_HEIGHT = 14;

	private static final int CORE_COLOR = HologramColors.withAlpha(HologramColors.GLOW, 0.95F);
	private static final int GLOW_COLOR = HologramColors.withAlpha(HologramColors.ACCENT, 0.35F);
	private static final int SWEEP_COLOR = HologramColors.withAlpha(HologramColors.ACCENT, 0.18F);
	private static final int SWEEP_FADE = HologramColors.withAlpha(HologramColors.ACCENT, 0.0F);

	private static final PlatformPreviewRenderer PREVIEW = new PlatformPreviewRenderer(PANEL_WIDTH, PANEL_HEIGHT, PreviewPanelStyle.HOLOGRAM);

	private static boolean panelOnRight = true;

	public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
		BlockPos pos = HologramTarget.getPos();
		if (pos == null || Minecraft.getInstance().screen != null || !HologramWorldRenderer.isAnchorVisible()) {
			return;
		}

		float progress = HologramTarget.getProgress(partialTick);
		if (progress <= 0.0F) {
			return;
		}

		float anchorX = HologramWorldRenderer.getAnchorX(screenWidth);
		float anchorY = HologramWorldRenderer.getAnchorY(screenHeight);
		int reach = LEADER_RISE + LEADER_RUN;
		int footprint = reach + PANEL_WIDTH + SCREEN_MARGIN;
		boolean fitsRight = anchorX + footprint <= screenWidth;
		boolean fitsLeft = anchorX - footprint >= 0.0F;
		if (panelOnRight ? !fitsRight && fitsLeft : !fitsLeft && fitsRight) {
			panelOnRight = !panelOnRight;
		}

		int panelLeft = Mth.clamp(
				panelOnRight ? Mth.floor(anchorX) + reach : Mth.floor(anchorX) - reach - PANEL_WIDTH,
				SCREEN_MARGIN,
				screenWidth - SCREEN_MARGIN - PANEL_WIDTH
		);
		int panelTop = Mth.clamp(
				Mth.floor(anchorY) - LEADER_RISE - PANEL_HEIGHT / 2,
				SCREEN_MARGIN,
				screenHeight - SCREEN_MARGIN - PANEL_HEIGHT
		);
		float lineY = Mth.clamp(anchorY - LEADER_RISE, panelTop + JOIN_INSET, panelTop + PANEL_HEIGHT - JOIN_INSET);
		float joinX = panelOnRight ? panelLeft : panelLeft + PANEL_WIDTH;
		float rise = Math.abs(anchorY - lineY);
		float elbowX = panelOnRight ? Math.min(anchorX + rise, joinX) : Math.max(anchorX - rise, joinX);

		graphics.flush();
		clearDepth();

		drawLeader(graphics, anchorX, anchorY, elbowX, lineY, joinX, Mth.clamp(progress / LEADER_PHASE, 0.0F, 1.0F));

		float panelProgress = (progress - LEADER_PHASE) / (1.0F - LEADER_PHASE);
		if (panelProgress > 0.0F) {
			PlatformSettings settings = HologramTarget.getSettings();
			PREVIEW.update(pos, settings.mode(), settings.upFill(), settings.downFill());
			drawPanel(graphics, panelLeft, panelTop, lineY, panelProgress);
		}

		graphics.flush();
		clearDepth();
	}

	private static void drawLeader(GuiGraphics graphics, float anchorX, float anchorY, float elbowX, float lineY, float joinX, float progress) {
		VertexConsumer consumer = graphics.bufferSource().getBuffer(RenderType.guiOverlay());
		Matrix4f matrix = graphics.pose().last().pose();

		float diagonal = Mth.sqrt(Mth.square(elbowX - anchorX) + Mth.square(lineY - anchorY));
		float run = Math.abs(joinX - elbowX);
		float drawn = (diagonal + run) * progress;

		if (drawn <= diagonal) {
			float diagonalShown = diagonal > 0.0F ? drawn / diagonal : 1.0F;
			drawGlowLine(consumer, matrix, anchorX, anchorY, Mth.lerp(diagonalShown, anchorX, elbowX), Mth.lerp(diagonalShown, anchorY, lineY));
		} else {
			float runShown = Math.min(1.0F, (drawn - diagonal) / run);
			drawGlowPath(consumer, matrix, anchorX, anchorY, elbowX, lineY, Mth.lerp(runShown, elbowX, joinX), lineY);
		}

		drawDiamond(consumer, matrix, anchorX, anchorY, ANCHOR_GLOW_RADIUS, GLOW_COLOR);
		drawDiamond(consumer, matrix, anchorX, anchorY, ANCHOR_RADIUS, CORE_COLOR);

		if (progress >= 1.0F) {
			drawDiamond(consumer, matrix, joinX, lineY, NODE_RADIUS, CORE_COLOR);
		}
	}

	private static void drawPanel(GuiGraphics graphics, int left, int top, float lineY, float progress) {
		int right = left + PANEL_WIDTH;
		int bottom = top + PANEL_HEIGHT;
		int clipTop = Mth.floor(Mth.lerp(progress, lineY, top));
		int clipBottom = Mth.ceil(Mth.lerp(progress, lineY, bottom));

		graphics.flush();
		graphics.enableScissor(left, clipTop, right, clipBottom);
		PREVIEW.render(graphics, left, top);
		drawSweep(graphics, left, top, right);
		graphics.flush();
		graphics.disableScissor();

		if (progress < 1.0F) {
			graphics.fill(RenderType.guiOverlay(), left, clipTop, right, clipTop + 1, CORE_COLOR);
			graphics.fill(RenderType.guiOverlay(), left, clipBottom - 1, right, clipBottom, CORE_COLOR);
		}
	}

	private static void drawSweep(GuiGraphics graphics, int left, int top, int right) {
		float phase = (Util.getMillis() % SWEEP_PERIOD_MS) / (float) SWEEP_PERIOD_MS;
		int sweepBottom = top + Mth.floor(phase * (PANEL_HEIGHT + SWEEP_HEIGHT));
		graphics.fillGradient(RenderType.guiOverlay(), left, sweepBottom - SWEEP_HEIGHT, right, sweepBottom, SWEEP_FADE, SWEEP_COLOR, 0);
	}

	private static void drawGlowLine(VertexConsumer consumer, Matrix4f matrix, float fromX, float fromY, float toX, float toY) {
		drawLine(consumer, matrix, fromX, fromY, toX, toY, GLOW_WIDTH, GLOW_COLOR);
		drawLine(consumer, matrix, fromX, fromY, toX, toY, CORE_WIDTH, CORE_COLOR);
	}

	private static void drawGlowPath(VertexConsumer consumer, Matrix4f matrix, float startX, float startY, float elbowX, float elbowY, float endX, float endY) {
		drawPath(consumer, matrix, startX, startY, elbowX, elbowY, endX, endY, GLOW_WIDTH, GLOW_COLOR);
		drawPath(consumer, matrix, startX, startY, elbowX, elbowY, endX, endY, CORE_WIDTH, CORE_COLOR);
	}

	private static void drawPath(VertexConsumer consumer, Matrix4f matrix, float startX, float startY, float elbowX, float elbowY, float endX, float endY, float width, int color) {
		float firstLength = Mth.sqrt(Mth.square(elbowX - startX) + Mth.square(elbowY - startY));
		float secondLength = Mth.sqrt(Mth.square(endX - elbowX) + Mth.square(endY - elbowY));
		if (firstLength <= 0.0F || secondLength <= 0.0F) {
			drawLine(consumer, matrix, startX, startY, elbowX, elbowY, width, color);
			drawLine(consumer, matrix, elbowX, elbowY, endX, endY, width, color);
			return;
		}

		float halfWidth = width * 0.5F;
		float firstNormalX = (startY - elbowY) / firstLength;
		float firstNormalY = (elbowX - startX) / firstLength;
		float secondNormalX = (elbowY - endY) / secondLength;
		float secondNormalY = (endX - elbowX) / secondLength;

		float miterX = firstNormalX + secondNormalX;
		float miterY = firstNormalY + secondNormalY;
		float miterScale = width / Math.max(MIN_MITER_SQUARED, miterX * miterX + miterY * miterY);
		float jointX = miterX * miterScale;
		float jointY = miterY * miterScale;

		drawQuad(consumer, matrix,
				startX + firstNormalX * halfWidth, startY + firstNormalY * halfWidth,
				elbowX + jointX, elbowY + jointY,
				elbowX - jointX, elbowY - jointY,
				startX - firstNormalX * halfWidth, startY - firstNormalY * halfWidth,
				color);
		drawQuad(consumer, matrix,
				elbowX + jointX, elbowY + jointY,
				endX + secondNormalX * halfWidth, endY + secondNormalY * halfWidth,
				endX - secondNormalX * halfWidth, endY - secondNormalY * halfWidth,
				elbowX - jointX, elbowY - jointY,
				color);
	}

	private static void drawLine(VertexConsumer consumer, Matrix4f matrix, float fromX, float fromY, float toX, float toY, float width, int color) {
		float dx = toX - fromX;
		float dy = toY - fromY;
		float length = Mth.sqrt(dx * dx + dy * dy);
		if (length <= 0.0F) {
			return;
		}

		float offsetX = -dy / length * width * 0.5F;
		float offsetY = dx / length * width * 0.5F;
		drawQuad(consumer, matrix,
				fromX + offsetX, fromY + offsetY,
				toX + offsetX, toY + offsetY,
				toX - offsetX, toY - offsetY,
				fromX - offsetX, fromY - offsetY,
				color);
	}

	private static void drawDiamond(VertexConsumer consumer, Matrix4f matrix, float x, float y, float radius, int color) {
		drawQuad(consumer, matrix, x, y - radius, x - radius, y, x, y + radius, x + radius, y, color);
	}

	private static void drawQuad(VertexConsumer consumer, Matrix4f matrix, float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
		consumer.vertex(matrix, x0, y0, 0.0F).color(color).endVertex();
		consumer.vertex(matrix, x1, y1, 0.0F).color(color).endVertex();
		consumer.vertex(matrix, x2, y2, 0.0F).color(color).endVertex();
		consumer.vertex(matrix, x3, y3, 0.0F).color(color).endVertex();
	}

	private static void clearDepth() {
		RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
	}

	@SubscribeEvent
	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		PREVIEW.close();
	}
}
