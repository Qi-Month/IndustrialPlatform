package dev.celestiacraft.industrialplatform.client.screen;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.block.state.properties.platform.PlatformProperties;
import dev.celestiacraft.industrialplatform.client.BoundaryRenderData;
import dev.celestiacraft.industrialplatform.client.screen.widget.ToggleButton;
import dev.celestiacraft.industrialplatform.menu.PlatformBuildMenu;
import dev.celestiacraft.industrialplatform.network.IPNetwork;
import dev.celestiacraft.industrialplatform.network.packet.PlatformBuildPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * 平台搭建界面
 */
public class PlatformBuildScreen extends AbstractContainerScreen<PlatformBuildMenu> {
	private static final int LEFT = 12;
	private static final int RIGHT_BUTTON_X = 100;
	private static final int BUTTON_WIDTH = 88;
	private static final int BUTTON_HEIGHT = 18;

	private static final int FILL_ROW_UP_Y = 18;
	private static final int FILL_ROW_HEIGHT = 24;
	private static final int MINUS_X = 108;
	private static final int FIELD_X = 128;
	private static final int FIELD_WIDTH = 34;
	private static final int FIELD_HEIGHT = 16;
	private static final int PLUS_X = 164;

	private static final int STYLE_BUTTON_Y = 76;
	private static final int SIZE_BUTTON_Y = 110;
	private static final int BUILD_BUTTON_Y = 132;
	private static final int BUILD_BUTTON_HEIGHT = 20;

	/**
	 * 界面背景贴图, 256x256 的画布, 面板画在左上角 200x250
	 */
	private static final ResourceLocation BACKGROUND = IndustrialPlatform.loadResource("textures/gui/platform_build.png");
	private static final int TEXTURE_SIZE = 256;

	private static final int COLOR_TITLE = 0xFF404040;
	private static final int COLOR_LABEL = 0xFF404040;
	private static final int COLOR_DISABLED = 0xFF808080;
	private static final int COLOR_INVENTORY_LABEL = 0xFF404040;

	private final List<ToggleButton> modeButtons = new ArrayList<>();

	private EditBox upFillField;
	private EditBox downFillField;
	private Button upMinus;
	private Button upPlus;
	private Button downMinus;
	private Button downPlus;
	private Button buildButton;

	private int upFill;
	private int downFill;
	private PlatformMode mode = PlatformMode.INDUSTRIAL_LIGHT;
	private boolean updatingWidgets;
	/**
	 * 玩家是否已经改动过设置, 改动之后客户端以自己为准, 不再跟随服务端数据槽
	 */
	private boolean settingsChanged;

	public PlatformBuildScreen(PlatformBuildMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);

		this.imageWidth = PlatformBuildMenu.PANEL_WIDTH;
		this.imageHeight = PlatformBuildMenu.PANEL_HEIGHT;
		this.titleLabelX = 8;
		this.titleLabelY = 6;
		this.inventoryLabelX = PlatformBuildMenu.INVENTORY_X;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	protected void init() {
		super.init();

		this.upFill = this.menu.getUpFill();
		this.downFill = this.menu.getDownFill();
		this.mode = this.menu.getMode();
		this.modeButtons.clear();

		this.upFillField = this.createFillField(this.topPos + FILL_ROW_UP_Y, this.upFill, this::onUpFillTyped);
		this.downFillField = this.createFillField(this.topPos + FILL_ROW_UP_Y + FILL_ROW_HEIGHT, this.downFill, this::onDownFillTyped);

		this.upMinus = this.addRenderableWidget(this.createStepButton(MINUS_X, FILL_ROW_UP_Y, Component.literal("-"), button -> this.setUpFill(this.upFill - 1)));
		this.upPlus = this.addRenderableWidget(this.createStepButton(PLUS_X, FILL_ROW_UP_Y, Component.literal("+"), button -> this.setUpFill(this.upFill + 1)));
		this.downMinus = this.addRenderableWidget(this.createStepButton(MINUS_X, FILL_ROW_UP_Y + FILL_ROW_HEIGHT, Component.literal("-"), button -> this.setDownFill(this.downFill - 1)));
		this.downPlus = this.addRenderableWidget(this.createStepButton(PLUS_X, FILL_ROW_UP_Y + FILL_ROW_HEIGHT, Component.literal("+"), button -> this.setDownFill(this.downFill + 1)));

		this.addRenderableWidget(this.upFillField);
		this.addRenderableWidget(this.downFillField);

		this.modeButtons.add(this.createModeButton(LEFT, STYLE_BUTTON_Y, "style.industrial", () -> !this.mode.isCheckerboard(), button -> this.selectMode(false, this.mode.isHeavy())));
		this.modeButtons.add(this.createModeButton(RIGHT_BUTTON_X, STYLE_BUTTON_Y, "style.checkerboard", () -> this.mode.isCheckerboard(), button -> this.selectMode(true, this.mode.isHeavy())));
		this.modeButtons.add(this.createModeButton(LEFT, SIZE_BUTTON_Y, "size.light", () -> !this.mode.isHeavy(), button -> this.selectMode(this.mode.isCheckerboard(), false)));
		this.modeButtons.add(this.createModeButton(RIGHT_BUTTON_X, SIZE_BUTTON_Y, "size.heavy", () -> this.mode.isHeavy(), button -> this.selectMode(this.mode.isCheckerboard(), true)));

		this.modeButtons.forEach(this::addRenderableWidget);

		this.buildButton = this.addRenderableWidget(
				Button.builder(Component.translatable("gui.industrial_platform.build"), (button) -> {
							IPNetwork.sendToServer(new PlatformBuildPacket());
						})
						.bounds(this.leftPos + RIGHT_BUTTON_X, this.topPos + BUILD_BUTTON_Y, BUTTON_WIDTH, BUILD_BUTTON_HEIGHT)
						.build());

		this.updateBuildButton();
	}

