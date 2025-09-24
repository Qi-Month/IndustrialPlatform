package top.qm.industrialplatform.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import top.qm.industrialplatform.IPTags;
import top.qm.industrialplatform.IndustrialPlatform;
import top.qm.industrialplatform.block.BlockRegister;

import java.util.Optional;

@SuppressWarnings("ALL")
@Mod.EventBusSubscriber(modid = IndustrialPlatform.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IndustrialPlatformBlock extends Block implements SimpleWaterloggedBlock {
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public IndustrialPlatformBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Block.box(0, 0, 0, 16, 12, 16);
    }

    @SubscribeEvent
    public static void fillStructure(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);
        BlockPos pos = event.getPos();
        BlockState blockState = level.getBlockState(pos);

        if (level.isClientSide) {
            return;
        }

        // ServerLevel必须写在检测下面
        ServerLevel serverLevel = (ServerLevel) level;

        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();

        int finX = (int) Math.floor(posX / 16.0) * 16;
        int finZ = (int) Math.floor(posZ / 16.0) * 16;

        if (blockState.is(BlockRegister.INDUSTRIAL_PLATFORM.get())) {
            if (stack.is(Items.COBBLESTONE)) {
                fillArea(serverLevel, finX, posY + 1, finZ, finX + 15, posY + 11, finZ + 15);
                fillAreaConditional(serverLevel, finX, posY - 6, finZ, finX + 15, posY - 1, finZ + 15);
                placeStructure(serverLevel, finX, posY, finZ, "industrial_platform:industrial_platform/light");
                consumeItem(player, stack, hand);
            } else if (stack.is(IPTags.Items.DEEPSLATE)) {
                fillArea(serverLevel, finX - 16, posY + 1, finZ - 16, finX + 31, posY + 11, finZ + 31);
                fillAreaConditional(serverLevel, finX - 16, posY - 6, finZ - 16, finX + 31, posY - 1, finZ + 31);
                placeStructure(serverLevel, finX - 16, posY, finZ - 16, "industrial_platform:industrial_platform/heavy");
                consumeItem(player, stack, hand);
            } else if (stack.is(Items.ANDESITE)) {
                fillArea(serverLevel, finX, posY, finZ, finX + 15, posY + 18, finZ + 15);
                placeStructure(serverLevel, finX, posY, finZ, "industrial_platform:industrial_platform/levitational");
                consumeItem(player, stack, hand);
            }

            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }

    private static void fillArea(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void fillAreaConditional(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || !state.getFluidState().isEmpty()) {
                        level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
                    }
                }
            }
        }
    }

    private static void placeStructure(ServerLevel level, int x, int y, int z, String structureId) {
        StructureTemplateManager manager = level.getStructureManager();
        Optional<StructureTemplate> template = manager.get(ResourceLocation.parse(structureId));
        template.ifPresent((temp) -> {
            temp.placeInWorld(
                    level,
                    new BlockPos(x, y, z),
                    new BlockPos(x, y, z),
                    new StructurePlaceSettings()
                            .setRotation(Rotation.NONE)
                            .setMirror(Mirror.NONE)
                            .setIgnoreEntities(false),
                    level.random,
                    3
            );
        });
    }

    private static void consumeItem(Player player, ItemStack stack, InteractionHand hand) {
        player.swing(hand);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }
}