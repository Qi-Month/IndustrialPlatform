package dev.celestiacraft.industrialplatform.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class BoundaryRenderer {
	// 预览统一用蓝色
	private static final float PREVIEW_R = 0.4F;
	private static final float PREVIEW_G = 0.65F;
	private static final float PREVIEW_B = 1.0F;

	// 区块范围外墙: 满高度 + 波浪
	private static final float BASE_ALPHA = 0.15F;
	private static final float WAVE_AMPLITUDE = 0.35F;
	private static final float STRIP_HEIGHT = 4.0F;
	private static final float WAVE_SPEED = 0.3F;
	private static final float WAVE_FREQUENCY = 0.15F;

	// 填充范围线框: 原版碰撞箱那种线
	private static final float LINE_ALPHA = 0.75F;
	private static final float LINE_PULSE = 0.15F;
	private static final float PULSE_SPEED = 0.06F;

	// 平台自己所在的那一层: 比线框深, 并且抬高一点点免得和方块面打架
	private static final float DECK_ALPHA = 0.22F;
	private static final float DECK_DARKEN = 0.55F;
	private static final float DECK_Y_OFFSET = 0.01F;

	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
			return;
		}

		List<BoundaryRenderData.BoundaryEntry> entries = collectEntries();
		if (entries.isEmpty()) {
			return;
		}

		Level level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}

		Vec3 camera = event.getCamera().getPosition();
		PoseStack poseStack = event.getPoseStack();

		float waveTime = (level.getGameTime() + event.getPartialTick()) * WAVE_SPEED;
		float lineAlpha = LINE_ALPHA + LINE_PULSE * Mth.sin((level.getGameTime() + event.getPartialTick()) * PULSE_SPEED);

		poseStack.pushPose();
		poseStack.translate(-camera.x, -camera.y, -camera.z);

		// 1) 区块范围外墙 + 平台那一层
		drawFaces(poseStack, entries, level, waveTime);

		// 2) 填充范围的线框
		drawOutlines(poseStack, entries, lineAlpha);

		poseStack.popPose();
	}

	private static List<BoundaryRenderData.BoundaryEntry> collectEntries() {
		BoundaryRenderData.BoundaryEntry editing = BoundaryRenderData.getOverride();
		List<BoundaryRenderData.BoundaryEntry> entries = new ArrayList<>();

		for (BoundaryRenderData.BoundaryEntry entry : BoundaryRenderData.getEntries()) {
			// 界面里正在编辑的那个单独画, 免得重复
			if (editing != null && editing.pos().equals(entry.pos())) {
				continue;
			}
			entries.add(entry);
		}

		if (editing != null) {
			entries.add(editing);
		}

		return entries;
	}

	private static void drawFaces(PoseStack poseStack, List<BoundaryRenderData.BoundaryEntry> entries, Level level, float waveTime) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuilder();
		Matrix4f matrix = poseStack.last().pose();

		float bottom = level.getMinBuildHeight();
		float top = level.getMaxBuildHeight();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		for (BoundaryRenderData.BoundaryEntry entry : entries) {
			float[] color = previewColor();
			AABB box = boxOf(entry);

			drawWalls(builder, matrix, box, color, bottom, top, waveTime);
			drawDeck(builder, matrix, box, entry.pos().getY() + 1.0F + DECK_Y_OFFSET, color);
		}

		BufferUploader.drawWithShader(builder.end());

		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

	private static void drawWalls(BufferBuilder builder, Matrix4f matrix, AABB box, float[] color, float bottom, float top, float time) {
		float minX = (float) box.minX;
		float minZ = (float) box.minZ;
		float maxX = (float) box.maxX;
		float maxZ = (float) box.maxZ;

		for (float y = bottom; y < top; y += STRIP_HEIGHT) {
			float stripTop = Math.min(y + STRIP_HEIGHT, top);
			float midY = (y + stripTop) * 0.5F;

			float wave = Mth.sin((midY * WAVE_FREQUENCY) - time);
			float alpha = Mth.clamp(BASE_ALPHA + WAVE_AMPLITUDE * wave, 0.0F, 1.0F);

			// North
			builder.vertex(matrix, minX, y, minZ).color(color[0], color[1], color[2], alpha).endVertex();
			builder.vertex(matrix, minX, stripTop, minZ).color(color[0], color[1], color[2], alpha).endVertex();
			builder.vertex(matrix, maxX, stripTop, minZ).color(color[0], color[1], color[2], alpha).endVertex();
			builder.vertex(matrix, maxX, y, minZ).color(color[0], color[1], color[2], alpha).endVertex();

			// South
			builder.vertex(matrix, maxX, y, maxZ).color(color[0], color[1], color[2], alpha).endVertex();
			builder.vertex(matrix, maxX, stripTop, maxZ).color(color[0], color[1], color[2], alpha).endVertex();
			builder.vertex(matrix, minX, stripTop, maxZ).color(color[0], color[1], color[2], alpha).endVertex();
			builder.vertex(matrix, minX, y, maxZ).color(color[0], color[1], color[2], alpha).endVertex();

			// West
			builder.vertex(matrix, minX, y, maxZ).color(color[0], color[1], color[2], alpha).endVertex();
			builder.vertex(matrix, minX, stripTop, maxZ).color(color[0], color[1], color[2], alpha).endVertex();
			builder.vertex(matrix, minX, stripTop, minZ).color(color[0], color[1], color[2], alpha).endVertex();
			builder.vertex(matrix, minX, y, minZ).color(color[0], color[1], color[2], alpha).endVertex();

			// East
			builder.vertex(matrix, maxX, y, minZ).color(color[0], color[1], color[2], alpha).endVertex();
			builder.vertex(matrix, maxX, stripTop, minZ).color(color[0], color[1], color[2], alpha).endVertex();
			builder.vertex(matrix, maxX, stripTop, maxZ).color(color[0], color[1], color[2], alpha).endVertex();
			builder.vertex(matrix, maxX, y, maxZ).color(color[0], color[1], color[2], alpha).endVertex();
		}
	}

	private static void drawDeck(BufferBuilder builder, Matrix4f matrix, AABB box, float deckY, float[] color) {
		float red = color[0] * DECK_DARKEN;
		float green = color[1] * DECK_DARKEN;
		float blue = color[2] * DECK_DARKEN;

		builder.vertex(matrix, (float) box.minX, deckY, (float) box.minZ).color(red, green, blue, DECK_ALPHA).endVertex();
		builder.vertex(matrix, (float) box.minX, deckY, (float) box.maxZ).color(red, green, blue, DECK_ALPHA).endVertex();
		builder.vertex(matrix, (float) box.maxX, deckY, (float) box.maxZ).color(red, green, blue, DECK_ALPHA).endVertex();
		builder.vertex(matrix, (float) box.maxX, deckY, (float) box.minZ).color(red, green, blue, DECK_ALPHA).endVertex();
	}

	private static void drawOutlines(PoseStack poseStack, List<BoundaryRenderData.BoundaryEntry> entries, float alpha) {
		MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
		VertexConsumer consumer = buffers.getBuffer(RenderType.lines());

		for (BoundaryRenderData.BoundaryEntry entry : entries) {
			float[] color = previewColor();
			LevelRenderer.renderLineBox(poseStack, consumer, boxOf(entry), color[0], color[1], color[2], alpha);
		}

		buffers.endBatch(RenderType.lines());
	}

	private static AABB boxOf(BoundaryRenderData.BoundaryEntry entry) {
		int chunkSize = 16;
		int chunkX = Math.floorDiv(entry.pos().getX(), chunkSize) * chunkSize;
		int chunkZ = Math.floorDiv(entry.pos().getZ(), chunkSize) * chunkSize;

		// 控制器所在区块尽量落在平台中间, 和摆放规则保持一致
		double originX = chunkX - ((entry.chunksX() - 1) / 2) * (double) chunkSize;
		double originZ = chunkZ - ((entry.chunksZ() - 1) / 2) * (double) chunkSize;

		double minX = originX;
		double minZ = originZ;
		double maxX = originX + Math.max(1, entry.sizeX());
		double maxZ = originZ + Math.max(1, entry.sizeZ());

		int posY = entry.pos().getY();
		double minY = posY - Math.max(0, entry.downFill());
		double maxY = posY + Math.max(0, entry.upFill()) + 1;

		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	private static float[] previewColor() {
		return new float[]{PREVIEW_R, PREVIEW_G, PREVIEW_B};
	}
}