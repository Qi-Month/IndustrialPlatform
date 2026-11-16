package dev.celestiacraft.industrialplatform.client.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.celestiacraft.industrialplatform.client.hologram.HologramColors;
import dev.celestiacraft.industrialplatform.client.hologram.HologramRenderTypes;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.platform.blueprint.PlatformBlueprint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Matrix3x2f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class PlatformPreviewRenderer implements AutoCloseable {
	public static final Component TITLE = Component.translatable("gui.industrial_platform.preview");

	private static final int TITLE_Y = 6;
	private static final int VIEW_INSET = 7;
	private static final int VIEW_TOP = 17;
	private static final int VIEW_BOTTOM = 19;
	private static final int FOOTER_OFFSET = 13;
	private static final int FIT_PADDING = 6;
	private static final float PITCH = 35.0F;
	private static final Quaternionf PITCH_ROTATION = Axis.XP.rotationDegrees(PITCH);
	private static final float PITCH_SIN = Mth.sin(PITCH * Mth.DEG_TO_RAD);
	private static final float PITCH_COS = Mth.cos(PITCH * Mth.DEG_TO_RAD);
	private static final long ROTATION_PERIOD_MS = 12000L;
	private static final float DEGREES_PER_MS = -360.0F / ROTATION_PERIOD_MS;
	private static final float MAX_SCALE = 8.0F;
	private static final float CLEAR_Y_OFFSET = 0.01F;
	private static final int CLEAR_COLOR = HologramColors.withAlpha(HologramColors.ACCENT, 0.18F);
	private static final int OUTLINE_COLOR = HologramColors.withAlpha(HologramColors.ACCENT, 0.6F);

	private final PlatformPreviewModel model = new PlatformPreviewModel();
	private final int width;
	private final int height;
	private final PreviewPanelStyle style;
	private BlockPos platformPos;
	private PlatformMode mode;
	private int upFill;
	private int downFill;
	private int sizeX;
	private int sizeY;
	private int sizeZ;
	private float modelCenterY;
	private float scale;
	private boolean modelDirty;
	private int resourceGeneration = -1;
	private Component infoText = Component.empty();

	public PlatformPreviewRenderer(int width, int height, PreviewPanelStyle style) {
		this.width = width;
		this.height = height;
		this.style = style;
	}

	public void update(BlockPos platformPos, PlatformMode mode, int upFill, int downFill) {
		boolean samePos = platformPos.equals(this.platformPos);
		if (samePos && this.mode == mode && this.upFill == upFill && this.downFill == downFill) {
			return;
		}
		modelDirty |= !samePos || this.mode != mode || this.downFill != downFill;
		this.platformPos = platformPos.immutable();
		this.mode = mode;
		this.upFill = upFill;
		this.downFill = downFill;
		updateFit();
	}

	private void updateFit() {
		PlatformBlueprint structure = PreviewStructures.get(mode);
		sizeX = Math.max(1, structure.sizeX());
		sizeY = Math.max(1, structure.sizeY());
		sizeZ = Math.max(1, structure.sizeZ());
		infoText = Component.translatable("gui.industrial_platform.preview.info", sizeX, sizeZ, upFill, downFill);
		float radius = 0.5F * Mth.sqrt(sizeX * sizeX + sizeZ * sizeZ);
		int top = sizeY + upFill;
		int bottom = -downFill;
		modelCenterY = (top + bottom) * 0.5F;
		float projectedWidth = 2.0F * radius;
		float projectedHeight = 2.0F * radius * PITCH_SIN + (top - bottom) * PITCH_COS;
		float viewWidth = width - 2 * (VIEW_INSET + FIT_PADDING);
		float viewHeight = height - VIEW_TOP - VIEW_BOTTOM - 2 * FIT_PADDING;
		scale = Math.min(MAX_SCALE, Math.min(viewWidth / projectedWidth, viewHeight / projectedHeight));
	}

	public void render(GuiGraphicsExtractor graphics, int left, int top) {
		if (mode == null) {
			return;
		}
		if (resourceGeneration != PlatformPreviewModel.resourceGeneration()) {
			resourceGeneration = PlatformPreviewModel.resourceGeneration();
			modelDirty = true;
			updateFit();
		}
		int viewLeft = left + VIEW_INSET;
		int viewTop = top + VIEW_TOP;
		int viewRight = left + width - VIEW_INSET;
		int viewBottom = top + height - VIEW_BOTTOM;
		drawPanel(graphics, left, top, viewLeft, viewTop, viewRight, viewBottom);
		if (modelDirty) {
			model.rebuild(new PreviewBlockView(PreviewStructures.get(mode), downFill, platformPos));
			modelDirty = false;
		}
		graphics.enableScissor(viewLeft, viewTop, viewRight, viewBottom);
		ScreenRectangle scissor = graphics.peekScissorStack();
		if (scissor != null && scissor.width() > 0 && scissor.height() > 0) {
			graphics.submitPictureInPictureRenderState(new State(viewLeft, viewTop, viewRight, viewBottom, scale,
					new Matrix3x2f(graphics.pose()), scissor, model.snapshot(), sizeX, sizeY, sizeZ, upFill, modelCenterY,
					(Util.getMillis() % ROTATION_PERIOD_MS) * DEGREES_PER_MS));
		}
		graphics.disableScissor();
	}

	private void drawPanel(GuiGraphicsExtractor graphics, int left, int top, int viewLeft, int viewTop, int viewRight, int viewBottom) {
		int right = left + width;
		int bottom = top + height;
		style.drawFrame(graphics, left, top, right, bottom, viewLeft, viewTop, viewRight, viewBottom);
		Font font = Minecraft.getInstance().font;
		int textColor = style.textColor();
		graphics.text(font, TITLE, left + (width - font.width(TITLE)) / 2, top + TITLE_Y, textColor, false);
		graphics.text(font, infoText, left + (width - font.width(infoText)) / 2, bottom - FOOTER_OFFSET, textColor, false);
	}

	public record State(int x0, int y0, int x1, int y1, float scale, Matrix3x2f pose, ScreenRectangle scissorArea,
			List<PlatformPreviewModel.Layer> layers, int sizeX, int sizeY, int sizeZ, int upFill, float centerY, float yaw) implements PictureInPictureRenderState {
		@Override
		public ScreenRectangle bounds() {
			ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
			return scissorArea == null ? bounds : scissorArea.intersection(bounds);
		}
	}

	public static class PictureRenderer extends PictureInPictureRenderer<State> {
		private final Quaternionf yawRotation = new Quaternionf();
		private final Vector3f position = new Vector3f();
		private final Vector3f normal = new Vector3f();

		public PictureRenderer(MultiBufferSource.BufferSource buffers) {
			super(buffers);
		}

		@Override
		public Class<State> getRenderStateClass() {
			return State.class;
		}

		@Override
		protected float getTranslateY(int height, int guiScale) {
			return height / 2.0F;
		}

		@Override
		protected String getTextureLabel() {
			return "industrial_platform_preview";
		}

		@Override
		protected void renderToTexture(State state, PoseStack pose) {
			pose.scale(1.0F, -1.0F, 1.0F);
			pose.mulPose(PITCH_ROTATION);
			pose.mulPose(yawRotation.rotationY(state.yaw() * Mth.DEG_TO_RAD));
			pose.translate(-state.sizeX() * 0.5F, -state.centerY(), -state.sizeZ() * 0.5F);
			PlatformPreviewModel.draw(state.layers(), pose.last(), bufferSource, position, normal);
			drawClearBox(state, pose.last());
		}

		private void drawClearBox(State state, PoseStack.Pose pose) {
			if (state.upFill() <= 0) {
				return;
			}
			float minY = state.sizeY() + CLEAR_Y_OFFSET;
			float maxY = state.sizeY() + state.upFill();
			VertexConsumer faces = bufferSource.getBuffer(HologramRenderTypes.FACES);
			for (int corner = 0; corner < 4; corner++) {
				int next = (corner + 1) & 3;
				float x = corner == 1 || corner == 2 ? state.sizeX() : 0.0F;
				float z = corner >= 2 ? state.sizeZ() : 0.0F;
				float nextX = next == 1 || next == 2 ? state.sizeX() : 0.0F;
				float nextZ = next >= 2 ? state.sizeZ() : 0.0F;
				HologramRenderTypes.vertex(pose.pose(), faces, x, minY, z, CLEAR_COLOR);
				HologramRenderTypes.vertex(pose.pose(), faces, x, maxY, z, CLEAR_COLOR);
				HologramRenderTypes.vertex(pose.pose(), faces, nextX, maxY, nextZ, CLEAR_COLOR);
				HologramRenderTypes.vertex(pose.pose(), faces, nextX, minY, nextZ, CLEAR_COLOR);
			}
			HologramRenderTypes.vertex(pose.pose(), faces, 0.0F, maxY, 0.0F, CLEAR_COLOR);
			HologramRenderTypes.vertex(pose.pose(), faces, state.sizeX(), maxY, 0.0F, CLEAR_COLOR);
			HologramRenderTypes.vertex(pose.pose(), faces, state.sizeX(), maxY, state.sizeZ(), CLEAR_COLOR);
			HologramRenderTypes.vertex(pose.pose(), faces, 0.0F, maxY, state.sizeZ(), CLEAR_COLOR);
			bufferSource.endBatch(HologramRenderTypes.FACES);
			VertexConsumer lines = bufferSource.getBuffer(HologramRenderTypes.BOUNDARY_LINES);
			HologramRenderTypes.box(pose, lines, 0.0F, minY, 0.0F, state.sizeX(), maxY, state.sizeZ(), OUTLINE_COLOR, 1.0F);
			bufferSource.endBatch(HologramRenderTypes.BOUNDARY_LINES);
		}
	}

	@Override
	public void close() {
		model.close();
		mode = null;
		modelDirty = true;
	}
}
