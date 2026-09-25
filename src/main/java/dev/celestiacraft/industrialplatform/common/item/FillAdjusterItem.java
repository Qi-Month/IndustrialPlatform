package dev.celestiacraft.industrialplatform.common.item;

import dev.celestiacraft.industrialplatform.common.block.state.properties.platform.PlatformProperties;
import dev.celestiacraft.industrialplatform.config.CommonConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 填充调节器: 站立时什么也不接管, 任何操作都保持原版逻辑
 * (滚轮照常切快捷栏, 右键照常交给方块)。
 * <p>
 * 潜行时才由调节器接管: 潜行右键在"向上填充"与"向下填充"之间切换当前调节目标,
 * 潜行滚动滚轮调节当前选中的那一项, 按住 Ctrl 一次 10 格。
 * <p>
 * 上下填充格数与当前目标都存在物品自己的数据组件里(1.21 用 DataComponent 取代了原来的 ItemStack NBT)。
 */
public class FillAdjusterItem extends Item {
	private static final String UP_KEY = "UpFill";
	private static final String DOWN_KEY = "DownFill";
	private static final String TARGET_KEY = "FillTarget";

	public FillAdjusterItem() {
		super(new Item.Properties().stacksTo(1));
	}

	/**
	 * 当前正在调节的目标: 潜行右键在两个值之间来回切
	 */
	public enum Target {
		UP,
		DOWN;

		private static final Target[] VALUES = values();

		/**
		 * 切换到的下一个目标
		 */
		public Target next() {
			return this == UP ? DOWN : UP;
		}

		/**
		 * 提示里显示的名字, 复用搭建界面那套"向上填充 / 向下填充"
		 */
		public Component displayName() {
			return Component.translatable(this == UP ? "gui.industrial_platform.fill_up" : "gui.industrial_platform.fill_down");
		}

		/**
		 * 存档 / 网络读取时的安全取值
		 */
		public static Target byIndex(int index) {
			return VALUES[Mth.clamp(index, 0, VALUES.length - 1)];
		}
	}

