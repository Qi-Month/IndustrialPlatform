package dev.celestiacraft.industrialplatform.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.client.BoundaryRenderData;
import dev.celestiacraft.industrialplatform.common.menu.PlatformDesignerMenu;
import dev.celestiacraft.industrialplatform.network.IPNetwork;
import dev.celestiacraft.industrialplatform.network.packet.DesignerSelectPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformBuildPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsPacket;
import dev.celestiacraft.industrialplatform.platform.blueprint.ClientBlueprintData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 平台设计台界面: 上面是蓝图列表, 中间是上下填充, 下面是材料清单与搭建按钮
 */
public class PlatformDesignerScreen extends AbstractContainerScreen<PlatformDesignerMenu> {
	private static final ResourceLocation BACKGROUND = IndustrialPlatform.loadResource("textures/gui/platform_designer.png");
	private static final int TEXTURE_WIDTH = 256;
	private static final int TEXTURE_HEIGHT = 340;

	private static final int LIST_X = 14;
	private static final int LIST_Y = 26;
	private static final int LIST_WIDTH = 188;
	private static final int LIST_ROW_HEIGHT = 20;
	private static final int LIST_ROWS = 3;

	private static final int SCROLL_X = 208;
	private static final int SCROLL_UP_Y = 26;
	private static final int SCROLL_DOWN_Y = 48;
	private static final int SCROLL_SIZE = 18;

	private static final int FILL_ROW_UP_Y = 98;
	private static final int FILL_ROW_HEIGHT = 24;
	private static final int MINUS_X = 148;
	private static final int FIELD_X = 168;
	private static final int FIELD_WIDTH = 34;
	private static final int FIELD_HEIGHT = 16;
	private static final int PLUS_X = 206;
	private static final int STEP_SIZE = 18;

	private static final int MATERIAL_X = 12;
	private static final int MATERIAL_Y = 158;
	private static final int MATERIAL_STEP = 20;
	private static final int MATERIAL_SLOTS = 6;
	private static final int BUILD_X = 172;
	private static final int BUILD_Y = 158;
	private static final int BUILD_WIDTH = 56;
	private static final int BUILD_HEIGHT = 20;
	/**
	 * 材料翻页按钮(材料超过一页时才亮)
	 */
	private static final int PAGE_SIZE = 16;
	private static final int PAGE_PREV_X = 132;
	private static final int PAGE_NEXT_X = 150;
	private static final int PAGE_Y = 159;
	private static final int LABEL_X = 12;

	private static final int COLOR_TITLE = 0xFF404040;
	private static final int COLOR_LABEL = 0xFF404040;
	private static final int COLOR_HINT = 0xFF7A7A7A;
	private static final int COLOR_SIZE = 0xFF4A4A4A;
	private static final int COLOR_SELECTED = 0x806A9BE0;
	private static final int COLOR_HOVER = 0x30FFFFFF;

	private EditBox upFillField;
	private EditBox downFillField;
	private Button upMinus;
	private Button upPlus;
	private Button downMinus;
	private Button downPlus;
	private Button buildButton;
	private Button pagePrev;
	private Button pageNext;

	private String selectedId = "";
	private int scroll;
	private int materialScroll;
	private int upFill;
	private int downFill;
	private boolean updatingWidgets;
	/**
	 * 玩家是否已经改动过设置, 改动之后客户端以自己为准, 不再跟随服务端数据槽
	 */
	private boolean settingsChanged;

	public PlatformDesignerScreen(PlatformDesignerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);

