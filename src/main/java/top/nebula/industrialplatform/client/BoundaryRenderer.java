package top.nebula.industrialplatform.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import top.nebula.industrialplatform.IndustrialPlatform;

import java.util.List;

@Mod.EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class BoundaryRenderer {
	// Cyan
	private static final float FLOAT_R = 0.0f;
	private static final float FLOAT_G = 1.0f;
	private static final float FLOAT_B = 1.0f;

	// Orange
	private static final float SOLID_R = 0.9f;
	private static final float SOLID_G = 0.35f;
	private static final float SOLID_B = 0.0f;

	private static final float BASE_ALPHA = 0.15f;
	private static final float WAVE_AMPLITUDE = 0.35f;
	private static final float STRIP_HEIGHT = 4.0f;
	private static final float WAVE_SPEED = 0.1f;
	private static final float WAVE_FREQUENCY = 0.15f;

	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
			return;
		}

		List<BoundaryRenderData.BoundaryEntry> entries = BoundaryRenderData.getEntries();
		if (entries.isEmpty()) {
			return;
		}

		Level level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}

		Vec3 camera = event.getCamera().getPosition();
		PoseStack poseStack = event.getPoseStack();
		float time = (level.getGameTime() + event.getPartialTick()) * WAVE_SPEED;

		poseStack.pushPose();
		poseStack.translate(-camera.x, -camera.y, -camera.z);
		Matrix4f matrix = poseStack.last().pose();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuilder();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		float bottom = level.getMinBuildHeight();
		float top = level.getMaxBuildHeight();

		for (BoundaryRenderData.BoundaryEntry entry : entries) {
			drawBoundary(builder, matrix, entry, bottom, top, time);
		}

		BufferUploader.drawWithShader(builder.end());

		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();

		poseStack.popPose();
	}

	private static void drawBoundary(BufferBuilder builder, Matrix4f matrix, BoundaryRenderData.BoundaryEntry entry, float bottom, float top, float time) {
		int posX = entry.pos().getX();
		int posZ = entry.pos().getZ();
		int finX = (int) Math.floor(posX / 16.0) * 16;
		int finZ = (int) Math.floor(posZ / 16.0) * 16;

		float r, g, b;
		if (entry.floating()) {
			r = FLOAT_R;
			g = FLOAT_G;
			b = FLOAT_B;
		} else {
			r = SOLID_R;
			g = SOLID_G;
			b = SOLID_B;
		}

		float minX, minZ, maxX, maxZ;
		if (entry.extended()) {
			minX = finX - 16;
			minZ = finZ - 16;
			maxX = finX + 32;
			maxZ = finZ + 32;
		} else {
			minX = finX;
			minZ = finZ;
			maxX = finX + 16;
			maxZ = finZ + 16;
		}

		for (float y = bottom; y < top; y += STRIP_HEIGHT) {
			float stripTop = Math.min(y + STRIP_HEIGHT, top);
			float midY = (y + stripTop) * 0.5f;

			float wave = (float) Math.sin((midY * WAVE_FREQUENCY) - time);
			float a = Math.max(0.0f, Math.min(1.0f, BASE_ALPHA + WAVE_AMPLITUDE * wave));

			// North
			builder.vertex(matrix, minX, y, minZ)
					.color(r, g, b, a)
					.endVertex();
			builder.vertex(matrix, minX, stripTop, minZ)
					.color(r, g, b, a)
					.endVertex();
			builder.vertex(matrix, maxX, stripTop, minZ)
					.color(r, g, b, a)
					.endVertex();
			builder.vertex(matrix, maxX, y, minZ)
					.color(r, g, b, a)
					.endVertex();

			// South
			builder.vertex(matrix, maxX, y, maxZ)
					.color(r, g, b, a)
					.endVertex();
			builder.vertex(matrix, maxX, stripTop, maxZ)
					.color(r, g, b, a)
					.endVertex();
			builder.vertex(matrix, minX, stripTop, maxZ)
					.color(r, g, b, a)
					.endVertex();
			builder.vertex(matrix, minX, y, maxZ)
					.color(r, g, b, a)
					.endVertex();

			// West
			builder.vertex(matrix, minX, y, maxZ)
					.color(r, g, b, a)
					.endVertex();
			builder.vertex(matrix, minX, stripTop, maxZ)
					.color(r, g, b, a)
					.endVertex();
			builder.vertex(matrix, minX, stripTop, minZ)
					.color(r, g, b, a)
					.endVertex();
			builder.vertex(matrix, minX, y, minZ)
					.color(r, g, b, a)
					.endVertex();

			// East
			builder.vertex(matrix, maxX, y, minZ)
					.color(r, g, b, a)
					.endVertex();
			builder.vertex(matrix, maxX, stripTop, minZ)
					.color(r, g, b, a)
					.endVertex();
			builder.vertex(matrix, maxX, stripTop, maxZ)
					.color(r, g, b, a)
					.endVertex();
			builder.vertex(matrix, maxX, y, maxZ)
					.color(r, g, b, a)
					.endVertex();
		}
	}
}