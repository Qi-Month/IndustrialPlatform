package dev.celestiacraft.industrialplatform.common.menu;

import dev.celestiacraft.industrialplatform.api.IPTags;
import dev.celestiacraft.industrialplatform.api.ItemMatcher;
import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.common.block.IPlatformController;
import dev.celestiacraft.industrialplatform.common.block.platform.PlatformBlock;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformProperties;
import dev.celestiacraft.industrialplatform.common.register.IPMenus;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import dev.celestiacraft.industrialplatform.data.PlatformSettingsStorage;
import dev.celestiacraft.industrialplatform.network.IPNetwork;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsSyncPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 平台搭建界面: 调整上下填充格数 / 平台样式与尺寸, 放入材料后点击搭建
 */
public class PlatformBuildMenu extends AbstractContainerMenu implements IPlatformBuilderMenu {
	public static final int PANEL_WIDTH = 200;
	public static final int PANEL_HEIGHT = 250;

	/**
	 * 材料槽在界面里的坐标(界面左上角为原点)
	 */
	public static final int MATERIAL_SLOT_INDEX = 0;
	public static final int MATERIAL_SLOT_X = 13;
	public static final int MATERIAL_SLOT_Y = 134;
	public static final int INVENTORY_X = 19;
	public static final int INVENTORY_ROW_Y = PANEL_HEIGHT - 82;
	public static final int HOTBAR_Y = PANEL_HEIGHT - 24;

	private static final int DATA_UP_FILL = 0;
	private static final int DATA_DOWN_FILL = 1;
	private static final int DATA_MODE = 2;
	private static final int DATA_CAN_BUILD = 3;
	private static final int DATA_COST = 4;
	private static final int DATA_SIZE = 5;

	/**
	 * 扣除材料时只动主背包与快捷栏(0 ~ 35), 不碰盔甲与副手
	 */
	private static final int PLAYER_INVENTORY_SIZE = 36;

	private final BlockPos platformPos;
	private final Level level;
	private final Player player;
	private final Container material = new SimpleContainer(1);
	private final ContainerData data;

	public PlatformBuildMenu(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
		this(windowId, playerInventory, extraData.readBlockPos());
	}

	public PlatformBuildMenu(int windowId, Inventory playerInventory, BlockPos platformPos) {
		super(IPMenus.PLATFORM_BUILD.get(), windowId);

		this.platformPos = platformPos;
		player = playerInventory.player;
		level = player.level();
		data = new SimpleContainerData(DATA_SIZE);

		// 上次在这个方块上选好的设置优先, 没有就用方块状态与配置默认值
		PlatformSettings stored = readStoredSettings();

		data.set(DATA_UP_FILL, clampFill(stored != null ? stored.upFill() : CommonConfig.TOP_FILLING_DISTANCE.get()));
		data.set(DATA_DOWN_FILL, clampFill(stored != null ? stored.downFill() : CommonConfig.BOTTOM_FILLING_DISTANCE.get()));
		data.set(DATA_MODE, (stored != null ? stored.mode() : readModeFromLevel()).ordinal());

		addSlot(new MaterialSlot(material, MATERIAL_SLOT_INDEX, MATERIAL_SLOT_X, MATERIAL_SLOT_Y));
		addPlayerInventory(playerInventory);
		addDataSlots(data);

		updateBuildState();
		persist();
	}

	private PlatformSettings readStoredSettings() {
		if (level instanceof ServerLevel serverLevel) {
			return PlatformSettingsStorage.get(serverLevel).get(platformPos).orElse(null);
		}
		return null;
	}

	/**
	 * 把当前设置写回方块状态与存档, 并把上下填充格数同步给客户端, 界面外的预览要用
	 */
	private void persist() {
		if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
			return;
		}

		PlatformMode mode = getMode();
		int upFill = getUpFill();
		int downFill = getDownFill();

		BlockState state = serverLevel.getBlockState(platformPos);
		if (state.getBlock() instanceof PlatformBlock) {
			BlockState updated = state
					.setValue(PlatformBlock.PLATFORM_MODE, mode)
					.setValue(PlatformBlock.FLOATING, upFill == 0 && downFill == 0);

			if (updated != state) {
				serverLevel.setBlock(platformPos, updated, 3);
			}
		}

