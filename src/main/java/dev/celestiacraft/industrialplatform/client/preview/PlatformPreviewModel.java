package dev.celestiacraft.industrialplatform.client.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PlatformPreviewModel implements AutoCloseable {
	private static int resourceGeneration;

	public record Layer(RenderType renderType, List<Quad> quads) {
	}

	private record Quad(float[] vertices, int[] colors, int[] lights, int overlay) {
	}

	private List<Layer> layers = List.of();

	public static int resourceGeneration() {
		return resourceGeneration;
	}

	public static void invalidateResources() {
		resourceGeneration++;
		PreviewStructures.clear();
	}

	public void rebuild(PreviewBlockView view) {
		Minecraft minecraft = Minecraft.getInstance();
		BlockStateModelSet models = minecraft.getModelManager().getBlockStateModelSet();
		ModelBlockRenderer renderer = new ModelBlockRenderer(true, true, minecraft.getBlockColors());
		Map<ChunkSectionLayer, List<Quad>> used = new EnumMap<>(ChunkSectionLayer.class);
		view.forEachVisibleBlock((pos, state) -> renderer.tesselateBlock(
				(x, y, z, quad, instance) -> used.computeIfAbsent(quad.materialInfo().layer(), ignored -> new ArrayList<>()).add(copyQuad(x, y, z, quad, instance)),
				pos.getX(), pos.getY(), pos.getZ(), view, pos, state, models.get(state), state.getSeed(pos)));
		List<Layer> result = new ArrayList<>(used.size());
		for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
			List<Quad> quads = used.get(layer);
			if (quads != null) {
				RenderType renderType = switch (layer) {
					case SOLID -> RenderTypes.solidMovingBlock();
					case CUTOUT -> RenderTypes.cutoutMovingBlock();
					case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
				};
				result.add(new Layer(renderType, List.copyOf(quads)));
			}
		}
		layers = List.copyOf(result);
	}

	private static Quad copyQuad(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
		float[] vertices = new float[32];
		int[] colors = new int[4];
		int[] lights = new int[4];
		Vector3fc faceNormal = quad.direction().getUnitVec3f();
		for (int vertex = 0; vertex < 4; vertex++) {
			int offset = vertex * 8;
			Vector3fc position = quad.position(vertex);
			vertices[offset] = position.x() + x;
			vertices[offset + 1] = position.y() + y;
			vertices[offset + 2] = position.z() + z;
			vertices[offset + 3] = UVPair.unpackU(quad.packedUV(vertex));
			vertices[offset + 4] = UVPair.unpackV(quad.packedUV(vertex));
			int normal = quad.bakedNormals().normal(vertex);
			boolean specified = !BakedNormals.isUnspecified(normal);
			vertices[offset + 5] = specified ? BakedNormals.unpackX(normal) : faceNormal.x();
			vertices[offset + 6] = specified ? BakedNormals.unpackY(normal) : faceNormal.y();
			vertices[offset + 7] = specified ? BakedNormals.unpackZ(normal) : faceNormal.z();
			colors[vertex] = ARGB.multiply(instance.getColor(vertex), quad.bakedColors().color(vertex));
			lights[vertex] = instance.getLightCoordsWithEmission(vertex, quad.materialInfo().lightEmission());
		}
		return new Quad(vertices, colors, lights, instance.overlayCoords());
	}

	public List<Layer> snapshot() {
		return layers;
	}

	public boolean isEmpty() {
		return layers.isEmpty();
	}

	public static void submit(List<Layer> layers, PoseStack pose, SubmitNodeCollector collector, Vector3f position, Vector3f normal) {
		for (Layer layer : layers) {
			collector.submitCustomGeometry(pose, layer.renderType(), (renderPose, consumer) -> draw(layer.quads(), renderPose, consumer, position, normal));
		}
	}

	private static void draw(List<Quad> quads, PoseStack.Pose pose, VertexConsumer consumer, Vector3f position, Vector3f normal) {
		for (Quad quad : quads) {
			float[] vertices = quad.vertices();
			for (int vertex = 0; vertex < 4; vertex++) {
				int offset = vertex * 8;
				pose.pose().transformPosition(vertices[offset], vertices[offset + 1], vertices[offset + 2], position);
				pose.transformNormal(vertices[offset + 5], vertices[offset + 6], vertices[offset + 7], normal);
				consumer.addVertex(position.x, position.y, position.z, quad.colors()[vertex], vertices[offset + 3], vertices[offset + 4],
						quad.overlay(), quad.lights()[vertex], normal.x, normal.y, normal.z);
			}
		}
	}

	@Override
	public void close() {
		layers = List.of();
	}
}
