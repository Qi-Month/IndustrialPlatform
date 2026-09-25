package dev.celestiacraft.industrialplatform.common.menu;

import dev.celestiacraft.industrialplatform.api.PlatformSettings;
import dev.celestiacraft.industrialplatform.common.block.IPlatformController;
import dev.celestiacraft.industrialplatform.common.block.platform.PlatformBlock;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformMode;
import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformProperties;
import dev.celestiacraft.industrialplatform.common.item.FillAdjusterItem;
import dev.celestiacraft.industrialplatform.common.register.IPMenus;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import dev.celestiacraft.industrialplatform.data.PlatformSettingsStorage;
import dev.celestiacraft.industrialplatform.network.IPNetwork;
import dev.celestiacraft.industrialplatform.network.packet.BlueprintListPacket;
import dev.celestiacraft.industrialplatform.network.packet.PlatformSettingsSyncPacket;
import dev.celestiacraft.industrialplatform.platform.MaterialScanner;
import dev.celestiacraft.industrialplatform.platform.blueprint.BlueprintLibrary;
import dev.celestiacraft.industrialplatform.platform.blueprint.ClientBlueprintData;
import dev.celestiacraft.industrialplatform.platform.blueprint.PlatformBlueprint;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 平台建造站: 打开后就是蓝图列表, 选一份就能照着搭
 */
public class PlatformBuilderMenu extends AbstractContainerMenu implements IPlatformBuilderMenu {
	public static final int PANEL_WIDTH = 240;
	public static final int PANEL_HEIGHT = 290;
	public static final int INVENTORY_X = 39;
	public static final int INVENTORY_ROW_Y = PANEL_HEIGHT - 82;
	public static final int HOTBAR_Y = PANEL_HEIGHT - 24;

	private static final int DATA_UP_FILL = 0;
	private static final int DATA_DOWN_FILL = 1;
	private static final int DATA_CAN_BUILD = 2;
	private static final int DATA_SIZE = 3;
	/**
	 * 材料够不够是服务端算的, 没必要每 tick 都扫一遍容器
	 */
	private static final int BUILD_STATE_INTERVAL = 10;

	private final BlockPos controllerPos;
	private final Level level;
	private final Player player;
	private final ContainerData data;

	private String blueprintId = "";
	private int buildStateTimer;

	public PlatformBuilderMenu(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
		this(windowId, playerInventory, extraData.readBlockPos());
	}

	public PlatformBuilderMenu(int windowId, Inventory playerInventory, BlockPos controllerPos) {
		super(IPMenus.PLATFORM_BUILDER.get(), windowId);

		this.controllerPos = controllerPos;
		player = playerInventory.player;
		level = player.level();
		data = new SimpleContainerData(DATA_SIZE);

		PlatformSettings stored = readStoredSettings();
		blueprintId = stored != null && stored.hasBlueprint() ? stored.blueprintId() : "";

		data.set(DATA_UP_FILL, clampFill(stored != null ? stored.upFill() : CommonConfig.TOP_FILLING_DISTANCE.get()));
		data.set(DATA_DOWN_FILL, clampFill(stored != null ? stored.downFill() : CommonConfig.BOTTOM_FILLING_DISTANCE.get()));

		addPlayerInventory(playerInventory);
		addDataSlots(data);

		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			ensureBlueprint();
			persist();
			IPNetwork.sendToPlayer(serverPlayer, buildListPacket());
		}

