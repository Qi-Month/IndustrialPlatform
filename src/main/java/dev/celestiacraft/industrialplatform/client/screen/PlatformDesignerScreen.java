package dev.celestiacraft.industrialplatform.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.client.BoundaryRenderData;
import dev.celestiacraft.industrialplatform.menu.PlatformDesignerMenu;
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
	private static final int BUILD_X = 164;
	private static final int BUILD_Y = 158;
	private static final int BUILD_WIDTH = 64;
	private static final int BUILD_HEIGHT = 20;
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

	private String selectedId = "";
	private int scroll;
	private int materialScroll;
	private boolean updatingWidgets;

	public PlatformDesignerScreen(PlatformDesignerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);

		this.imageWidth = PlatformDesignerMenu.PANEL_WIDTH;
		this.imageHeight = PlatformDesignerMenu.PANEL_HEIGHT;
		this.titleLabelX = 8;
		this.titleLabelY = 6;
		this.inventoryLabelX = PlatformDesignerMenu.INVENTORY_X;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	protected void init() {
		super.init();

		this.selectedId = ClientBlueprintData.getSelected();
		if (this.selectedId.isEmpty()) {
			this.selectedId = this.menu.getBlueprintId();
		}
		this.scroll = Math.max(0, indexOf(this.selectedId) - 1);

		this.upFillField = this.createFillField(this.topPos + FILL_ROW_UP_Y, this.menu.getUpFill(), this::onUpFillTyped);
		this.downFillField = this.createFillField(this.topPos + FILL_ROW_UP_Y + FILL_ROW_HEIGHT, this.menu.getDownFill(), this::onDownFillTyped);

		this.upMinus = this.addRenderableWidget(this.createStepButton(MINUS_X, FILL_ROW_UP_Y, "-", (button) -> {
			this.setUpFill(this.menu.getUpFill() - 1);
		}));
		this.upPlus = this.addRenderableWidget(this.createStepButton(PLUS_X, FILL_ROW_UP_Y, "+", (button) -> this.setUpFill(this.menu.getUpFill() + 1)));
		this.downMinus = this.addRenderableWidget(this.createStepButton(MINUS_X, FILL_ROW_UP_Y + FILL_ROW_HEIGHT, "-", (button) -> {
			this.setDownFill(this.menu.getDownFill() - 1);
		}));
		this.downPlus = this.addRenderableWidget(this.createStepButton(PLUS_X, FILL_ROW_UP_Y + FILL_ROW_HEIGHT, "+", (button) -> {
			this.setDownFill(this.menu.getDownFill() + 1);
		}));

		this.addRenderableWidget(this.upFillField);
		this.addRenderableWidget(this.downFillField);

		this.addRenderableWidget(Button.builder(Component.literal("↑"), (button) -> {
					this.scrollBy(-1);
				})
				.bounds(this.leftPos + SCROLL_X, this.topPos + SCROLL_UP_Y, SCROLL_SIZE, SCROLL_SIZE)
				.build());
		this.addRenderableWidget(Button.builder(Component.literal("↓"), (button) -> {
					this.scrollBy(1);
				})
				.bounds(this.leftPos + SCROLL_X, this.topPos + SCROLL_DOWN_Y, SCROLL_SIZE, SCROLL_SIZE)
				.build());

		this.buildButton = this.addRenderableWidget(
				Button.builder(Component.translatable("gui.industrial_platform.build_plain"), (button) -> {
							IPNetwork.sendToServer(new PlatformBuildPacket());
						})
						.bounds(this.leftPos + BUILD_X, this.topPos + BUILD_Y, BUILD_WIDTH, BUILD_HEIGHT)
						.build());

		this.updateBuildButton();
	}

	private Button createStepButton(int x, int y, String label, Button.OnPress onPress) {
		return Button.builder(Component.literal(label), onPress)
				.bounds(this.leftPos + x, this.topPos + y, STEP_SIZE, STEP_SIZE)
				.build();
	}

	private EditBox createFillField(int y, int value, java.util.function.Consumer<String> responder) {
		EditBox field = new EditBox(this.font, this.leftPos + FIELD_X, y + 1, FIELD_WIDTH, FIELD_HEIGHT, Component.empty()) {
			@Override
			public void setFocused(boolean focused) {
				boolean wasFocused = this.isFocused();
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

		this.updatingWidgets = true;
		field.setValue(String.valueOf(value));
		this.updatingWidgets = false;

		return field;
	}

	private void onUpFillTyped(String text) {
		if (this.updatingWidgets) {
			return;
		}

		int parsed = parseFill(text, this.menu.getUpFill());
		if (parsed != this.menu.getUpFill()) {
			this.sendFill(parsed, this.menu.getDownFill());
		}
	}

	private void onDownFillTyped(String text) {
		if (this.updatingWidgets) {
			return;
		}

		int parsed = parseFill(text, this.menu.getDownFill());
		if (parsed != this.menu.getDownFill()) {
			this.sendFill(this.menu.getUpFill(), parsed);
		}
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
		this.sendFill(PlatformDesignerMenu.clampFill(value), this.menu.getDownFill());
	}

	private void setDownFill(int value) {
		this.sendFill(this.menu.getUpFill(), PlatformDesignerMenu.clampFill(value));
	}

	private void sendFill(int upFill, int downFill) {
		IPNetwork.sendToServer(new PlatformSettingsPacket(upFill, downFill, 0));
	}

	private void normalizeFields() {
		this.updatingWidgets = true;
		this.upFillField.setValue(String.valueOf(this.menu.getUpFill()));
		this.downFillField.setValue(String.valueOf(this.menu.getDownFill()));
		this.updatingWidgets = false;
	}

	private List<ClientBlueprintData.Entry> entries() {
		return ClientBlueprintData.list();
	}

	private int maxScroll() {
		return Math.max(0, this.entries().size() - LIST_ROWS);
	}

	private int indexOf(String id) {
		List<ClientBlueprintData.Entry> entries = this.entries();
		for (int index = 0; index < entries.size(); index++) {
			if (entries.get(index).id().equals(id)) {
				return index;
			}
		}
		return 0;
	}

	private void scrollBy(int delta) {
		this.scroll = Mth.clamp(this.scroll + delta, 0, this.maxScroll());
	}

	private void select(String id) {
		if (id.equals(this.selectedId)) {
			return;
		}

		this.selectedId = id;
		ClientBlueprintData.setSelected(id);
		IPNetwork.sendToServer(new DesignerSelectPacket(id));
		this.updateBuildButton();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (delta != 0.0D) {
			if (this.isOverList(mouseX, mouseY)) {
				this.scrollBy(delta > 0.0D ? -1 : 1);
				return true;
			}

			if (this.isOverMaterials(mouseX, mouseY) && this.maxMaterialScroll() > 0) {
				this.materialScroll = Mth.clamp(this.materialScroll + (delta > 0.0D ? -1 : 1), 0, this.maxMaterialScroll());
				return true;
			}

			if (this.isOverFillControls(this.upFillField, this.upMinus, this.upPlus, mouseX, mouseY)) {
				this.setUpFill(this.menu.getUpFill() + (delta > 0.0D ? 1 : -1));
				return true;
			}

			if (this.isOverFillControls(this.downFillField, this.downMinus, this.downPlus, mouseX, mouseY)) {
				this.setDownFill(this.menu.getDownFill() + (delta > 0.0D ? 1 : -1));
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
		double localX = mouseX - this.leftPos;
		double localY = mouseY - this.topPos;

		return localX >= LIST_X && localX < LIST_X + LIST_WIDTH && localY >= LIST_Y && localY < LIST_Y + LIST_ROWS * LIST_ROW_HEIGHT;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && this.isOverList(mouseX, mouseY)) {
			int row = (int) ((mouseY - this.topPos - LIST_Y) / LIST_ROW_HEIGHT);
			int index = this.scroll + row;
			List<ClientBlueprintData.Entry> entries = this.entries();

			if (index >= 0 && index < entries.size()) {
				this.select(entries.get(index).id());
				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void containerTick() {
		super.containerTick();

		this.scroll = Mth.clamp(this.scroll, 0, this.maxScroll());
		this.materialScroll = Mth.clamp(this.materialScroll, 0, this.maxMaterialScroll());
		this.updateBuildButton();
		this.updatePreview();
	}

	private void updatePreview() {
		ClientBlueprintData.Entry entry = ClientBlueprintData.get(this.selectedId);
		if (entry == null) {
			BoundaryRenderData.clearOverride();
			return;
		}

		BoundaryRenderData.setOverride(new BoundaryRenderData.BoundaryEntry(
				this.menu.getControllerPos(),
				entry.sizeX(),
				entry.sizeZ(),
				this.menu.getUpFill() == 0 && this.menu.getDownFill() == 0,
				this.menu.getUpFill(),
				this.menu.getDownFill()
		));
	}

	@Override
	public void removed() {
		super.removed();
		BoundaryRenderData.clearOverride();
	}

	private void updateBuildButton() {
		if (this.buildButton == null) {
			return;
		}

		boolean canBuild = this.menu.canBuild();
		this.buildButton.active = canBuild;

		ClientBlueprintData.Entry entry = ClientBlueprintData.get(this.selectedId);

		if (canBuild && entry != null) {
			this.buildButton.setTooltip(Tooltip.create(Component.literal(entry.id() + "  " + entry.describeSize())));
		} else {
			this.buildButton.setTooltip(Tooltip.create(Component.translatable("gui.industrial_platform.not_enough_materials")));
		}
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// 原版那种打开容器时压暗背景
		this.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTick);

		int index = this.materialIndexAt(mouseX, mouseY);
		if (index < 0) {
			return;
		}

		List<Map.Entry<Item, Integer>> materials = this.materials();
		if (index >= materials.size()) {
			return;
		}

		Map.Entry<Item, Integer> material = materials.get(index);
		graphics.renderTooltip(this.font, Component.translatable(
				"gui.industrial_platform.blueprint_material_tip",
				new ItemStack(material.getKey()).getHoverName(),
				material.getValue()
		), mouseX, mouseY);
	}

	@Override
	protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		this.drawBlueprintList(graphics, mouseX, mouseY);
		this.drawMaterials(graphics);
	}

	private void drawBlueprintList(GuiGraphics graphics, int mouseX, int mouseY) {
		List<ClientBlueprintData.Entry> entries = this.entries();

		if (entries.isEmpty()) {
			Component hint = Component.translatable("gui.industrial_platform.blueprint_empty");
			graphics.drawString(this.font, hint, this.leftPos + (this.imageWidth - this.font.width(hint)) / 2, this.topPos + LIST_Y + 26, COLOR_HINT, false);
			return;
		}

		for (int row = 0; row < LIST_ROWS; row++) {
			int index = this.scroll + row;
			if (index >= entries.size()) {
				break;
			}

			ClientBlueprintData.Entry entry = entries.get(index);
			int rowX = this.leftPos + LIST_X;
			int rowY = this.topPos + LIST_Y + row * LIST_ROW_HEIGHT;
			boolean selected = entry.id().equals(this.selectedId);
			boolean hovered = mouseX >= rowX && mouseX < rowX + LIST_WIDTH && mouseY >= rowY && mouseY < rowY + LIST_ROW_HEIGHT - 2;

			if (selected) {
				graphics.fill(rowX, rowY, rowX + LIST_WIDTH, rowY + LIST_ROW_HEIGHT - 2, COLOR_SELECTED);
			} else if (hovered) {
				graphics.fill(rowX, rowY, rowX + LIST_WIDTH, rowY + LIST_ROW_HEIGHT - 2, COLOR_HOVER);
			}

			String name = entry.id();
			int maxNameWidth = LIST_WIDTH - this.font.width(entry.describeSize()) - 16;
			if (this.font.width(name) > maxNameWidth) {
				name = this.font.plainSubstrByWidth(name, maxNameWidth - 6) + "...";
			}

			graphics.drawString(this.font, name, rowX + 4, rowY + 6, COLOR_LABEL, false);

			String size = entry.describeSize();
			graphics.drawString(this.font, size, rowX + LIST_WIDTH - this.font.width(size) - 4, rowY + 6, COLOR_SIZE, false);
		}
	}

	private void drawMaterials(GuiGraphics graphics) {
		List<Map.Entry<Item, Integer>> materials = this.materials();

		for (int slot = 0; slot < MATERIAL_SLOTS; slot++) {
			int index = this.materialScroll + slot;
			if (index >= materials.size()) {
				break;
			}

			Map.Entry<Item, Integer> material = materials.get(index);
			int x = this.leftPos + MATERIAL_X + slot * MATERIAL_STEP;
			int y = this.topPos + MATERIAL_Y;

			ItemStack stack = new ItemStack(material.getKey(), Math.min(material.getValue(), 64));
			graphics.renderItem(stack, x, y);
			graphics.renderItemDecorations(this.font, stack, x, y, String.valueOf(material.getValue()));
		}
	}

	/**
	 * 选中蓝图的材料清单(顺序固定)
	 */
	private List<Map.Entry<Item, Integer>> materials() {
		ClientBlueprintData.Entry entry = ClientBlueprintData.get(this.selectedId);
		if (entry == null) {
			return List.of();
		}

		return new ArrayList<>(entry.materials().entrySet());
	}

	private int maxMaterialScroll() {
		return Math.max(0, this.materials().size() - MATERIAL_SLOTS);
	}

	/**
	 * 鼠标底下是第几个材料槽, 不在材料行上就返回 -1
	 */
	private int materialIndexAt(double mouseX, double mouseY) {
		double localX = mouseX - this.leftPos;
		double localY = mouseY - this.topPos;

		if (localY < MATERIAL_Y || localY >= MATERIAL_Y + 18) {
			return -1;
		}

		for (int slot = 0; slot < MATERIAL_SLOTS; slot++) {
			double slotX = MATERIAL_X + slot * MATERIAL_STEP;
			if (localX >= slotX && localX < slotX + 18) {
				int index = this.materialScroll + slot;
				return index < this.materials().size() ? index : -1;
			}
		}

		return -1;
	}

	private boolean isOverMaterials(double mouseX, double mouseY) {
		return this.materialIndexAt(mouseX, mouseY) >= 0;
	}

	@Override
	protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, COLOR_TITLE, false);
		graphics.drawString(this.font, Component.translatable("gui.industrial_platform.fill_up"), LABEL_X, FILL_ROW_UP_Y + 5, COLOR_LABEL, false);
		graphics.drawString(this.font, Component.translatable("gui.industrial_platform.fill_down"), LABEL_X, FILL_ROW_UP_Y + FILL_ROW_HEIGHT + 5, COLOR_LABEL, false);
		graphics.drawString(this.font, Component.translatable("gui.industrial_platform.blueprint_materials"), LABEL_X, 146, COLOR_LABEL, false);

		int materialCount = this.materials().size();
		if (materialCount > MATERIAL_SLOTS) {
			Component pages = Component.translatable("gui.industrial_platform.blueprint_pages", this.materialScroll + 1, this.materialScroll + MATERIAL_SLOTS, materialCount);
			graphics.drawString(this.font, pages, MATERIAL_X + MATERIAL_SLOTS * MATERIAL_STEP + 4, MATERIAL_Y + 5, COLOR_HINT, false);
		}
		graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, COLOR_LABEL, false);
	}
}