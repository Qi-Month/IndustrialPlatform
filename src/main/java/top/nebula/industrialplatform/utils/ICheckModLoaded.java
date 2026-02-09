package top.nebula.industrialplatform.utils;

import net.minecraftforge.fml.ModList;

public interface ICheckModLoaded {
	static boolean hasMod(String modid) {
		return ModList.get().isLoaded(modid);
	}

	static boolean hasCreate() {
		return hasMod("create");
	}

	static boolean hasTCon() {
		return hasMod("tconstruct");
	}

	static boolean hasKubeJS() {
		return hasMod("kubejs");
	}

	static boolean hasJade() {
		return hasMod("jade");
	}

	static boolean hasJei() {
		return hasMod("jei");
	}
}