package dev.celestiacraft.industrialplatform.client.preview;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlatformPreviewModel implements AutoCloseable {
	private static final Map<RenderType, ByteBufferBuilder> BUFFERS = new HashMap<>();

	private record Layer(RenderType renderType, VertexBuffer buffer) {
	}

	private final List<Layer> layers = new ArrayList<>();

	public void rebuild(PreviewBlockView view) {
		close();

		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		RandomSource random = RandomSource.create();
		PoseStack poseStack = new PoseStack();
		Map<RenderType, BufferBuilder> used = new HashMap<>();

		try {
			bake(view, dispatcher, random, poseStack, used);
		} catch (RuntimeException exception) {
			BUFFERS.values().forEach(ByteBufferBuilder::close);
			BUFFERS.clear();
			throw exception;
		}

		BufferUploader.reset();

		for (RenderType renderType : RenderType.chunkBufferLayers()) {
			BufferBuilder builder = used.get(renderType);
			if (builder == null) {
				continue;
			}

			MeshData mesh = builder.build();
			if (mesh == null) {
				continue;
			}

			VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
			buffer.bind();
			buffer.upload(mesh);
			layers.add(new Layer(renderType, buffer));
		}

		VertexBuffer.unbind();
	}

	private static void bake(PreviewBlockView view, BlockRenderDispatcher dispatcher, RandomSource random, PoseStack poseStack, Map<RenderType, BufferBuilder> used) {
		view.forEachVisibleBlock((pos, state) -> {
			BakedModel model = dispatcher.getBlockModel(state);
			ModelData modelData = model.getModelData(view, pos, state, ModelData.EMPTY);

			poseStack.pushPose();
			poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

			for (RenderType renderType : model.getRenderTypes(state, random, modelData)) {
				dispatcher.renderBatched(state, pos, view, poseStack, beginLayer(renderType, used), true, random, modelData, renderType);
			}

			poseStack.popPose();
		});
	}

	private static BufferBuilder beginLayer(RenderType renderType, Map<RenderType, BufferBuilder> used) {
		return used.computeIfAbsent(renderType, type -> {
			ByteBufferBuilder bytes = BUFFERS.computeIfAbsent(type, ignored -> new ByteBufferBuilder(type.bufferSize()));
			return new BufferBuilder(bytes, type.mode(), type.format());
		});
	}

	public boolean isEmpty() {
		return layers.isEmpty();
	}

	public void draw(Matrix4f modelView, Matrix4f projection) {
		if (layers.isEmpty()) {
			return;
		}

		RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
		BufferUploader.reset();

		for (Layer layer : layers) {
			layer.renderType().setupRenderState();
			mainTarget.bindWrite(false);
			layer.buffer().bind();
			layer.buffer().drawWithShader(modelView, projection, RenderSystem.getShader());
			layer.renderType().clearRenderState();
		}

		VertexBuffer.unbind();
	}

	@Override
	public void close() {
		layers.forEach(layer -> layer.buffer().close());
		layers.clear();
	}
}
