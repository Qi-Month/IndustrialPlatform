package top.nebula.industrialplatform.utils;

import net.minecraftforge.fml.ModList;

public interface CheckModLoaded {
	static boolean hasMod(String modid) {
		return ModList.get().isLoaded(modid);
	}

	static boolean hasCreate() {
		return hasMod("create");
	}

	static boolean hasJei() {
		return hasMod("jei");
	}

	static boolean hasJade() {
		return hasMod("jade");
	}
}