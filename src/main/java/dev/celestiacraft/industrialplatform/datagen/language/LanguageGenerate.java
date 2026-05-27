package dev.celestiacraft.industrialplatform.datagen.language;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.datagen.language.type.BlockLanguage;
import dev.celestiacraft.industrialplatform.datagen.language.type.OtherLanguage;

import java.util.ArrayList;
import java.util.List;

public class LanguageGenerate {
	public static final List<List<String>> TRANSLATION_LIST = new ArrayList<>();

	public static void register() {
		BlockLanguage.addLang();
		OtherLanguage.addLang();
	}

	protected static void addLanguage(String type, String key, String english, String chinese) {
		String fullKey;

		if (type == null || type.isEmpty()) {
			fullKey = String.format("%s.%s", IndustrialPlatform.MODID, key);
		} else {
			fullKey = String.format("%s.%s.%s", type, IndustrialPlatform.MODID, key);
		}

		addCustomLang(fullKey, english, chinese);
	}

	/**
	 * 添加自定义翻译
	 *
	 * @param key     翻译键
	 * @param english 英文
	 * @param chinese 中文
	 */
	protected static void addCustomLang(String key, String english, String chinese) {
		List<String> newList = new ArrayList<>();
		newList.add(key);
		newList.add(english);
		newList.add(chinese);
		TRANSLATION_LIST.add(newList);
	}

	protected static void addBlockLanguage(String key, String english, String chinese) {
		addLanguage("block", key, english, chinese);
	}

	protected static void addItemLanguage(String key, String english, String chinese) {
		addLanguage("item", key, english, chinese);
	}
}