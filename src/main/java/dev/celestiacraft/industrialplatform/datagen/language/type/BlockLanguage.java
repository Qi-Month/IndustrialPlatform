package dev.celestiacraft.industrialplatform.datagen.language.type;

import dev.celestiacraft.industrialplatform.datagen.language.LanguageGenerate;

public class BlockLanguage extends LanguageGenerate {
	public static void addLang() {
		addBlockLanguage("industrial_platform", "Industrial Platform", "工业平台");
		addBlockLanguage("fluid_pool", "Fluid Pool", "流体池");
	}
}