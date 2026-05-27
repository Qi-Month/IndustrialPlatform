package dev.celestiacraft.industrialplatform.datagen.language.locale;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.datagen.language.LanguageGenerate;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.List;

public class Chinese extends LanguageProvider {
	public Chinese(PackOutput output) {
		super(output, IndustrialPlatform.MODID, "zh_cn");
	}

	@Override
	protected void addTranslations() {
		for (List<String> item : LanguageGenerate.TRANSLATION_LIST) {
			add(item.get(0), item.get(2));
		}
	}
}