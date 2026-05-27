package dev.celestiacraft.industrialplatform.datagen.language.locale;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.datagen.language.LanguageGenerate;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.List;

public class English extends LanguageProvider {
	public English(PackOutput output) {
		super(output, IndustrialPlatform.MODID, "en_us");
	}

	@Override
	protected void addTranslations() {
		for (List<String> item : LanguageGenerate.TRANSLATION_LIST) {
			add(item.get(0), item.get(1));
		}
	}
}