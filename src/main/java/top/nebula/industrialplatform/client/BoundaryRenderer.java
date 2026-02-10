package top.nebula.industrialplatform.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import top.nebula.industrialplatform.IndustrialPlatform;

import java.util.List;

@EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT)
public class BoundaryRenderer {

	// Cyan
	private static final float FLOAT_R = 0.0f;
	private static final float FLOAT_G = 1.0f;
	private static final float FLOAT_B = 1.0f;

	// Deep orange
	private static final float SOLID_R = 0.9f;
	private static final float SOLID_G = 0.35f;
	private static final float SOLID_B = 0.0f;

	private static final float BASE_ALPHA = 0.5f;

	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

		List<BoundaryRenderData.BoundaryEntry> entries = BoundaryRenderData.getEntries();
		if (entries.isEmpty()) return;

		Level level = Minecraft.getInstance().level;
		if (level == null) return;

		Vec3 camera = event.getCamera().getPosition();
		PoseStack poseStack = event.getPoseStack();
		float bottom = level.getMinBuildHeight();
		float top = level.getMaxBuildHeight();

		poseStack.pushPose();
		poseStack.translate(-camera.x, -camera.y, -camera.z);
		Matrix4f matrix = poseStack.last().pose();

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.disableCull();
		RenderSystem.depthMask(true);
		RenderSystem.colorMask(false, false, false, false);

		BufferBuilder depthBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		for (BoundaryRenderData.BoundaryEntry entry : entries) {
			drawDepthQuads(depthBuilder, matrix, entry, bottom, top);
		}
		MeshData depthMesh = depthBuilder.buildOrThrow();
		BufferUploader.drawWithShader(depthMesh);

		RenderSystem.colorMask(true, true, true, true);

