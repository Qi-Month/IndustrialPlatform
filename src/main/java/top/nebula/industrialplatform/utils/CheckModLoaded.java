package top.nebula.industrialplatform.utils;

import net.minecraftforge.fml.ModList;

public interface CheckModLoaded {
	static boolean hasCreate() {
		return ModList.get().isLoaded("create");
	}
}