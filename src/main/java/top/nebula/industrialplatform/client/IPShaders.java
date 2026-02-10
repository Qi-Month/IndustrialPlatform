package top.nebula.industrialplatform.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import top.nebula.industrialplatform.IndustrialPlatform;

import java.io.IOException;

@EventBusSubscriber(modid = IndustrialPlatform.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class IPShaders {

	private static ShaderInstance boundaryShader;

	public static ShaderInstance getBoundaryShader() {
		return boundaryShader;
	}

	@SubscribeEvent
	public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
		event.registerShader(
				new ShaderInstance(
						event.getResourceProvider(),
						ResourceLocation.fromNamespaceAndPath(IndustrialPlatform.MODID, "boundary"),
						DefaultVertexFormat.POSITION_TEX_COLOR
				),
				shader -> boundaryShader = shader
		);
		IndustrialPlatform.LOGGER.info("Boundary shader registered");
	}
}
