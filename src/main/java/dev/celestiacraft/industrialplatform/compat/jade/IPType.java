package dev.celestiacraft.industrialplatform.compat.jade;

import net.minecraft.resources.Identifier;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;

public class IPType {
	public static final Identifier COMMON = addType("common");

	public static Identifier addType(String path) {
		return IndustrialPlatform.loadResource(path);
	}
}