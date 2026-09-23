package dev.celestiacraft.industrialplatform.client.hologram;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

public class HologramRenderTypes {
	private static final float LINE_WIDTH_SCALE = 2.0F;
	private static final float VANILLA_LINE_WIDTH = 2.5F;
	private static final float VANILLA_REFERENCE_WIDTH = 1920.0F;

	private static final RenderStateShard.LineStateShard BRACKET_LINE_WIDTH = new RenderStateShard.LineStateShard(OptionalDouble.empty()) {
		@Override
		public void setupRenderState() {
			float windowWidth = Minecraft.getInstance().getWindow().getWidth();
			float vanillaWidth = Math.max(VANILLA_LINE_WIDTH, windowWidth / VANILLA_REFERENCE_WIDTH * VANILLA_LINE_WIDTH);
			RenderSystem.lineWidth(vanillaWidth * LINE_WIDTH_SCALE);
		}
	};

	public static final RenderType BRACKET_LINES = RenderType.create(
			IndustrialPlatform.MODID + ":hologram_bracket_lines",
			DefaultVertexFormat.POSITION_COLOR_NORMAL,
			VertexFormat.Mode.LINES,
			RenderType.TRANSIENT_BUFFER_SIZE,
			false,
			false,
			RenderType.CompositeState.builder()
					.setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
					.setLineState(BRACKET_LINE_WIDTH)
					.setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
					.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
					.setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
					.setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
					.setCullState(RenderStateShard.NO_CULL)
					.createCompositeState(false)
	);
}