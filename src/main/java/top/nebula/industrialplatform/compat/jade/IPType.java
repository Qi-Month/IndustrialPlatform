package top.nebula.industrialplatform.compat.jade;

import net.minecraft.resources.ResourceLocation;
import top.nebula.industrialplatform.IndustrialPlatform;

public class IPType {
	public static final ResourceLocation COMMON = IP("common");

	public static ResourceLocation IP(String path) {
		return IndustrialPlatform.loadResource(path);
	}
}