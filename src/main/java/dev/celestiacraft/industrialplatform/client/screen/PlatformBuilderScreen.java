package dev.celestiacraft.industrialplatform.client.screen;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.client.BoundaryRenderData;
import dev.celestiacraft.industrialplatform.client.network.IPClientNetwork;
import dev.celestiacraft.industrialplatform.common.menu.PlatformBuilderMenu;
import dev.celestiacraft.industrialplatform.network.packet.BuilderSelectPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformBuildPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsPacket;
import dev.celestiacraft.industrialplatform.platform.blueprint.ClientBlueprintData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 平台建造站界面: 上面是蓝图列表, 中间是上下填充, 下面是材料清单与搭建按钮
 */
public class PlatformBuilderScreen extends AbstractContainerScreen<PlatformBuilderMenu> {
	private static final Identifier BACKGROUND = IndustrialPlatform.loadResource("textures/gui/platform_builder.png");
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
	private ClientBlueprintData.Entry materialEntry;
	private List<MaterialDisplay> materialDisplays = List.of();
	private List<ClientBlueprintData.Entry> blueprintEntries = List.of();
	private List<BlueprintDisplay> blueprintDisplays = List.of();
	private ClientBlueprintData.Entry buildButtonEntry;
	private Boolean buildButtonAvailability;
	private BoundaryRenderData.BoundaryEntry boundaryOverride;

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