	private Button createStepButton(int x, int y, Component message, Button.OnPress onPress) {
		return Button.builder(message, onPress)
				.bounds(this.leftPos + x, this.topPos + y, BUTTON_HEIGHT, BUTTON_HEIGHT)
				.build();
	}

	private ToggleButton createModeButton(int x, int y, String langKey, BooleanSupplier selected, Button.OnPress onPress) {
		Component label = Component.translatable("gui.industrial_platform." + langKey);
		Component tooltip = Component.translatable("gui.industrial_platform." + langKey + ".tooltip");

		return new ToggleButton(
				this.leftPos + x,
				this.topPos + y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				label,
				onPress,
				selected,
				Tooltip.create(tooltip)
		);
	}

	private EditBox createFillField(int y, int value, java.util.function.Consumer<String> responder) {
		EditBox field = new EditBox(this.font, this.leftPos + FIELD_X, y + 1, FIELD_WIDTH, FIELD_HEIGHT, Component.empty()) {
			@Override
			public void setFocused(boolean focused) {
				boolean wasFocused = this.isFocused();
				super.setFocused(focused);

				if (wasFocused && !focused) {
					PlatformBuildScreen.this.normalizeFields();
				}
			}
		};

		field.setMaxLength(2);
		field.setFilter((text) -> {
			return text.isEmpty() || text.chars().allMatch(Character::isDigit);
		});
		field.setResponder(responder);
		field.setTooltip(Tooltip.create(Component.translatable(
				"gui.industrial_platform.fill_range",
				PlatformProperties.MIN_FILL_DISTANCE,
				PlatformProperties.MAX_FILL_DISTANCE
		)));

		this.updatingWidgets = true;
		field.setValue(String.valueOf(value));
		this.updatingWidgets = false;

		return field;
	}

	private void onUpFillTyped(String text) {
		if (this.updatingWidgets) {
			return;
		}

		int parsed = parseFill(text, this.upFill);
		if (parsed == this.upFill) {
			return;
		}

		this.upFill = parsed;
		this.sendSettings();
	}

	private void onDownFillTyped(String text) {
		if (this.updatingWidgets) {
			return;
		}

		int parsed = parseFill(text, this.downFill);
		if (parsed == this.downFill) {
			return;
		}

		this.downFill = parsed;
		this.sendSettings();
	}

	private static int parseFill(String text, int fallback) {
		if (text == null || text.isEmpty()) {
			return fallback;
		}

		try {
			return PlatformBuildMenu.clampFill(Integer.parseInt(text.trim()));
		} catch (NumberFormatException exception) {
			return fallback;
		}
	}

	private void setUpFill(int value) {
		int clamped = PlatformBuildMenu.clampFill(value);
		boolean changed = clamped != this.upFill;

		this.upFill = clamped;
		this.updatingWidgets = true;
		this.upFillField.setValue(String.valueOf(clamped));
		this.updatingWidgets = false;

		if (changed) {
			this.sendSettings();
		}
	}

	private void setDownFill(int value) {
		int clamped = PlatformBuildMenu.clampFill(value);
		boolean changed = clamped != this.downFill;

		this.downFill = clamped;
		this.updatingWidgets = true;
		this.downFillField.setValue(String.valueOf(clamped));
		this.updatingWidgets = false;

		if (changed) {
			this.sendSettings();
		}
	}

	private void normalizeFields() {
		this.updatingWidgets = true;
		this.upFillField.setValue(String.valueOf(this.upFill));
		this.downFillField.setValue(String.valueOf(this.downFill));
		this.updatingWidgets = false;
	}

	private void selectMode(boolean checkerboard, boolean heavy) {
		PlatformMode selected = PlatformMode.of(checkerboard, heavy);
		if (this.mode == selected) {
			return;
		}

		this.mode = selected;
		this.sendSettings();
	}