		imageWidth = PlatformDesignerMenu.PANEL_WIDTH;
		imageHeight = PlatformDesignerMenu.PANEL_HEIGHT;
		titleLabelX = 8;
		titleLabelY = 6;
		inventoryLabelX = PlatformDesignerMenu.INVENTORY_X;
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	protected void init() {
		super.init();

		selectedId = ClientBlueprintData.getSelected();
		if (selectedId.isEmpty()) {
			selectedId = menu.getBlueprintId();
		}
		scroll = Math.max(0, indexOf(selectedId) - 1);

		upFill = menu.getUpFill();
		downFill = menu.getDownFill();

		upFillField = createFillField(topPos + FILL_ROW_UP_Y, upFill, this::onUpFillTyped);
		downFillField = createFillField(topPos + FILL_ROW_UP_Y + FILL_ROW_HEIGHT, downFill, this::onDownFillTyped);

		upMinus = addRenderableWidget(createStepButton(MINUS_X, FILL_ROW_UP_Y, "-", (button) -> setUpFill(upFill - 1)));
		upPlus = addRenderableWidget(createStepButton(PLUS_X, FILL_ROW_UP_Y, "+", (button) -> setUpFill(upFill + 1)));
		downMinus = addRenderableWidget(createStepButton(MINUS_X, FILL_ROW_UP_Y + FILL_ROW_HEIGHT, "-", (button) -> setDownFill(downFill - 1)));
		downPlus = addRenderableWidget(createStepButton(PLUS_X, FILL_ROW_UP_Y + FILL_ROW_HEIGHT, "+", (button) -> setDownFill(downFill + 1)));

		addRenderableWidget(upFillField);
		addRenderableWidget(downFillField);

		addRenderableWidget(Button.builder(Component.literal("↑"), (button) -> {
					scrollBy(-1);
				}).bounds(leftPos + SCROLL_X, topPos + SCROLL_UP_Y, SCROLL_SIZE, SCROLL_SIZE)
				.build());
		addRenderableWidget(Button.builder(Component.literal("↓"), (button) -> {
					scrollBy(1);
				}).bounds(leftPos + SCROLL_X, topPos + SCROLL_DOWN_Y, SCROLL_SIZE, SCROLL_SIZE)
				.build());

		pagePrev = addRenderableWidget(Button.builder(Component.literal("←"), (button) -> {
					pageMaterials(-1);
				}).bounds(leftPos + PAGE_PREV_X, topPos + PAGE_Y, PAGE_SIZE, PAGE_SIZE)
				.build());

		pageNext = addRenderableWidget(Button.builder(Component.literal("→"), (button) -> {
					pageMaterials(1);
				}).bounds(leftPos + PAGE_NEXT_X, topPos + PAGE_Y, PAGE_SIZE, PAGE_SIZE)
				.build());

		buildButton = addRenderableWidget(Button.builder(Component.translatable("gui.industrial_platform.build_plain"), (button) -> {
					IPNetwork.sendToServer(new PlatformBuildPacket());
				}).bounds(leftPos + BUILD_X, topPos + BUILD_Y, BUILD_WIDTH, BUILD_HEIGHT)
				.build());

		updateBuildButton();
	}

	private Button createStepButton(int x, int y, String label, Button.OnPress onPress) {
		return Button.builder(Component.literal(label), onPress)
				.bounds(leftPos + x, topPos + y, STEP_SIZE, STEP_SIZE)
				.build();
	}

	private EditBox createFillField(int y, int value, java.util.function.Consumer<String> responder) {
		EditBox field = new EditBox(font, leftPos + FIELD_X, y + 1, FIELD_WIDTH, FIELD_HEIGHT, Component.empty()) {
			@Override
			public void setFocused(boolean focused) {
				boolean wasFocused = isFocused();
				super.setFocused(focused);

				if (wasFocused && !focused) {
					PlatformDesignerScreen.this.normalizeFields();
				}
			}
		};

		field.setMaxLength(2);
		field.setFilter((text) -> {
			return text.isEmpty() || text.chars().allMatch(Character::isDigit);
		});
		field.setResponder(responder);

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
		sendFill(upFill, downFill);
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
		sendFill(upFill, downFill);
	}

	private static int parseFill(String text, int fallback) {
		if (text == null || text.isEmpty()) {
			return fallback;
		}

		try {
			return PlatformDesignerMenu.clampFill(Integer.parseInt(text.trim()));
		} catch (NumberFormatException exception) {
			return fallback;
		}
	}

	private void setUpFill(int value) {
		int clamped = PlatformDesignerMenu.clampFill(value);
		boolean changed = clamped != upFill;

		upFill = clamped;
		updatingWidgets = true;
		upFillField.setValue(String.valueOf(clamped));
		updatingWidgets = false;

		if (changed) {
			sendFill(upFill, downFill);
		}
	}

	private void setDownFill(int value) {
		int clamped = PlatformDesignerMenu.clampFill(value);
		boolean changed = clamped != downFill;

		downFill = clamped;
		updatingWidgets = true;
		downFillField.setValue(String.valueOf(clamped));
		updatingWidgets = false;

		if (changed) {
			sendFill(upFill, downFill);
		}
	}

	private void sendFill(int upFill, int downFill) {
		settingsChanged = true;
		IPNetwork.sendToServer(new PlatformSettingsPacket(upFill, downFill, 0));
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

		if (serverUpFill == upFill && serverDownFill == downFill) {
			return;
		}

		upFill = serverUpFill;
		downFill = serverDownFill;
		normalizeFields();
	}

	private void normalizeFields() {
		updatingWidgets = true;
		upFillField.setValue(String.valueOf(upFill));
		downFillField.setValue(String.valueOf(downFill));
		updatingWidgets = false;
	}

	private List<ClientBlueprintData.Entry> entries() {
		return ClientBlueprintData.list();
	}

	private int maxScroll() {
		return Math.max(0, entries().size() - LIST_ROWS);
	}

