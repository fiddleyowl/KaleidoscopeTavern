package com.github.ysbbbbbb.kaleidoscopetavern.block.mixology;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class GlasswareBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 10, 12);

    private static final MapCodec<GlasswareBlock> CODEC = simpleCodec(GlasswareBlock::new);

    public GlasswareBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    public GlasswareBlock(Identifier id) {
        this(Properties.of().setId(ResourceKey.create(Registries.BLOCK, id))
                .noOcclusion()
                .instabreak()
                .pushReaction(PushReaction.DESTROY)
                .sound(SoundType.GLASS));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        // 如果是空手，那么可以尝试取回
        if (!player.getItemInHand(hand).isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel serverLevel) {
            getDrops(state, serverLevel, pos, level.getBlockEntity(pos))
                    .forEach(s -> ItemHandlerHelper.giveItemToPlayer(player, s));
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_ALL);
            level.playSound(null, pos, SoundType.STONE.getPlaceSound(), player.getSoundSource(), 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level, net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean isWaterAt = context.getLevel().isWaterAt(context.getClickedPos());
        Direction direction = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState()
                .setValue(FACING, direction)
                .setValue(WATERLOGGED, isWaterAt);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide()) {
            BlockPos pos = hit.getBlockPos();
            if (level instanceof ServerLevel serverLevel && projectile.mayInteract(serverLevel, pos)) {
                level.removeBlock(pos, false);
                // 播放玻璃的粒子效果
                int id = Block.getId(Blocks.GLASS.defaultBlockState());
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, id);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 1.0F;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
