package dev.celestiacraft.industrialplatform.client.hologram;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public class HologramRenderTypes {
	private static final Identifier LINE_SHADER = IndustrialPlatform.loadResource("core/hologram_lines");
	private static final VertexFormat LINE_FORMAT = VertexFormat.builder(0)
			.addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, GpuFormat.RGB32_FLOAT)
			.addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, GpuFormat.RGBA8_UNORM)
			.addAttribute(DefaultVertexFormat.UV0_SEMANTIC_NAME, GpuFormat.RG32_FLOAT)
			.addAttribute(DefaultVertexFormat.UV3_SEMANTIC_NAME, GpuFormat.RG32_FLOAT)
			.addAttribute(DefaultVertexFormat.LINE_WIDTH_SEMANTIC_NAME, GpuFormat.R32_FLOAT)
			.build();
	private static final DepthStencilState LINE_DEPTH = lineDepth(false);
	private static final DepthStencilState BRACKET_DEPTH = lineDepth(true);

	public static final RenderPipeline FACES_PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(IndustrialPlatform.loadResource("pipeline/hologram_faces"))
			.build();
	public static final RenderPipeline BOUNDARY_PIPELINE = linePipeline("boundary_lines", RenderPipelines.LINES_TRANSLUCENT_NO_DEPTH_WRITE, LINE_DEPTH);
	public static final RenderPipeline BRACKET_PIPELINE = linePipeline("hologram_bracket_lines", RenderPipelines.LINES_TRANSLUCENT, BRACKET_DEPTH);
	public static final OitPipelineSet LINES_OIT_PIPELINES = OitPipelineSet.builder(IndustrialPlatform.loadResource("hologram_lines"),
			lineBuilder(RenderPipeline.builder(RenderPipelines.OIT_LINES_SNIPPET)))
			.withDepthBoundsModifier(HologramRenderTypes::applyLineDepthBias)
			.withTransmittanceModifier(HologramRenderTypes::applyLineDepthBias)
			.withAccumulateModifier(HologramRenderTypes::applyLineDepthBias)
			.build();
	public static final RenderType FACES = RenderType.create(IndustrialPlatform.MODID + ":hologram_faces",
			RenderSetup.builder(FACES_PIPELINE).setOitPipelines(RenderPipelines.OIT_DEBUG_FILLED_BOX).sortOnUpload().createRenderSetup());
	public static final RenderType BOUNDARY_LINES = RenderType.create(IndustrialPlatform.MODID + ":boundary_lines",
			RenderSetup.builder(BOUNDARY_PIPELINE).setOitPipelines(LINES_OIT_PIPELINES).createRenderSetup());
	public static final RenderType BRACKET_LINES = RenderType.create(IndustrialPlatform.MODID + ":hologram_bracket_lines",
			RenderSetup.builder(BRACKET_PIPELINE).createRenderSetup());

	private static RenderPipeline linePipeline(String name, RenderPipeline original, DepthStencilState depth) {
		return lineBuilder(original.toBuilder())
				.withLocation(IndustrialPlatform.loadResource("pipeline/" + name))
				.withDepthStencilState(depth)
				.build();
	}

	private static RenderPipeline.Builder lineBuilder(RenderPipeline.Builder builder) {
		return builder.withVertexShader(LINE_SHADER)
				.withVertexBinding(0, LINE_FORMAT)
				.withPrimitiveTopology(PrimitiveTopology.QUADS);
	}

	private static DepthStencilState lineDepth(boolean writeDepth) {
		DepthStencilState bias = RenderPipelines.LINES_DEPTH_BIAS.getDepthStencilState();
		return new DepthStencilState(bias.depthTest(), writeDepth, 0.0F, bias.depthBiasConstant(), bias.stencilTest());
	}

	private static void applyLineDepthBias(RenderPipeline.Builder builder) {
		builder.withDepthStencilState(LINE_DEPTH);
	}

	public static float lineWidth() {
		return Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
	}

	public static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, int color) {
		consumer.addVertex(transformX(matrix, x, y, z), transformY(matrix, x, y, z), transformZ(matrix, x, y, z)).setColor(color);
	}

	public static void line(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, float endX, float endY, float endZ, int color, float width) {
		Matrix4f matrix = pose.pose();
		float startX = transformX(matrix, x, y, z);
		float startY = transformY(matrix, x, y, z);
		float startZ = transformZ(matrix, x, y, z);
		float transformedEndX = transformX(matrix, endX, endY, endZ);
		float transformedEndY = transformY(matrix, endX, endY, endZ);
		float transformedEndZ = transformZ(matrix, endX, endY, endZ);
		if (startX == transformedEndX && startY == transformedEndY && startZ == transformedEndZ) {
			return;
		}
		lineVertex(consumer, startX, startY, startZ, transformedEndX, transformedEndY, transformedEndZ, color, 0, width);
		lineVertex(consumer, startX, startY, startZ, transformedEndX, transformedEndY, transformedEndZ, color, 1, width);
		lineVertex(consumer, transformedEndX, transformedEndY, transformedEndZ, startX, startY, startZ, color, 2, width);
		lineVertex(consumer, transformedEndX, transformedEndY, transformedEndZ, startX, startY, startZ, color, 3, width);
	}

	private static void lineVertex(VertexConsumer consumer, float x, float y, float z, float endX, float endY, float endZ, int color, int corner, float width) {
		consumer.addVertex(x, y, z).setColor(color).setUv(endX, endY).setUv3(endZ, corner).setLineWidth(width);
	}

	private static float transformX(Matrix4f matrix, float x, float y, float z) {
		return matrix.m00() * x + matrix.m10() * y + matrix.m20() * z + matrix.m30();
	}

	private static float transformY(Matrix4f matrix, float x, float y, float z) {
		return matrix.m01() * x + matrix.m11() * y + matrix.m21() * z + matrix.m31();
	}

	private static float transformZ(Matrix4f matrix, float x, float y, float z) {
		return matrix.m02() * x + matrix.m12() * y + matrix.m22() * z + matrix.m32();
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
