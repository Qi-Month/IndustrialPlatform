package top.qm.industrialplatform.block.custom.platform;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import top.qm.industrialplatform.IPTags;
import top.qm.industrialplatform.IndustrialPlatform;
import top.qm.industrialplatform.block.BlockRegister;
import top.qm.industrialplatform.block.state.properties.platform.PlatformMode;
import top.qm.industrialplatform.block.state.properties.platform.PlatformProperties;

import java.util.*;

@SuppressWarnings("ALL")
@Mod.EventBusSubscriber(modid = IndustrialPlatform.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IndustrialPlatformBlock extends Block implements SimpleWaterloggedBlock {

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final EnumProperty PLATFORM_MODE = PlatformProperties.PLATFORM_MODE;
    private static final BooleanProperty FLOATING = PlatformProperties.FLOATING;

    public IndustrialPlatformBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WATERLOGGED, false)
                .setValue(FLOATING, false)
                .setValue(PLATFORM_MODE, PlatformMode.INDUSTRIAL_LIGHT)
        );
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PLATFORM_MODE);
        builder.add(FLOATING);
        builder.add(WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Block.box(0, 0, 0, 16, 12, 16);
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos blockPos,
                                 Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (player.getMainHandItem().isEmpty() &&
                !level.isClientSide() &&
                hand == InteractionHand.MAIN_HAND
        ) {
            level.setBlock(blockPos, state.cycle(FLOATING), 3);
        } else if (player.getMainHandItem().is(IPTags.Items.ADJUSTERS) &&
                !level.isClientSide() &&
                hand == InteractionHand.MAIN_HAND
        ) {
            level.setBlock(blockPos, state.cycle(PLATFORM_MODE), 3);
        } else if (player.getMainHandItem().is(IPTags.Items.STONE) &&
                !level.isClientSide() &&
                hand == InteractionHand.MAIN_HAND
        ) {
            int posX = blockPos.getX();
            int posY = blockPos.getY();
            int posZ = blockPos.getZ();
            int finX = (int) Math.floor(posX / 16.0) * 16;
            int finZ = (int) Math.floor(posZ / 16.0) * 16;

            if (state.getValue(FLOATING)) {

                if (state.getValue(PLATFORM_MODE) == PlatformMode.INDUSTRIAL_LIGHT) {
                    placeStructure((ServerLevel) level, finX, posY, finZ, "industrial");
                } else if (state.getValue(PLATFORM_MODE) == PlatformMode.INDUSTRIAL_HEAVY) {
                    placeExtendedStructure((ServerLevel) level, finX, posY, finZ, "industrial");
                } else if (state.getValue(PLATFORM_MODE) == PlatformMode.CHECKERBOARD_LIGHT) {
                    placeStructure((ServerLevel) level, finX, posY, finZ, "checkerboard");
                } else if (state.getValue(PLATFORM_MODE) == PlatformMode.CHECKERBOARD_HEAVY) {
                    placeExtendedStructure((ServerLevel) level, finX, posY, finZ, "checkerboard");
                }

            } else {

                if (state.getValue(PLATFORM_MODE) == PlatformMode.INDUSTRIAL_LIGHT) {
                    fillArea((ServerLevel) level, finX, posY + 1, finZ, finX + 15, posY + 11, finZ + 15);
                    fillAreaConditional((ServerLevel) level, finX, posY - 1, finZ, finX + 15, posY - 11, finZ + 15);
                    placeStructure((ServerLevel) level, finX, posY, finZ, "industrial");
                } else if (state.getValue(PLATFORM_MODE) == PlatformMode.INDUSTRIAL_HEAVY) {
                    fillArea((ServerLevel) level, finX - 15, posY + 1, finZ - 15, finX + 31, posY + 11, finZ + 31);
                    fillAreaConditional((ServerLevel) level, finX - 15, posY - 1, finZ - 15, finX + 31, posY - 11, finZ + 31);
                    placeExtendedStructure((ServerLevel) level, finX, posY, finZ, "industrial");
                } else if (state.getValue(PLATFORM_MODE) == PlatformMode.CHECKERBOARD_LIGHT) {
                    fillArea((ServerLevel) level, finX, posY + 1, finZ, finX + 15, posY + 11, finZ + 15);
                    fillAreaConditional((ServerLevel) level, finX, posY - 1, finZ, finX + 15, posY - 11, finZ + 15);
                    placeStructure((ServerLevel) level, finX, posY, finZ, "checkerboard");
                } else if (state.getValue(PLATFORM_MODE) == PlatformMode.CHECKERBOARD_HEAVY) {
                    fillArea((ServerLevel) level, finX - 15, posY + 1, finZ - 15, finX + 31, posY + 11, finZ + 31);
                    fillAreaConditional((ServerLevel) level, finX - 15, posY - 1, finZ - 15, finX + 31, posY - 11, finZ + 31);
                    placeExtendedStructure((ServerLevel) level, finX, posY, finZ, "checkerboard");
                }

            }

            MutableComponent tranKey = Component.translatable("message.industrial_platform.done")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
            player.displayClientMessage(tranKey, true);
        }

        return super.use(state, level, blockPos, player, hand, hitResult);
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
        ResourceLocation structureName = ResourceLocation.parse("industrial_platform:industrial_platform/" + structureId);
        Optional<StructureTemplate> template = manager.get(structureName);
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

    private static void placeExtendedStructure(ServerLevel level, int x, int y, int z, String structureId) {
        StructureTemplateManager manager = level.getStructureManager();
        ResourceLocation structureName = ResourceLocation.parse("industrial_platform:industrial_platform/" + structureId);
        Optional<StructureTemplate> template = manager.get(structureName);
        for (int i = x - 16; i <= x + 16; i = i + 16) {
            for (int j = z - 16; j <= z + 16; j = j + 16) {
                int finalI = i;
                int finalJ = j;
                template.ifPresent((temp) -> {
                    temp.placeInWorld(
                            level,
                            new BlockPos(finalI, y, finalJ),
                            new BlockPos(finalI, y, finalJ),
                            new StructurePlaceSettings()
                                    .setRotation(Rotation.NONE)
                                    .setMirror(Mirror.NONE)
                                    .setIgnoreEntities(false),
                            level.random,
                            3
                    );
                });
            }
        }
    }

    private static void consumeItem(Player player, ItemStack stack, InteractionHand hand) {
        player.swing(hand);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }

    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos blockPos, PathComputationType type) {
        return false;
    }

}