package dev.celestiacraft.industrialplatform.common.block.pool;

import dev.celestiacraft.industrialplatform.IndustrialPlatform;
import dev.celestiacraft.industrialplatform.api.IPLogic;
import dev.celestiacraft.industrialplatform.common.block.platform.PlatformBlock;
import dev.celestiacraft.industrialplatform.platform.PlatformLayout;
import dev.celestiacraft.industrialplatform.platform.PoolDrops;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = IndustrialPlatform.MODID)
public class FluidPoolBlock extends Block {
	public FluidPoolBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.getHand() != InteractionHand.MAIN_HAND) {
			return;
		}
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		Player player = event.getEntity();
		ItemStack held = player.getMainHandItem();
		if (!(level.getBlockState(pos).getBlock() instanceof FluidPoolBlock) || !PlatformBlock.isMaterial(held)) {
			return;
		}
		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		StructureTemplate top = IPLogic.getStructure(serverLevel, "pool_top").orElse(null);
		StructureTemplate bottom = IPLogic.getStructure(serverLevel, "pool_bottom").orElse(null);
		if (!isValidPoolTemplate(top) || !isValidPoolTemplate(bottom)) {
			showFailure(player, "message.industrial_platform.build_failed");
			return;
		}
		int topY = pos.getY() - top.getSize().getY() + 1;
		int bottomY = topY - bottom.getSize().getY();
		if (!IPLogic.fitsBuildHeight(serverLevel, topY, top.getSize().getY())
				|| !IPLogic.fitsBuildHeight(serverLevel, bottomY, bottom.getSize().getY())) {
			showFailure(player, "message.industrial_platform.too_low");
			return;
		}

		int chunkSize = PlatformLayout.BLOCKS_PER_CHUNK;
		int x = Math.floorDiv(pos.getX(), chunkSize) * chunkSize;
		int z = Math.floorDiv(pos.getZ(), chunkSize) * chunkSize;
		Vec3 releasePosition = new Vec3(x + 0.5D, pos.getY() + 1.25D, z + 0.5D);
		try (PoolDrops drops = new PoolDrops(serverLevel, player, pos, releasePosition)) {
			if (!IPLogic.placePoolStructure(serverLevel, new BlockPos(x, topY, z), top, drops)
					|| !IPLogic.placePoolStructure(serverLevel, new BlockPos(x, bottomY, z), bottom, drops)) {
				showFailure(player, "message.industrial_platform.build_failed");
				return;
			}
		}

		player.sendOverlayMessage(Component.translatable("message.industrial_platform.pool_done").withStyle(ChatFormatting.GREEN));
		IPLogic.consumeItem(player, held, InteractionHand.MAIN_HAND);
	}

	public static boolean isValidPoolTemplate(@Nullable StructureTemplate template) {
		if (template == null) {
			return false;
		}
		Vec3i size = template.getSize();
		if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) {
			return false;
		}
		ListTag blocks = template.save(new CompoundTag()).getListOrEmpty("blocks");
		if (blocks.isEmpty()) {
			return false;
		}
		for (Tag tag : blocks) {
			if (!(tag instanceof CompoundTag block)) {
				return false;
			}
			ListTag position = block.getListOrEmpty("pos");
			if (position.size() != 3 || !(position.get(0) instanceof IntTag x)
					|| !(position.get(1) instanceof IntTag y) || !(position.get(2) instanceof IntTag z)) {
				return false;
			}
			if (x.intValue() < 0 || x.intValue() >= size.getX() || y.intValue() < 0 || y.intValue() >= size.getY()
					|| z.intValue() < 0 || z.intValue() >= size.getZ()) {
				return false;
			}
		}
		return true;
	}

	private static void showFailure(Player player, String key) {
		player.sendOverlayMessage(Component.translatable(key).withStyle(ChatFormatting.RED));
	}
}