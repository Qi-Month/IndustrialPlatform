package dev.celestiacraft.industrialplatform.client.hologram;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class HologramRenderTypes {
	public static final RenderPipeline FACES_PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(IndustrialPlatform.loadResource("pipeline/hologram_faces"))
			.withCull(false)
			.build();
	public static final RenderType FACES = RenderType.create(IndustrialPlatform.MODID + ":hologram_faces",
			RenderSetup.builder(FACES_PIPELINE).sortOnUpload().createRenderSetup());
	public static final RenderType BOUNDARY_LINES = RenderType.create(IndustrialPlatform.MODID + ":boundary_lines",
			RenderSetup.builder(RenderPipelines.LINES_TRANSLUCENT)
					.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
					.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
					.createRenderSetup());
	public static final RenderType BRACKET_LINES = RenderType.create(IndustrialPlatform.MODID + ":hologram_bracket_lines",
			RenderSetup.builder(RenderPipelines.LINES)
					.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
					.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
					.createRenderSetup());

	public static float lineWidth() {
		return Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
	}

	public static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, int color) {
		consumer.addVertex(matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30(),
				matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31(),
				matrix.m02() * x + matrix.m12() * y + matrix.m22() * z + matrix.m32()).setColor(color);
	}

	public static void line(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, float endX, float endY, float endZ, int color, float width) {
		float dx = endX - x;
		float dy = endY - y;
		float dz = endZ - z;
		Matrix3f matrix = pose.normal();
		float nx = matrix.m00() * dx + matrix.m10() * dy + matrix.m20() * dz;
		float ny = matrix.m01() * dx + matrix.m11() * dy + matrix.m21() * dz;
		float nz = matrix.m02() * dx + matrix.m12() * dy + matrix.m22() * dz;
		float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
		if (length <= 0.0F) {
			return;
		}
		nx /= length;
		ny /= length;
		nz /= length;
		vertex(pose.pose(), consumer, x, y, z, color);
		consumer.setNormal(nx, ny, nz).setLineWidth(width);
		vertex(pose.pose(), consumer, endX, endY, endZ, color);
		consumer.setNormal(nx, ny, nz).setLineWidth(width);
	}

	public static void box(PoseStack.Pose pose, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color, float width) {
		for (int corner = 0; corner < 8; corner++) {
			float x = (corner & 1) == 0 ? minX : maxX;
			float y = (corner & 2) == 0 ? minY : maxY;
			float z = (corner & 4) == 0 ? minZ : maxZ;
			if ((corner & 1) == 0) {
				line(pose, consumer, x, y, z, maxX, y, z, color, width);
			}
			if ((corner & 2) == 0) {
				line(pose, consumer, x, y, z, x, maxY, z, color, width);
			}
			if ((corner & 4) == 0) {
				line(pose, consumer, x, y, z, x, y, maxZ, color, width);
			}
		}
	}
}