		if (IPShaders.getBoundaryShader() == null) {
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
		} else {
			RenderSystem.setShader(IPShaders::getBoundaryShader);
		}

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);
		RenderSystem.depthFunc(GL11.GL_LEQUAL);

		BufferBuilder colorBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		for (BoundaryRenderData.BoundaryEntry entry : entries) {
			drawBoundary(colorBuilder, matrix, entry, bottom, top);
		}
		MeshData colorMesh = colorBuilder.buildOrThrow();
		BufferUploader.drawWithShader(colorMesh);

		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();

		poseStack.popPose();
	}

	private static void drawDepthQuads(BufferBuilder builder, Matrix4f matrix,
										BoundaryRenderData.BoundaryEntry entry,
										float bottom, float top) {
		int posX = entry.pos().getX();
		int posZ = entry.pos().getZ();
		int finX = (int) Math.floor(posX / 16.0) * 16;
		int finZ = (int) Math.floor(posZ / 16.0) * 16;

		float minX, minZ, maxX, maxZ;
		if (entry.extended()) {
			minX = finX - 16; minZ = finZ - 16;
			maxX = finX + 32; maxZ = finZ + 32;
		} else {
			minX = finX; minZ = finZ;
			maxX = finX + 16; maxZ = finZ + 16;
		}

		// North
		builder.addVertex(matrix, minX, bottom, minZ).setColor(0, 0, 0, 255);
		builder.addVertex(matrix, minX, top, minZ).setColor(0, 0, 0, 255);
		builder.addVertex(matrix, maxX, top, minZ).setColor(0, 0, 0, 255);
		builder.addVertex(matrix, maxX, bottom, minZ).setColor(0, 0, 0, 255);

		// South
		builder.addVertex(matrix, maxX, bottom, maxZ).setColor(0, 0, 0, 255);
		builder.addVertex(matrix, maxX, top, maxZ).setColor(0, 0, 0, 255);
		builder.addVertex(matrix, minX, top, maxZ).setColor(0, 0, 0, 255);
		builder.addVertex(matrix, minX, bottom, maxZ).setColor(0, 0, 0, 255);

		// West
		builder.addVertex(matrix, minX, bottom, maxZ).setColor(0, 0, 0, 255);
		builder.addVertex(matrix, minX, top, maxZ).setColor(0, 0, 0, 255);
		builder.addVertex(matrix, minX, top, minZ).setColor(0, 0, 0, 255);
		builder.addVertex(matrix, minX, bottom, minZ).setColor(0, 0, 0, 255);

		// East
		builder.addVertex(matrix, maxX, bottom, minZ).setColor(0, 0, 0, 255);
		builder.addVertex(matrix, maxX, top, minZ).setColor(0, 0, 0, 255);
		builder.addVertex(matrix, maxX, top, maxZ).setColor(0, 0, 0, 255);
		builder.addVertex(matrix, maxX, bottom, maxZ).setColor(0, 0, 0, 255);
	}

	private static void drawBoundary(BufferBuilder builder, Matrix4f matrix,
									  BoundaryRenderData.BoundaryEntry entry,
									  float bottom, float top) {
		int posX = entry.pos().getX();
		int posZ = entry.pos().getZ();
		int finX = (int) Math.floor(posX / 16.0) * 16;
		int finZ = (int) Math.floor(posZ / 16.0) * 16;

		float r, g, b;
		if (entry.floating()) {
			r = FLOAT_R; g = FLOAT_G; b = FLOAT_B;
		} else {
			r = SOLID_R; g = SOLID_G; b = SOLID_B;
		}

		float minX, minZ, maxX, maxZ;
		if (entry.extended()) {
			minX = finX - 16; minZ = finZ - 16;
			maxX = finX + 32; maxZ = finZ + 32;
		} else {
			minX = finX; minZ = finZ;
			maxX = finX + 16; maxZ = finZ + 16;
		}

		float a = BASE_ALPHA;
		float sizeX = maxX - minX;
		float sizeZ = maxZ - minZ;

		float u0 = 0;
		float u1 = sizeX;
		float u2 = sizeX + sizeZ;
		float u3 = u2 + sizeX;
		float u4 = u3 + sizeZ;

		// North
		builder.addVertex(matrix, minX, bottom, minZ).setUv(u0, bottom).setColor(r, g, b, a);
		builder.addVertex(matrix, minX, top, minZ).setUv(u0, top).setColor(r, g, b, a);
		builder.addVertex(matrix, maxX, top, minZ).setUv(u1, top).setColor(r, g, b, a);
		builder.addVertex(matrix, maxX, bottom, minZ).setUv(u1, bottom).setColor(r, g, b, a);

		// East
		builder.addVertex(matrix, maxX, bottom, minZ).setUv(u1, bottom).setColor(r, g, b, a);
		builder.addVertex(matrix, maxX, top, minZ).setUv(u1, top).setColor(r, g, b, a);
		builder.addVertex(matrix, maxX, top, maxZ).setUv(u2, top).setColor(r, g, b, a);
		builder.addVertex(matrix, maxX, bottom, maxZ).setUv(u2, bottom).setColor(r, g, b, a);

		// South
		builder.addVertex(matrix, maxX, bottom, maxZ).setUv(u2, bottom).setColor(r, g, b, a);
		builder.addVertex(matrix, maxX, top, maxZ).setUv(u2, top).setColor(r, g, b, a);
		builder.addVertex(matrix, minX, top, maxZ).setUv(u3, top).setColor(r, g, b, a);
		builder.addVertex(matrix, minX, bottom, maxZ).setUv(u3, bottom).setColor(r, g, b, a);

		// West
		builder.addVertex(matrix, minX, bottom, maxZ).setUv(u3, bottom).setColor(r, g, b, a);
		builder.addVertex(matrix, minX, top, maxZ).setUv(u3, top).setColor(r, g, b, a);
		builder.addVertex(matrix, minX, top, minZ).setUv(u4, top).setColor(r, g, b, a);
		builder.addVertex(matrix, minX, bottom, minZ).setUv(u4, bottom).setColor(r, g, b, a);
	}
}
