package dev.celestiacraft.industrialplatform.client.preview;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预览平台的显存几何体: 用原版方块渲染管线烘焙一次, 之后每帧直接画缓存, 设置变化时才重建
 */
public class PlatformPreviewModel implements AutoCloseable {
	/**
	 * 每种渲染层共用一个顶点构建器, 它申请的是堆外内存, 不能每次重建都新建; 原版构造器会把容量乘 6 当作字节数
	 */
	private static final Map<RenderType, BufferBuilder> BUILDERS = new HashMap<>();
	private static final int BUILDER_CAPACITY = 524288;

	private record Layer(RenderType renderType, VertexBuffer buffer) {
	}

	private final List<Layer> layers = new ArrayList<>();

	public void rebuild(PreviewBlockView view) {
		close();

		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		RandomSource random = RandomSource.create();
		PoseStack poseStack = new PoseStack();
		Map<RenderType, BufferBuilder> used = new HashMap<>();

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

		BufferUploader.reset();

		for (RenderType renderType : RenderType.chunkBufferLayers()) {
			BufferBuilder builder = used.get(renderType);
			if (builder == null) {
				continue;
			}

			BufferBuilder.RenderedBuffer rendered = builder.endOrDiscardIfEmpty();
			if (rendered == null) {
				continue;
			}

			VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
			buffer.bind();
			buffer.upload(rendered);
			layers.add(new Layer(renderType, buffer));
		}

		VertexBuffer.unbind();
	}

	private static BufferBuilder beginLayer(RenderType renderType, Map<RenderType, BufferBuilder> used) {
		return used.computeIfAbsent(renderType, type -> {
			BufferBuilder builder = BUILDERS.computeIfAbsent(type, ignored -> new BufferBuilder(BUILDER_CAPACITY));
			discardStale(builder);
			builder.begin(type.mode(), type.format());
			return builder;
		});
	}

	/**
	 * 上一次烘焙中途出错时构建器会停在构建状态, 先收尾才能重新开始
	 */
	private static void discardStale(BufferBuilder builder) {
		if (!builder.building()) {
			return;
		}

		BufferBuilder.RenderedBuffer stale = builder.endOrDiscardIfEmpty();
		if (stale != null) {
			stale.release();
		}
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
			// 极佳画质下半透明层会被切到单独的缓冲, 界面里看不见, 这里强制画回主缓冲
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
