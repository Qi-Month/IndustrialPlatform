package dev.celestiacraft.industrialplatform.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class NumericEditBox extends EditBox {
	public NumericEditBox(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
	}

	@Override
	public void setValue(@NotNull String value) {
		if (isNumeric(value, false)) {
			super.setValue(value);
		}
	}

	@Override
	public void insertText(@NotNull String input) {
		if (isNumeric(input, true)) {
			super.insertText(input);
		}
	}

	private static boolean isNumeric(String text, boolean ignoreFilteredCharacters) {
		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);
			if (!Character.isDigit(character) && (!ignoreFilteredCharacters || StringUtil.isAllowedChatCharacter(character))) {
				return false;
			}
		}
		return true;
	}
}
