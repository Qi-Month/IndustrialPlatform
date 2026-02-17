package dev.celestiacraft.industrialplatform.compat.jade;

import net.minecraft.resources.ResourceLocation;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;

public class IPType {
	public static final ResourceLocation COMMON = addType("common");

	public static ResourceLocation addType(String path) {
		return IndustrialPlatform.loadResource(path);
	}
}