	private int indexOf(String id) {
		List<ClientBlueprintData.Entry> entries = entries();
		for (int index = 0; index < entries.size(); index++) {
			if (entries.get(index).id().equals(id)) {
				return index;
			}
		}
		return 0;
	}

	private void pageMaterials(int delta) {
		materialScroll = Mth.clamp(materialScroll + delta, 0, maxMaterialScroll());
	}

	private void updatePageButtons() {
		if (pagePrev == null || pageNext == null) {
			return;
		}

		pagePrev.active = materialScroll > 0;
		pageNext.active = materialScroll < maxMaterialScroll();
	}

	private void scrollBy(int delta) {
		scroll = Mth.clamp(scroll + delta, 0, maxScroll());
	}

	private void select(String id) {
		if (id.equals(selectedId)) {
			return;
		}

		selectedId = id;
		ClientBlueprintData.setSelected(id);
		IPNetwork.sendToServer(new DesignerSelectPacket(id));
		updateBuildButton();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (delta != 0.0D) {
			if (isOverList(mouseX, mouseY)) {
				scrollBy(delta > 0.0D ? -1 : 1);
				return true;
			}

			if (isOverMaterials(mouseX, mouseY) && maxMaterialScroll() > 0) {
				materialScroll = Mth.clamp(materialScroll + (delta > 0.0D ? -1 : 1), 0, maxMaterialScroll());
				return true;
			}

			if (isOverFillControls(upFillField, upMinus, upPlus, mouseX, mouseY)) {
				setUpFill(upFill + (delta > 0.0D ? 1 : -1));
				return true;
			}

			if (isOverFillControls(downFillField, downMinus, downPlus, mouseX, mouseY)) {
				setDownFill(downFill + (delta > 0.0D ? 1 : -1));
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

	private boolean isOverList(double mouseX, double mouseY) {
		double localX = mouseX - leftPos;
		double localY = mouseY - topPos;

		return localX >= LIST_X && localX < LIST_X + LIST_WIDTH && localY >= LIST_Y && localY < LIST_Y + LIST_ROWS * LIST_ROW_HEIGHT;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && isOverList(mouseX, mouseY)) {
			int row = (int) ((mouseY - topPos - LIST_Y) / LIST_ROW_HEIGHT);
			int index = scroll + row;
			List<ClientBlueprintData.Entry> entries = entries();

			if (index >= 0 && index < entries.size()) {
				select(entries.get(index).id());
				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void containerTick() {
		super.containerTick();

		scroll = Mth.clamp(scroll, 0, maxScroll());
		materialScroll = Mth.clamp(materialScroll, 0, maxMaterialScroll());
		syncFromMenu();
		updatePageButtons();
		updateBuildButton();
		updatePreview();
	}

	private void updatePreview() {
		ClientBlueprintData.Entry entry = ClientBlueprintData.get(selectedId);
		if (entry == null) {
			BoundaryRenderData.clearOverride();
			return;
		}

		BoundaryRenderData.setOverride(new BoundaryRenderData.BoundaryEntry(
				menu.getControllerPos(),
				entry.sizeX(),
				entry.sizeZ(),
				upFill,
				downFill
		));
	}

	@Override
	public void removed() {
		super.removed();
		BoundaryRenderData.clearOverride();
	}

	private void updateBuildButton() {
		if (buildButton == null) {
			return;
		}

		boolean canBuild = menu.canBuild();
		buildButton.active = canBuild;

		ClientBlueprintData.Entry entry = ClientBlueprintData.get(selectedId);

		if (canBuild && entry != null) {
			buildButton.setTooltip(Tooltip.create(Component.literal(entry.id() + "  " + entry.describeSize())));
		} else {
			buildButton.setTooltip(Tooltip.create(Component.translatable("gui.industrial_platform.not_enough_materials")));
		}
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// 原版那种打开容器时压暗背景
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTick);

		renderMaterialTooltip(graphics, mouseX, mouseY);

		// 原版容器界面的物品 tooltip 要界面自己调, 不然鼠标放在背包物品上没有提示框
		renderTooltip(graphics, mouseX, mouseY);
	}

	/**
	 * 材料槽不是菜单槽位, 悬停提示要自己画
	 */
	private void renderMaterialTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
		List<Map.Entry<Item, Integer>> materials = materials();
		int index = materialIndexAt(mouseX, mouseY);

		if (index < 0 || index >= materials.size()) {
			return;
		}

		Map.Entry<Item, Integer> material = materials.get(index);
		graphics.renderTooltip(font, Component.translatable(
				"gui.industrial_platform.blueprint_material_tip",
				new ItemStack(material.getKey()).getHoverName(),
				material.getValue()
		), mouseX, mouseY);
	}

	@Override
	protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		graphics.blit(BACKGROUND, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		drawBlueprintList(graphics, mouseX, mouseY);
		drawMaterials(graphics);
	}

	private void drawBlueprintList(GuiGraphics graphics, int mouseX, int mouseY) {
		List<ClientBlueprintData.Entry> entries = entries();

		if (entries.isEmpty()) {
			Component hint = Component.translatable("gui.industrial_platform.blueprint_empty");
			graphics.drawString(font, hint, leftPos + (imageWidth - font.width(hint)) / 2, topPos + LIST_Y + 26, COLOR_HINT, false);
			return;
		}

		for (int row = 0; row < LIST_ROWS; row++) {
			int index = scroll + row;
			if (index >= entries.size()) {
				break;
			}

			ClientBlueprintData.Entry entry = entries.get(index);
			int rowX = leftPos + LIST_X;
			int rowY = topPos + LIST_Y + row * LIST_ROW_HEIGHT;
			boolean selected = entry.id().equals(selectedId);
			boolean hovered = mouseX >= rowX && mouseX < rowX + LIST_WIDTH && mouseY >= rowY && mouseY < rowY + LIST_ROW_HEIGHT - 2;

			if (selected) {
				graphics.fill(rowX, rowY, rowX + LIST_WIDTH, rowY + LIST_ROW_HEIGHT - 2, COLOR_SELECTED);
			} else if (hovered) {
				graphics.fill(rowX, rowY, rowX + LIST_WIDTH, rowY + LIST_ROW_HEIGHT - 2, COLOR_HOVER);
			}

			String name = entry.id();
			int maxNameWidth = LIST_WIDTH - font.width(entry.describeSize()) - 16;
			if (font.width(name) > maxNameWidth) {
				name = font.plainSubstrByWidth(name, maxNameWidth - 6) + "...";
			}

			graphics.drawString(font, name, rowX + 4, rowY + 6, COLOR_LABEL, false);

			String size = entry.describeSize();
			graphics.drawString(font, size, rowX + LIST_WIDTH - font.width(size) - 4, rowY + 6, COLOR_SIZE, false);
		}
	}

	private void drawMaterials(GuiGraphics graphics) {
		List<Map.Entry<Item, Integer>> materials = materials();

		for (int slot = 0; slot < MATERIAL_SLOTS; slot++) {
			int index = materialScroll + slot;
			if (index >= materials.size()) {
				break;
			}

			Map.Entry<Item, Integer> material = materials.get(index);
			int x = leftPos + MATERIAL_X + slot * MATERIAL_STEP;
			int y = topPos + MATERIAL_Y;

			ItemStack stack = new ItemStack(material.getKey(), 1);
			graphics.renderItem(stack, x, y);
			graphics.renderItemDecorations(font, stack, x, y, String.valueOf(material.getValue()));
		}
	}

	/**
	 * 选中蓝图的材料清单(顺序固定)
	 */
	private List<Map.Entry<Item, Integer>> materials() {
		ClientBlueprintData.Entry entry = ClientBlueprintData.get(selectedId);
		if (entry == null) {
			return List.of();
		}

		return new ArrayList<>(entry.materials().entrySet());
	}

	private int maxMaterialScroll() {
		return Math.max(0, materials().size() - MATERIAL_SLOTS);
	}

	/**
	 * 鼠标底下是第几个材料槽, 不在材料行上就返回 -1
	 */
	private int materialIndexAt(double mouseX, double mouseY) {
		double localX = mouseX - leftPos;
		double localY = mouseY - topPos;

		if (localY < MATERIAL_Y || localY >= MATERIAL_Y + 18) {
			return -1;
		}

		for (int slot = 0; slot < MATERIAL_SLOTS; slot++) {
			double slotX = MATERIAL_X + slot * MATERIAL_STEP;
			if (localX >= slotX && localX < slotX + 18) {
				int index = materialScroll + slot;
				return index < materials().size() ? index : -1;
			}
		}

		return -1;
	}

	private boolean isOverMaterials(double mouseX, double mouseY) {
		return materialIndexAt(mouseX, mouseY) >= 0;
	}

	@Override
	protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, title, titleLabelX, titleLabelY, COLOR_TITLE, false);
		graphics.drawString(font, Component.translatable("gui.industrial_platform.fill_up"), LABEL_X, FILL_ROW_UP_Y + 5, COLOR_LABEL, false);
		graphics.drawString(font, Component.translatable("gui.industrial_platform.fill_down"), LABEL_X, FILL_ROW_UP_Y + FILL_ROW_HEIGHT + 5, COLOR_LABEL, false);
		graphics.drawString(font, Component.translatable("gui.industrial_platform.blueprint_materials"), LABEL_X, 146, COLOR_LABEL, false);


		graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, COLOR_LABEL, false);
	}
}