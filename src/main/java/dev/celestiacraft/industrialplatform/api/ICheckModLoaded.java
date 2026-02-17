package dev.celestiacraft.industrialplatform.api;

import net.minecraftforge.fml.ModList;

public interface ICheckModLoaded {
	static boolean hasMod(String modid) {
		return ModList.get().isLoaded(modid);
	}

	static boolean hasCreate() {
		return hasMod("create");
	}

	static boolean hasJade() {
		return hasMod("jade");
	}

	static boolean hasJei() {
		return hasMod("jei");
	}
}