		PlatformSettingsStorage.get(serverLevel).put(platformPos, new PlatformSettings(mode, upFill, downFill, null));
		IPNetwork.sendToPlayer(serverPlayer, new PlatformSettingsSyncPacket(platformPos, mode, upFill, downFill, null));
	}

	private void addPlayerInventory(Inventory playerInventory) {
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(new Slot(playerInventory, column + row * 9 + 9, INVENTORY_X + column * 18, INVENTORY_ROW_Y + row * 18));
			}
		}

		for (int column = 0; column < 9; column++) {
			addSlot(new Slot(playerInventory, column, INVENTORY_X + column * 18, HOTBAR_Y));
		}
	}

	private PlatformMode readModeFromLevel() {
		if (level != null) {
			BlockState state = level.getBlockState(platformPos);
			if (state.getBlock() instanceof PlatformBlock) {
				return state.getValue(PlatformProperties.PLATFORM_MODE);
			}
		}
		return PlatformMode.INDUSTRIAL_LIGHT;
	}

	public BlockPos getPlatformPos() {
		return platformPos;
	}

	public int getUpFill() {
		return data.get(DATA_UP_FILL);
	}

	public int getDownFill() {
		return data.get(DATA_DOWN_FILL);
	}

	public PlatformMode getMode() {
		return PlatformMode.byIndex(data.get(DATA_MODE));
	}

	public int getCost() {
		return data.get(DATA_COST);
	}

	public boolean canBuild() {
		return data.get(DATA_CAN_BUILD) == 1;
	}

	public ItemStack getMaterial() {
		return material.getItem(MATERIAL_SLOT_INDEX);
	}

	public static int clampFill(int value) {
		return Mth.clamp(value, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);
	}

	/**
	 * 一次搭建需要消耗的材料数量
	 */
	public static int getBuildCost(PlatformMode mode, int upFill, int downFill) {
		return CommonConfig.getBuildCost(mode, upFill, downFill);
	}

	public static boolean isValidMaterial(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		return stack.is(IPTags.Items.PLATFORM_MATERIAL) || ItemMatcher.matches(stack, CommonConfig.PLATFORM_MATERIAL);
	}

	/**
	 * 客户端调整界面设置, 由数据包调用
	 */
	public void applySettings(int upFill, int downFill, int modeIndex) {
		data.set(DATA_UP_FILL, clampFill(upFill));
		data.set(DATA_DOWN_FILL, clampFill(downFill));
		data.set(DATA_MODE, PlatformMode.byIndex(modeIndex).ordinal());

		// 立刻落盘 + 改方块状态, 外面的区块预览马上跟着变
		persist();
		updateBuildState();
	}

	/**
	 * 点击搭建按钮, 由数据包调用
	 */
	public void build(ServerPlayer serverPlayer) {
		if (!(level instanceof ServerLevel serverLevel) || !stillValid(serverPlayer)) {
			return;
		}

		ItemStack materialStack = getMaterial();
		if (materialStack.isEmpty()) {
			sendMessage(serverPlayer, Component.translatable("message.industrial_platform.no_material").withStyle(ChatFormatting.RED));
			return;
		}

		int cost = getBuildCost(getMode(), getUpFill(), getDownFill());
		if (!serverPlayer.isCreative() && countMaterial(materialStack) < cost) {
			sendMessage(serverPlayer, Component.translatable("message.industrial_platform.not_enough_material", cost).withStyle(ChatFormatting.RED));
			return;
		}

		if (!PlatformBlock.buildPlatform(serverLevel, platformPos, getMode(), getUpFill(), getDownFill())) {
			sendMessage(serverPlayer, Component.translatable("message.industrial_platform.build_failed").withStyle(ChatFormatting.RED));
			return;
		}

		if (!serverPlayer.isCreative()) {
			consumeMaterial(serverPlayer, materialStack, cost);
		}

		sendMessage(serverPlayer, Component.translatable("message.industrial_platform.done").withStyle(ChatFormatting.GREEN));
		updateBuildState();
		serverPlayer.closeContainer();
	}

	private void sendMessage(ServerPlayer player, Component message) {
		player.displayClientMessage(message, true);
	}

	/**
	 * 先扣材料槽, 再从背包里扣
	 */
	private void consumeMaterial(ServerPlayer serverPlayer, ItemStack type, int amount) {
		int remaining = amount;

		ItemStack slotStack = material.getItem(MATERIAL_SLOT_INDEX);
		if (ItemStack.isSameItemSameComponents(slotStack, type)) {
			int taken = Math.min(remaining, slotStack.getCount());
			slotStack.shrink(taken);
			remaining -= taken;
			material.setChanged();
		}

		Inventory inventory = serverPlayer.getInventory();
		for (int index = 0; index < PLAYER_INVENTORY_SIZE && remaining > 0; index++) {
			ItemStack stack = inventory.getItem(index);
			if (!ItemStack.isSameItemSameComponents(stack, type)) {
				continue;
			}
			int taken = Math.min(remaining, stack.getCount());
			stack.shrink(taken);
			remaining -= taken;
		}

		inventory.setChanged();
	}

	private int countMaterial(ItemStack type) {
		int count = ItemStack.isSameItemSameComponents(material.getItem(MATERIAL_SLOT_INDEX), type)
				? material.getItem(MATERIAL_SLOT_INDEX).getCount()
				: 0;

		Inventory inventory = player.getInventory();
		for (int index = 0; index < PLAYER_INVENTORY_SIZE; index++) {
			ItemStack stack = inventory.getItem(index);
			if (ItemStack.isSameItemSameComponents(stack, type)) {
				count += stack.getCount();
			}
		}

		return count;
	}

	private void updateBuildState() {
		int cost = getBuildCost(getMode(), getUpFill(), getDownFill());
		boolean canBuild = !getMaterial().isEmpty()
				&& (player.isCreative() || countMaterial(getMaterial()) >= cost);

		data.set(DATA_COST, cost);
		data.set(DATA_CAN_BUILD, canBuild ? 1 : 0);
	}

	@Override
	public void broadcastChanges() {
		updateBuildState();
		super.broadcastChanges();
	}

	@Override
	public void removed(@NotNull Player player) {
		super.removed(player);
		// 关闭界面时把槽里剩下的材料还给玩家
		clearContainer(player, material);
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		if (level == null) {
			return false;
		}
		if (player.distanceToSqr(Vec3.atCenterOf(platformPos)) > 64.0D) {
			return false;
		}
		return level.getBlockState(platformPos).getBlock() instanceof IPlatformController;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getItem();
		ItemStack copy = stack.copy();

		if (index == MATERIAL_SLOT_INDEX) {
			if (!moveItemStackTo(stack, MATERIAL_SLOT_INDEX + 1, slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (!moveItemStackTo(stack, MATERIAL_SLOT_INDEX, MATERIAL_SLOT_INDEX + 1, false)) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		return copy;
	}

	/**
	 * 材料槽: 只能放一个搭建材料
	 */
	private static class MaterialSlot extends Slot {
		private MaterialSlot(Container container, int index, int x, int y) {
			super(container, index, x, y);
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}

		@Override
		public boolean mayPlace(@NotNull ItemStack stack) {
			return isValidMaterial(stack);
		}
	}
}