	private void sendSettings() {
		this.settingsChanged = true;
		IPNetwork.sendToServer(new PlatformSettingsPacket(this.upFill, this.downFill, this.mode.ordinal()));
		this.updateBuildButton();
	}

	/**
	 * 界面刚打开时数据槽还没同步过来, 这里把服务端的权威值补上
	 */
	private void syncFromMenu() {
		if (this.settingsChanged) {
			return;
		}

		int serverUpFill = this.menu.getUpFill();
		int serverDownFill = this.menu.getDownFill();
		PlatformMode serverMode = this.menu.getMode();

		if (serverUpFill == this.upFill && serverDownFill == this.downFill && serverMode == this.mode) {
			return;
		}

		this.upFill = serverUpFill;
		this.downFill = serverDownFill;
		this.mode = serverMode;
		this.normalizeFields();
	}

	private void updateBuildButton() {
		if (this.buildButton == null) {
			return;
		}

		boolean canBuild = this.menu.canBuild();
		this.buildButton.active = canBuild;
		this.buildButton.setMessage(Component.translatable("gui.industrial_platform.build", this.menu.getCost()));

		if (canBuild) {
			this.buildButton.setTooltip(Tooltip.create(Component.translatable(
					"gui.industrial_platform.build.tooltip",
					this.getStyleName(),
					this.getSizeName(),
					this.upFill,
					this.downFill
			)));
		} else {
			this.buildButton.setTooltip(Tooltip.create(Component.translatable("gui.industrial_platform.build.missing_material")));
		}
	}

	private Component getStyleName() {
		return Component.translatable(this.mode.isCheckerboard()
				? "gui.industrial_platform.style.checkerboard"
				: "gui.industrial_platform.style.industrial");
	}

	private Component getSizeName() {
		return Component.translatable(this.mode.isHeavy()
				? "gui.industrial_platform.size.heavy"
				: "gui.industrial_platform.size.light");
	}

	/**
	 * 在填充格数的输入框 / 加减号上滚轮可以直接调节, 按住 Shift 一次 10 格
	 */
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (delta != 0.0D) {
			int step = hasShiftDown() ? 10 : 1;

			if (this.isOverFillControls(this.upFillField, this.upMinus, this.upPlus, mouseX, mouseY)) {
				this.setUpFill(this.upFill + (delta > 0.0D ? step : -step));
				return true;
			}

			if (this.isOverFillControls(this.downFillField, this.downMinus, this.downPlus, mouseX, mouseY)) {
				this.setDownFill(this.downFill + (delta > 0.0D ? step : -step));
				return true;
			}
		}

		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	private boolean isOverFillControls(EditBox field, Button minus, Button plus, double mouseX, double mouseY) {
		if (field == null || minus == null || plus == null) {
			return false;
		}

		return field.isMouseOver(mouseX, mouseY) || minus.isMouseOver(mouseX, mouseY) || plus.isMouseOver(mouseX, mouseY);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		this.syncFromMenu();
		this.updateBuildButton();
		this.updatePreview();
	}

	/**
	 * 把界面里的当前选择丢给外面的区块预览, 改一下模式/填充格数马上就能看到
	 */
	private void updatePreview() {
		int size = this.mode.isHeavy() ? 48 : 16;

		BoundaryRenderData.setOverride(new BoundaryRenderData.BoundaryEntry(
				this.menu.getPlatformPos(),
				size,
				size,
				this.upFill == 0 && this.downFill == 0,
				this.upFill,
				this.downFill
		));
	}

	@Override
	public void removed() {
		super.removed();
		// 关掉界面后交给方块状态 + 客户端缓存继续画
		BoundaryRenderData.clearOverride();
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// 原版那种打开容器时压暗背景
		this.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(
				BACKGROUND,
				this.leftPos, this.topPos,
				0.0F, 0.0F,
				this.imageWidth, this.imageHeight,
				TEXTURE_SIZE, TEXTURE_SIZE
		);
	}

	@Override
	protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, COLOR_TITLE, false);
		graphics.drawString(this.font, Component.translatable("gui.industrial_platform.fill_up"), LEFT, 23, COLOR_LABEL, false);
		graphics.drawString(this.font, Component.translatable("gui.industrial_platform.fill_down"), LEFT, 47, COLOR_LABEL, false);
		graphics.drawString(this.font, Component.translatable("gui.industrial_platform.style"), LEFT, 66, COLOR_LABEL, false);
		graphics.drawString(this.font, Component.translatable("gui.industrial_platform.size"), LEFT, 100, COLOR_LABEL, false);

		boolean hasMaterial = !this.menu.getMaterial().isEmpty();
		graphics.drawString(this.font, Component.translatable("gui.industrial_platform.material"), 34, 138, hasMaterial ? COLOR_LABEL : COLOR_DISABLED, false);

		graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, COLOR_INVENTORY_LABEL, false);
	}
}