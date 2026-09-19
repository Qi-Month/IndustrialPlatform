package dev.celestiacraft.industrialplatform.client.screen;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformProperties;
import dev.celestiacraft.industrialplatform.client.BoundaryRenderData;
import dev.celestiacraft.industrialplatform.client.screen.widget.PlatformPreviewWidget;
import dev.celestiacraft.industrialplatform.client.screen.widget.ToggleButton;
import dev.celestiacraft.industrialplatform.common.menu.PlatformBuildMenu;
import dev.celestiacraft.industrialplatform.network.IPNetwork;
import dev.celestiacraft.industrialplatform.network.packet.PlatformBuildPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
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
	 * 面板左边的平台预览, 屏幕太窄放不下时不显示
	 */
	private static final int PREVIEW_WIDTH = 120;
	private static final int PREVIEW_GAP = 4;

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
	private PlatformPreviewWidget preview;
	private List<Rect2i> extraAreas = List.of();

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

		imageWidth = PlatformBuildMenu.PANEL_WIDTH;
		imageHeight = PlatformBuildMenu.PANEL_HEIGHT;
		titleLabelX = 8;
		titleLabelY = 6;
		inventoryLabelX = PlatformBuildMenu.INVENTORY_X;
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	protected void init() {
		super.init();

		upFill = menu.getUpFill();
		downFill = menu.getDownFill();
		mode = menu.getMode();
		modeButtons.clear();

		upFillField = createFillField(topPos + FILL_ROW_UP_Y, upFill, this::onUpFillTyped);
		downFillField = createFillField(topPos + FILL_ROW_UP_Y + FILL_ROW_HEIGHT, downFill, this::onDownFillTyped);

		upMinus = addRenderableWidget(createStepButton(MINUS_X, FILL_ROW_UP_Y, Component.literal("-"), button -> setUpFill(upFill - 1)));
		upPlus = addRenderableWidget(createStepButton(PLUS_X, FILL_ROW_UP_Y, Component.literal("+"), button -> setUpFill(upFill + 1)));
		downMinus = addRenderableWidget(createStepButton(MINUS_X, FILL_ROW_UP_Y + FILL_ROW_HEIGHT, Component.literal("-"), button -> setDownFill(downFill - 1)));
		downPlus = addRenderableWidget(createStepButton(PLUS_X, FILL_ROW_UP_Y + FILL_ROW_HEIGHT, Component.literal("+"), button -> setDownFill(downFill + 1)));

		addRenderableWidget(upFillField);
		addRenderableWidget(downFillField);

		modeButtons.add(createModeButton(LEFT, STYLE_BUTTON_Y, "style.industrial", () -> !mode.isCheckerboard(), button -> selectMode(false, mode.isHeavy())));
		modeButtons.add(createModeButton(RIGHT_BUTTON_X, STYLE_BUTTON_Y, "style.checkerboard", () -> mode.isCheckerboard(), button -> selectMode(true, mode.isHeavy())));
		modeButtons.add(createModeButton(LEFT, SIZE_BUTTON_Y, "size.light", () -> !mode.isHeavy(), button -> selectMode(mode.isCheckerboard(), false)));
		modeButtons.add(createModeButton(RIGHT_BUTTON_X, SIZE_BUTTON_Y, "size.heavy", () -> mode.isHeavy(), button -> selectMode(mode.isCheckerboard(), true)));

		modeButtons.forEach(this::addRenderableWidget);
		initPreview();

		buildButton = addRenderableWidget(Button.builder(Component.translatable("gui.industrial_platform.build"), (button) -> {
					IPNetwork.sendToServer(new PlatformBuildPacket());
				}).bounds(leftPos + RIGHT_BUTTON_X, topPos + BUILD_BUTTON_Y, BUTTON_WIDTH, BUILD_BUTTON_HEIGHT)
				.build());

		updateBuildButton();
	}

	/**
	 * 预览组件持有显存资源, 窗口缩放重新 init 时沿用同一个, 只挪位置
	 */
	private void initPreview() {
		int x = leftPos - PREVIEW_WIDTH - PREVIEW_GAP;

		if (preview == null) {
			preview = new PlatformPreviewWidget(menu.getPlatformPos(), x, topPos, PREVIEW_WIDTH, imageHeight);
		} else {
			preview.setX(x);
			preview.setY(topPos);
		}

		preview.visible = x >= 0;
		preview.update(mode, upFill, downFill);
		extraAreas = preview.visible ? List.of(new Rect2i(x, topPos, PREVIEW_WIDTH, imageHeight)) : List.of();
		addRenderableWidget(preview);
	}

	/**
	 * 面板之外被本界面占用的区域, JEI 之类的覆盖层要避开
	 */
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

	private Button createStepButton(int x, int y, Component message, Button.OnPress onPress) {
		return Button.builder(message, onPress)
				.bounds(leftPos + x, topPos + y, BUTTON_HEIGHT, BUTTON_HEIGHT)
				.build();
	}

	private ToggleButton createModeButton(int x, int y, String langKey, BooleanSupplier selected, Button.OnPress onPress) {
		Component label = Component.translatable("gui.industrial_platform." + langKey);
		Component tooltip = Component.translatable("gui.industrial_platform." + langKey + ".tooltip");

		return new ToggleButton(
				leftPos + x,
				topPos + y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				label,
				onPress,
				selected,
				Tooltip.create(tooltip)
		);
	}

	private EditBox createFillField(int y, int value, java.util.function.Consumer<String> responder) {
		EditBox field = new EditBox(font, leftPos + FIELD_X, y + 1, FIELD_WIDTH, FIELD_HEIGHT, Component.empty()) {
			@Override
			public void setFocused(boolean focused) {
				boolean wasFocused = isFocused();
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

		updatingWidgets = true;
		field.setValue(String.valueOf(value));
		updatingWidgets = false;

		return field;
	}

	private void onUpFillTyped(String text) {
		if (updatingWidgets) {
			return;
		}

		int parsed = parseFill(text, upFill);
		if (parsed == upFill) {
			return;
		}

		upFill = parsed;
		sendSettings();
	}

	private void onDownFillTyped(String text) {
		if (updatingWidgets) {
			return;
		}

		int parsed = parseFill(text, downFill);
		if (parsed == downFill) {
			return;
		}

		downFill = parsed;
		sendSettings();
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
		boolean changed = clamped != upFill;

		upFill = clamped;
		updatingWidgets = true;
		upFillField.setValue(String.valueOf(clamped));
		updatingWidgets = false;

		if (changed) {
			sendSettings();
		}
	}

	private void setDownFill(int value) {
		int clamped = PlatformBuildMenu.clampFill(value);
		boolean changed = clamped != downFill;

		downFill = clamped;
		updatingWidgets = true;
		downFillField.setValue(String.valueOf(clamped));
		updatingWidgets = false;

		if (changed) {
			sendSettings();
		}
	}

	private void normalizeFields() {
		updatingWidgets = true;
		upFillField.setValue(String.valueOf(upFill));
		downFillField.setValue(String.valueOf(downFill));
		updatingWidgets = false;
	}

	private void selectMode(boolean checkerboard, boolean heavy) {
		PlatformMode selected = PlatformMode.of(checkerboard, heavy);
		if (mode == selected) {
			return;
		}

		mode = selected;
		sendSettings();
	}

	private void sendSettings() {
		settingsChanged = true;
		IPNetwork.sendToServer(new PlatformSettingsPacket(upFill, downFill, mode.ordinal()));
		updateBuildButton();
	}

	/**
	 * 界面刚打开时数据槽还没同步过来, 这里把服务端的权威值补上
	 */
	private void syncFromMenu() {
		if (settingsChanged) {
			return;
		}

		int serverUpFill = menu.getUpFill();
		int serverDownFill = menu.getDownFill();
		PlatformMode serverMode = menu.getMode();

		if (serverUpFill == upFill && serverDownFill == downFill && serverMode == mode) {
			return;
		}

		upFill = serverUpFill;
		downFill = serverDownFill;
		mode = serverMode;
		normalizeFields();
	}

	private void updateBuildButton() {
		if (buildButton == null) {
			return;
		}

		boolean canBuild = menu.canBuild();
		buildButton.active = canBuild;
		buildButton.setMessage(Component.translatable("gui.industrial_platform.build", menu.getCost()));

		if (canBuild) {
			buildButton.setTooltip(Tooltip.create(Component.translatable(
					"gui.industrial_platform.build.tooltip",
					getStyleName(),
					getSizeName(),
					upFill,
					downFill
			)));
		} else {
			buildButton.setTooltip(Tooltip.create(Component.translatable("gui.industrial_platform.build.missing_material")));
		}
	}

	private Component getStyleName() {
		return Component.translatable(mode.isCheckerboard()
				? "gui.industrial_platform.style.checkerboard"
				: "gui.industrial_platform.style.industrial");
	}

	private Component getSizeName() {
		return Component.translatable(mode.isHeavy()
				? "gui.industrial_platform.size.heavy"
				: "gui.industrial_platform.size.light");
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (delta != 0.0D) {
			int step = hasShiftDown() ? 10 : 1;

			if (isOverFillControls(upFillField, upMinus, upPlus, mouseX, mouseY)) {
				setUpFill(upFill + (delta > 0.0D ? step : -step));
				return true;
			}

			if (isOverFillControls(downFillField, downMinus, downPlus, mouseX, mouseY)) {
				setDownFill(downFill + (delta > 0.0D ? step : -step));
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
		syncFromMenu();
		updateBuildButton();
		updatePreview();
	}

	private void updatePreview() {
		preview.update(mode, upFill, downFill);

		int size = mode.isHeavy() ? 48 : 16;

		BoundaryRenderData.setOverride(new BoundaryRenderData.BoundaryEntry(
				menu.getPlatformPos(),
				size,
				size,
				upFill,
				downFill
		));
	}

	@Override
	public void removed() {
		super.removed();
		// 关掉界面后交给方块状态 + 客户端缓存继续画
		BoundaryRenderData.clearOverride();

		if (preview != null) {
			preview.close();
		}
	}

	/**
	 * 点在预览面板上不算点到界面外面, 不然手里拿着物品一点就扔出去了
	 */
	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int button) {
		return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, button) && !preview.isMouseOver(mouseX, mouseY);
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(
				BACKGROUND,
				leftPos, topPos,
				0.0F, 0.0F,
				imageWidth, imageHeight,
				TEXTURE_SIZE, TEXTURE_SIZE
		);
	}

	@Override
	protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, title, titleLabelX, titleLabelY, COLOR_TITLE, false);
		graphics.drawString(font, Component.translatable("gui.industrial_platform.fill_up"), LEFT, 23, COLOR_LABEL, false);
		graphics.drawString(font, Component.translatable("gui.industrial_platform.fill_down"), LEFT, 47, COLOR_LABEL, false);
		graphics.drawString(font, Component.translatable("gui.industrial_platform.style"), LEFT, 66, COLOR_LABEL, false);
		graphics.drawString(font, Component.translatable("gui.industrial_platform.size"), LEFT, 100, COLOR_LABEL, false);

		boolean hasMaterial = !menu.getMaterial().isEmpty();
		graphics.drawString(font, Component.translatable("gui.industrial_platform.material"), 34, 138, hasMaterial ? COLOR_LABEL : COLOR_DISABLED, false);

		graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, COLOR_INVENTORY_LABEL, false);
	}
}