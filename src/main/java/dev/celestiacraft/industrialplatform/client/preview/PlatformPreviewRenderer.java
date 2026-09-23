package dev.celestiacraft.industrialplatform.client.preview;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import dev.celestiacraft.industrialplatform.client.BoundaryRenderer;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.platform.blueprint.PlatformBlueprint;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

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
	private static final float MODEL_Z = 150.0F;
	private static final float MAX_SCALE = 8.0F;

	private static final float CLEAR_ALPHA = 0.18F;
	private static final float OUTLINE_ALPHA = 0.6F;
	private static final float CLEAR_Y_OFFSET = 0.01F;
	private static final int RING_CORNERS = 4;

	private final PlatformPreviewModel model = new PlatformPreviewModel();
	private final int width;
	private final int height;
	private final PreviewPanelStyle style;
	private final Matrix4f flip = new Matrix4f();
	private final Matrix4f modelView = new Matrix4f();
	private final Quaternionf yawRotation = new Quaternionf();
	private final float[] ringX = new float[RING_CORNERS];
	private final float[] ringZ = new float[RING_CORNERS];

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
		this.platformPos = platformPos;
		this.mode = mode;
		this.upFill = upFill;
		this.downFill = downFill;

		PlatformBlueprint structure = PreviewStructures.get(mode);
		sizeX = Math.max(1, structure.sizeX());
		sizeY = Math.max(1, structure.sizeY());
		sizeZ = Math.max(1, structure.sizeZ());
		infoText = Component.translatable("gui.industrial_platform.preview.info", sizeX, sizeZ, upFill, downFill);

		updateFit();
	}

	private void updateFit() {
		float radius = 0.5F * Mth.sqrt(sizeX * sizeX + sizeZ * sizeZ);
		int top = sizeY + upFill;
		int bottom = -downFill;

		modelCenterY = (top + bottom) * 0.5F;

		float projectedWidth = 2.0F * radius;
		float projectedHeight = 2.0F * radius * PITCH_SIN + (top - bottom) * PITCH_COS;
		float viewWidth = width - 2 * (VIEW_INSET + FIT_PADDING);
		float viewHeight = height - VIEW_TOP - VIEW_BOTTOM - 2 * FIT_PADDING;

		scale = Math.min(MAX_SCALE, Math.min(viewWidth / projectedWidth, viewHeight / projectedHeight));

		ringX[0] = 0.0F;
		ringZ[0] = 0.0F;
		ringX[1] = sizeX;
		ringZ[1] = 0.0F;
		ringX[2] = sizeX;
		ringZ[2] = sizeZ;
		ringX[3] = 0.0F;
		ringZ[3] = sizeZ;
	}

	public void render(GuiGraphics graphics, int left, int top) {
		if (mode == null) {
			return;
		}

		int viewLeft = left + VIEW_INSET;
		int viewTop = top + VIEW_TOP;
		int viewRight = left + width - VIEW_INSET;
		int viewBottom = top + height - VIEW_BOTTOM;

		drawPanel(graphics, left, top, viewLeft, viewTop, viewRight, viewBottom);

		if (modelDirty) {
			modelDirty = false;
			model.rebuild(new PreviewBlockView(PreviewStructures.get(mode), downFill, platformPos));
		}

		graphics.flush();
		graphics.enableScissor(viewLeft, viewTop, viewRight, viewBottom);
		drawScene(graphics.pose(), (viewLeft + viewRight) * 0.5F, (viewTop + viewBottom) * 0.5F);
		graphics.disableScissor();
	}

	private void drawPanel(GuiGraphics graphics, int left, int top, int viewLeft, int viewTop, int viewRight, int viewBottom) {
		int right = left + width;
		int bottom = top + height;

		style.drawFrame(graphics, left, top, right, bottom, viewLeft, viewTop, viewRight, viewBottom);

		Font font = Minecraft.getInstance().font;
		int textColor = style.textColor();
		graphics.drawString(font, TITLE, left + (width - font.width(TITLE)) / 2, top + TITLE_Y, textColor, false);
		graphics.drawString(font, infoText, left + (width - font.width(infoText)) / 2, bottom - FOOTER_OFFSET, textColor, false);
	}

	private void drawScene(PoseStack pose, float centerX, float centerY) {
		float yaw = (Util.getMillis() % ROTATION_PERIOD_MS) * DEGREES_PER_MS;

		pose.pushPose();
		pose.translate(centerX, centerY, MODEL_Z);
		pose.mulPose(flip.scaling(scale, -scale, scale));
		pose.mulPose(PITCH_ROTATION);
		pose.mulPose(yawRotation.rotationY(yaw * Mth.DEG_TO_RAD));
		pose.translate(-sizeX * 0.5F, -modelCenterY, -sizeZ * 0.5F);

		Matrix4f matrix = pose.last().pose();
		model.draw(modelView.set(RenderSystem.getModelViewMatrix()).mul(matrix), RenderSystem.getProjectionMatrix());
		drawClearBox(matrix);

		pose.popPose();
	}

	private void drawClearBox(Matrix4f matrix) {
		if (upFill <= 0) {
			return;
		}

		float minY = sizeY + CLEAR_Y_OFFSET;
		float maxY = sizeY + upFill;
		float red = BoundaryRenderer.PREVIEW_R;
		float green = BoundaryRenderer.PREVIEW_G;
		float blue = BoundaryRenderer.PREVIEW_B;

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		for (int corner = 0; corner < RING_CORNERS; corner++) {
			int next = (corner + 1) % RING_CORNERS;
			builder.addVertex(matrix, ringX[corner], minY, ringZ[corner]).setColor(red, green, blue, CLEAR_ALPHA);
			builder.addVertex(matrix, ringX[corner], maxY, ringZ[corner]).setColor(red, green, blue, CLEAR_ALPHA);
			builder.addVertex(matrix, ringX[next], maxY, ringZ[next]).setColor(red, green, blue, CLEAR_ALPHA);
			builder.addVertex(matrix, ringX[next], minY, ringZ[next]).setColor(red, green, blue, CLEAR_ALPHA);
		}
		for (int corner = 0; corner < RING_CORNERS; corner++) {
			builder.addVertex(matrix, ringX[corner], maxY, ringZ[corner]).setColor(red, green, blue, CLEAR_ALPHA);
		}
		BufferUploader.drawWithShader(builder.buildOrThrow());

		builder = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
		for (int corner = 0; corner < RING_CORNERS; corner++) {
			int next = (corner + 1) % RING_CORNERS;
			builder.addVertex(matrix, ringX[corner], minY, ringZ[corner]).setColor(red, green, blue, OUTLINE_ALPHA);
			builder.addVertex(matrix, ringX[next], minY, ringZ[next]).setColor(red, green, blue, OUTLINE_ALPHA);
			builder.addVertex(matrix, ringX[corner], maxY, ringZ[corner]).setColor(red, green, blue, OUTLINE_ALPHA);
			builder.addVertex(matrix, ringX[next], maxY, ringZ[next]).setColor(red, green, blue, OUTLINE_ALPHA);
			builder.addVertex(matrix, ringX[corner], minY, ringZ[corner]).setColor(red, green, blue, OUTLINE_ALPHA);
			builder.addVertex(matrix, ringX[corner], maxY, ringZ[corner]).setColor(red, green, blue, OUTLINE_ALPHA);
		}
		BufferUploader.drawWithShader(builder.buildOrThrow());

		RenderSystem.depthMask(true);
		RenderSystem.disableDepthTest();
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

	@Override
	public void close() {
		model.close();
		mode = null;
	}
}