	/**
	 * 读物品自带的自定义数据组件, 空物品返回空 tag
	 */
	private static CompoundTag readTag(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	public static int getUpFill(ItemStack stack) {
		CompoundTag tag = readTag(stack);
		if (!tag.contains(UP_KEY)) {
			return Mth.clamp(CommonConfig.TOP_FILLING_DISTANCE.get(), PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);
		}

		return Mth.clamp(tag.getInt(UP_KEY), PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);
	}

	public static int getDownFill(ItemStack stack) {
		CompoundTag tag = readTag(stack);
		if (!tag.contains(DOWN_KEY)) {
			return Mth.clamp(CommonConfig.BOTTOM_FILLING_DISTANCE.get(), PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);
		}

		return Mth.clamp(tag.getInt(DOWN_KEY), PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);
	}

	public static void setFills(ItemStack stack, int upFill, int downFill) {
		if (stack.isEmpty()) {
			return;
		}

		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
			tag.putInt(UP_KEY, Mth.clamp(upFill, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE));
			tag.putInt(DOWN_KEY, Mth.clamp(downFill, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE));
		});
	}

	/**
	 * 玩家手上的填充调节器, 主手优先(滚轮改的也是主手那份)
	 */
	public static ItemStack findHeld(Player player) {
		ItemStack main = player.getMainHandItem();
		if (main.getItem() instanceof FillAdjusterItem) {
			return main;
		}

		ItemStack off = player.getOffhandItem();
		return off.getItem() instanceof FillAdjusterItem ? off : ItemStack.EMPTY;
	}

	/**
	 * 把这两个数值写回玩家快捷栏里的每一个调节器(外加副手那只)
	 * <p>
	 * 快捷栏就是物品栏 0 ~ {@link Inventory#getSelectionSize()} - 1 那几格, 手里正拿着的那只也在其中。
	 *
	 * @return 实际改动的数量
	 */
	public static int applyToHotbar(Player player, int upFill, int downFill) {
		Inventory inventory = player.getInventory();
		int changed = 0;

		for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
			if (applyTo(inventory.getItem(slot), upFill, downFill)) {
				changed++;
			}
		}

		if (applyTo(player.getOffhandItem(), upFill, downFill)) {
			changed++;
		}

		if (changed > 0) {
			inventory.setChanged();
		}

		return changed;
	}

	/**
	 * 只写调节器, 别的物品原样跳过; 数值已经一致就不写, 免得白标一次脏
	 */
	private static boolean applyTo(ItemStack stack, int upFill, int downFill) {
		if (!(stack.getItem() instanceof FillAdjusterItem)) {
			return false;
		}

		if (getUpFill(stack) == upFill && getDownFill(stack) == downFill) {
			return false;
		}

		setFills(stack, upFill, downFill);

		return true;
	}

	/**
	 * 当前选中的调节目标, 没存过就是向上填充
	 */
	public static Target getTarget(ItemStack stack) {
		CompoundTag tag = readTag(stack);
		if (!tag.contains(TARGET_KEY)) {
			return Target.UP;
		}

		return Target.byIndex(tag.getInt(TARGET_KEY));
	}

	public static void setTarget(ItemStack stack, Target target) {
		if (stack.isEmpty()) {
			return;
		}

		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(TARGET_KEY, target.ordinal()));
	}

	/**
	 * 切换到下一个目标
	 *
	 * @return 切换之后的目标
	 */
	public static Target cycleTarget(ItemStack stack) {
		Target target = getTarget(stack).next();
		setTarget(stack, target);

		return target;
	}

	/**
	 * 读当前选中目标的那一项数值
	 */
	public static int getSelectedFill(ItemStack stack, Target target) {
		return target == Target.UP ? getUpFill(stack) : getDownFill(stack);
	}

	/**
	 * 只动当前选中的那一项, 另一项保持原样
	 *
	 * @return 调完之后选中项的数值
	 */
	public static int adjust(ItemStack stack, Target target, int step) {
		int value = Mth.clamp(getSelectedFill(stack, target) + step, PlatformProperties.MIN_FILL_DISTANCE, PlatformProperties.MAX_FILL_DISTANCE);

		if (target == Target.UP) {
			setFills(stack, value, getDownFill(stack));
		} else {
			setFills(stack, getUpFill(stack), value);
		}

		return value;
	}

	/**
	 * 对空气右键: 只有潜行才归调节器管, 站立时交还原版
	 */
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!player.isShiftKeyDown() || !toggleTarget(level, player, stack)) {
			return InteractionResultHolder.pass(stack);
		}

		return InteractionResultHolder.success(stack);
	}

	/**
	 * 对方块右键: 站立时交给方块(平台方块 / 建造站照旧), 潜行时切换调节目标
	 */
	@Override
	public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
		Player player = context.getPlayer();

		if (player == null || !player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}

		return toggleTarget(context.getLevel(), player, context.getItemInHand()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

	/**
	 * 切换目标并提示, 两端都从同一份组件数据出发, 所以各自翻一次的结果是一致的
	 *
	 * @return 是否真的切了
	 */
	private static boolean toggleTarget(Level level, Player player, ItemStack stack) {
		if (!(stack.getItem() instanceof FillAdjusterItem)) {
			return false;
		}

		Target target = cycleTarget(stack);

		// 提示只在服务端发, 免得两边各显示一次
		if (!level.isClientSide()) {
			player.displayClientMessage(Component.translatable(
					"message.industrial_platform.fill_values",
					target.displayName(),
					getSelectedFill(stack, target)
			).withStyle(ChatFormatting.AQUA), true);
		}

		return true;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		Target target = getTarget(stack);

		tooltip.add(Component.translatable("tooltip.industrial_platform.fill_adjuster.up", getUpFill(stack))
				.withStyle(target == Target.UP ? ChatFormatting.AQUA : ChatFormatting.GRAY));
		tooltip.add(Component.translatable("tooltip.industrial_platform.fill_adjuster.down", getDownFill(stack))
				.withStyle(target == Target.DOWN ? ChatFormatting.AQUA : ChatFormatting.GRAY));
		tooltip.add(Component.translatable("tooltip.industrial_platform.fill_adjuster.target", target.displayName()).withStyle(ChatFormatting.YELLOW));
		tooltip.add(Component.translatable("tooltip.industrial_platform.fill_adjuster.switch").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("tooltip.industrial_platform.fill_adjuster.usage").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("tooltip.industrial_platform.fill_adjuster.deploy").withStyle(ChatFormatting.GRAY));
	}
}