	public PlatformBuilderScreen(PlatformBuilderMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, PlatformBuilderMenu.PANEL_WIDTH, PlatformBuilderMenu.PANEL_HEIGHT);
		inventoryLabelX = PlatformBuilderMenu.INVENTORY_X;
	}

	@Override
	protected void init() {
		super.init();

		blueprintEntries = List.of();
		buildButtonAvailability = null;
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
					IPClientNetwork.sendToServer(new PlatformBuildPacket());
				}).bounds(leftPos + BUILD_X, topPos + BUILD_Y, BUILD_WIDTH, BUILD_HEIGHT)
				.build());

		updateBuildButton();
	}

	private Button createStepButton(int x, int y, String label, Button.OnPress onPress) {
		return Button.builder(Component.literal(label), onPress)
				.bounds(leftPos + x, topPos + y, STEP_SIZE, STEP_SIZE)
				.build();
	}

	private EditBox createFillField(int y, int value, Consumer<String> responder) {
		EditBox field = new EditBox(font, leftPos + FIELD_X, y + 1, FIELD_WIDTH, FIELD_HEIGHT, Component.empty()) {
			@Override
			public void setFocused(boolean focused) {
				boolean wasFocused = isFocused();
				super.setFocused(focused);

				if (wasFocused && !focused) {
					normalizeFields();
				}
			}
		};

		field.setMaxLength(2);
		field.setFilter(text -> text.isEmpty() || text.chars().allMatch(Character::isDigit));
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
			return PlatformBuilderMenu.clampFill(Integer.parseInt(text.trim()));
		} catch (NumberFormatException exception) {
			return fallback;
		}
	}

	private void setUpFill(int value) {
		int clamped = PlatformBuilderMenu.clampFill(value);
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
		int clamped = PlatformBuilderMenu.clampFill(value);
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
		IPClientNetwork.sendToServer(new PlatformSettingsPacket(upFill, downFill, 0));
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
		List<ClientBlueprintData.Entry> entries = ClientBlueprintData.list();
		if (entries != blueprintEntries) {
			blueprintEntries = entries;
			List<BlueprintDisplay> displays = new ArrayList<>(entries.size());
			for (ClientBlueprintData.Entry entry : entries) {
				String size = entry.describeSize();
				int sizeWidth = font.width(size);
				int maxNameWidth = LIST_WIDTH - sizeWidth - 16;
				String name = entry.id();
				if (font.width(name) > maxNameWidth) {
					name = font.plainSubstrByWidth(name, maxNameWidth - 6) + "...";
				}
				displays.add(new BlueprintDisplay(name, size, sizeWidth));
			}
			blueprintDisplays = List.copyOf(displays);
		}
		return blueprintEntries;
	}

	private record BlueprintDisplay(String name, String size, int sizeWidth) {
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
		IPClientNetwork.sendToServer(new BuilderSelectPacket(id));
		updateBuildButton();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY != 0.0D) {
			if (isOverList(mouseX, mouseY)) {
				scrollBy(scrollY > 0.0D ? -1 : 1);
				return true;
			}

			if (isOverMaterials(mouseX, mouseY) && maxMaterialScroll() > 0) {
				materialScroll = Mth.clamp(materialScroll + (scrollY > 0.0D ? -1 : 1), 0, maxMaterialScroll());
				return true;
			}

			if (isOverFillControls(upFillField, upMinus, upPlus, mouseX, mouseY)) {
				setUpFill(upFill + (scrollY > 0.0D ? 1 : -1));
				return true;
			}

			if (isOverFillControls(downFillField, downMinus, downPlus, mouseX, mouseY)) {
				setDownFill(downFill + (scrollY > 0.0D ? 1 : -1));
				return true;
			}
		}

		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0 && isOverList(event.x(), event.y())) {
			int row = (int) ((event.y() - topPos - LIST_Y) / LIST_ROW_HEIGHT);
			int index = scroll + row;
			List<ClientBlueprintData.Entry> entries = entries();

			if (index >= 0 && index < entries.size()) {
				select(entries.get(index).id());
				return true;
			}
		}

		return super.mouseClicked(event, doubleClick);
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

		if (boundaryOverride == null || boundaryOverride.sizeX() != entry.sizeX() || boundaryOverride.sizeZ() != entry.sizeZ()
				|| boundaryOverride.upFill() != upFill || boundaryOverride.downFill() != downFill) {
			boundaryOverride = new BoundaryRenderData.BoundaryEntry(menu.getControllerPos(), entry.sizeX(), entry.sizeZ(), upFill, downFill);
		}
		BoundaryRenderData.setOverride(boundaryOverride);
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
		if (buildButtonAvailability != null && buildButtonAvailability == canBuild && buildButtonEntry == entry) {
			return;
		}
		buildButtonAvailability = canBuild;
		buildButtonEntry = entry;

		if (canBuild && entry != null) {
			buildButton.setTooltip(Tooltip.create(Component.literal(entry.id() + "  " + entry.describeSize())));
		} else {
			buildButton.setTooltip(Tooltip.create(Component.translatable("gui.industrial_platform.not_enough_materials")));
		}
	}

	@Override
	protected void extractTooltip(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractTooltip(graphics, mouseX, mouseY);
		extractMaterialTooltip(graphics, mouseX, mouseY);
	}

	/**
	 * 材料槽不是菜单槽位, 悬停提示要自己画
	 */
	private void extractMaterialTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		List<MaterialDisplay> materials = materials();
		int index = materialIndexAt(mouseX, mouseY);

		if (index < 0 || index >= materials.size()) {
			return;
		}

		graphics.setTooltipForNextFrame(font, materials.get(index).tooltip(), mouseX, mouseY);
	}

	@Override
	public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		drawBlueprintList(graphics, mouseX, mouseY);
		drawMaterials(graphics);
	}

	private void drawBlueprintList(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		List<ClientBlueprintData.Entry> entries = entries();

		if (entries.isEmpty()) {
			Component hint = Component.translatable("gui.industrial_platform.blueprint_empty");
			graphics.text(font, hint, leftPos + (imageWidth - font.width(hint)) / 2, topPos + LIST_Y + 26, COLOR_HINT, false);
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

			BlueprintDisplay display = blueprintDisplays.get(index);
			graphics.text(font, display.name(), rowX + 4, rowY + 6, COLOR_LABEL, false);
			graphics.text(font, display.size(), rowX + LIST_WIDTH - display.sizeWidth() - 4, rowY + 6, COLOR_SIZE, false);
		}
	}

	private void drawMaterials(GuiGraphicsExtractor graphics) {
		List<MaterialDisplay> materials = materials();

		for (int slot = 0; slot < MATERIAL_SLOTS; slot++) {
			int index = materialScroll + slot;
			if (index >= materials.size()) {
				break;
			}

			MaterialDisplay material = materials.get(index);
			int x = leftPos + MATERIAL_X + slot * MATERIAL_STEP;
			int y = topPos + MATERIAL_Y;

			graphics.item(material.stack(), x, y);
			graphics.itemDecorations(font, material.stack(), x, y, material.count());
		}
	}

	/**
	 * 选中蓝图的材料清单(顺序固定)
	 */
	private List<MaterialDisplay> materials() {
		ClientBlueprintData.Entry entry = ClientBlueprintData.get(selectedId);
		if (entry != materialEntry) {
			materialEntry = entry;
			if (entry == null) {
				materialDisplays = List.of();
			} else {
				List<MaterialDisplay> displays = new ArrayList<>(entry.materials().size());
				for (Map.Entry<Item, Integer> material : entry.materials().entrySet()) {
					ItemStack stack = new ItemStack(material.getKey());
					Component tooltip = Component.translatable("gui.industrial_platform.blueprint_material_tip", stack.getHoverName(), material.getValue());
					displays.add(new MaterialDisplay(stack, String.valueOf(material.getValue()), tooltip));
				}
				materialDisplays = List.copyOf(displays);
			}
		}
		return materialDisplays;
	}

	private record MaterialDisplay(ItemStack stack, String count, Component tooltip) {
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
	protected void extractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		graphics.text(font, title, titleLabelX, titleLabelY, COLOR_TITLE, false);
		graphics.text(font, Component.translatable("gui.industrial_platform.fill_up"), LABEL_X, FILL_ROW_UP_Y + 5, COLOR_LABEL, false);
		graphics.text(font, Component.translatable("gui.industrial_platform.fill_down"), LABEL_X, FILL_ROW_UP_Y + FILL_ROW_HEIGHT + 5, COLOR_LABEL, false);
		graphics.text(font, Component.translatable("gui.industrial_platform.blueprint_materials"), LABEL_X, 146, COLOR_LABEL, false);


		graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, COLOR_LABEL, false);
	}
}