		updateBuildState();
	}

	public static void open(ServerPlayer player, BlockPos controllerPos) {
		NetworkHooks.openScreen(
				player,
				new SimpleMenuProvider(
						(windowId, inventory, p) -> new PlatformBuilderMenu(windowId, inventory, controllerPos),
						Component.translatable("menu.industrial_platform.platform_builder")
				),
				controllerPos
		);
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

	private PlatformSettings readStoredSettings() {
		if (level instanceof ServerLevel serverLevel) {
			return PlatformSettingsStorage.get(serverLevel).get(controllerPos).orElse(null);
		}
		return null;
	}

	/**
	 * 存档里的蓝图已经不可用(文件被删/缺方块)时退回列表里的第一份
	 */
	private void ensureBlueprint() {
		if (BlueprintLibrary.get(blueprintId).isPresent()) {
			return;
		}

		List<PlatformBlueprint> available = BlueprintLibrary.list();
		blueprintId = available.isEmpty() ? "" : available.get(0).getId();
	}

	public Optional<PlatformBlueprint> currentBlueprint() {
		if (blueprintId.isEmpty()) {
			return Optional.empty();
		}

		return BlueprintLibrary.get(blueprintId);
	}

	public BlueprintListPacket buildListPacket() {
		List<ClientBlueprintData.Entry> entries = new ArrayList<>();

		for (PlatformBlueprint blueprint : BlueprintLibrary.list()) {
			entries.add(new ClientBlueprintData.Entry(
					blueprint.getId(),
					blueprint.sizeX(),
					blueprint.sizeY(),
					blueprint.sizeZ(),
					blueprint.getBlockCount(),
					blueprint.getMaterials()
			));
		}

		return new BlueprintListPacket(entries, blueprintId, BlueprintLibrary.unavailable());
	}

	public void selectBlueprint(String id) {
		if (!(level instanceof ServerLevel)) {
			return;
		}

		blueprintId = BlueprintLibrary.get(id).map(PlatformBlueprint::getId).orElse("");
		persist();
		updateBuildState();
	}

	@Override
	public void applySettings(int upFill, int downFill, int modeIndex) {
		data.set(DATA_UP_FILL, clampFill(upFill));
		data.set(DATA_DOWN_FILL, clampFill(downFill));

		persist();
		updateBuildState();
	}

	@Override
	public void build(ServerPlayer serverPlayer) {
		if (!(level instanceof ServerLevel serverLevel) || !stillValid(serverPlayer)) {
			return;
		}

		PlatformBlueprint blueprint = currentBlueprint().orElse(null);
		if (blueprint == null) {
			sendMessage(serverPlayer, Component.translatable("message.industrial_platform.no_blueprint").withStyle(ChatFormatting.RED));
			return;
		}

		// 创造模式不需要材料, 但界面里该显示的清单照旧显示
		Map<Item, Integer> needed = blueprint.getMaterials();

		if (!serverPlayer.isCreative()) {
			Map<Item, Integer> missing = MaterialScanner.missing(serverLevel, controllerPos, serverPlayer, needed);

			if (!missing.isEmpty()) {
				sendMessage(serverPlayer, Component.translatable("message.industrial_platform.missing_materials", describe(missing)).withStyle(ChatFormatting.RED));
				return;
			}

			if (!MaterialScanner.consume(serverLevel, controllerPos, serverPlayer, needed)) {
				sendMessage(serverPlayer, Component.translatable("message.industrial_platform.build_failed").withStyle(ChatFormatting.RED));
				return;
			}
		}

		if (!PlatformBlock.buildBlueprint(serverLevel, controllerPos, blueprint, getUpFill(), getDownFill())) {
			if (!serverPlayer.isCreative()) {
				MaterialScanner.give(serverLevel, controllerPos, serverPlayer, needed);
			}
			sendMessage(serverPlayer, Component.translatable("message.industrial_platform.build_failed").withStyle(ChatFormatting.RED));
			return;
		}

		sendMessage(serverPlayer, Component.translatable("message.industrial_platform.done").withStyle(ChatFormatting.GREEN));
		serverPlayer.closeContainer();
	}

	private Component describe(Map<Item, Integer> items) {
		StringBuilder text = new StringBuilder();

		items.forEach((item, amount) -> {
			if (text.length() > 0) {
				text.append(", ");
			}
			text.append(new ItemStack(item).getHoverName().getString()).append(" x").append(amount);
		});

		return Component.literal(text.toString());
	}

	private void sendMessage(ServerPlayer player, Component message) {
		player.displayClientMessage(message, true);
	}

	private void persist() {
		if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
			return;
		}

		PlatformSettings settings = new PlatformSettings(PlatformMode.INDUSTRIAL_LIGHT, getUpFill(), getDownFill(), blueprintId);
		PlatformSettingsStorage.get(serverLevel).put(controllerPos, settings);

		IPNetwork.sendToPlayer(serverPlayer, new PlatformSettingsSyncPacket(
				controllerPos,
				settings.mode(),
				settings.upFill(),
				settings.downFill(),
				settings.blueprintId()
		));
	}

	private void updateBuildState() {
		boolean canBuild = false;

		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			PlatformBlueprint blueprint = currentBlueprint().orElse(null);
			if (blueprint != null) {
				// 创造模式只看有没有选蓝图
				canBuild = serverPlayer.isCreative() || MaterialScanner.canAfford(serverLevel, controllerPos, serverPlayer, blueprint.getMaterials());
			}
		}

		data.set(DATA_CAN_BUILD, canBuild ? 1 : 0);
	}

	@Override
	public void broadcastChanges() {
		// 材料够不够不用每 tick 都算, 隔一会儿刷一次就够
		if (++buildStateTimer >= BUILD_STATE_INTERVAL) {
			buildStateTimer = 0;
			updateBuildState();
		}

		super.broadcastChanges();
	}

	public int getUpFill() {
		return data.get(DATA_UP_FILL);
	}

	public int getDownFill() {
		return data.get(DATA_DOWN_FILL);
	}

	public boolean canBuild() {
		return data.get(DATA_CAN_BUILD) == 1;
	}

	public String getBlueprintId() {
		return blueprintId;
	}

	public BlockPos getControllerPos() {
		return controllerPos;
	}

	public static int clampFill(int value) {
		return Mth.clamp(value, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);
	}

	@Override
	public void removed(@NotNull Player player) {
		super.removed(player);
		// 关闭界面时把界面里的上下填充格数同步给快捷栏里的每一个调节器(外加副手那只)
		FillAdjusterItem.applyToHotbar(player, getUpFill(), getDownFill());
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		if (level == null) {
			return false;
		}

		if (player.distanceToSqr(Vec3.atCenterOf(controllerPos)) > 64.0D) {
			return false;
		}

		return level.getBlockState(controllerPos).getBlock() instanceof IPlatformController;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
		return ItemStack.EMPTY;
	}
}