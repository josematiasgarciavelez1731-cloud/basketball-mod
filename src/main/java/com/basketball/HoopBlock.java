package com.basketball;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Tablero + aro. Solo el tablero tiene colisión; el aro se atraviesa y se detecta en BasketballEntity. */
public class HoopBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final double RIM_Y = 3 / 16.0;

    public HoopBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c) {
        return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
    }
    @Override public BlockState rotate(BlockState s, Rotation r) { return s.setValue(FACING, r.rotate(s.getValue(FACING))); }
    @Override public VoxelShape getShape(BlockState s, BlockGetter g, BlockPos p, CollisionContext c) { return Shapes.block(); }
    @Override public VoxelShape getCollisionShape(BlockState s, BlockGetter g, BlockPos p, CollisionContext c) {
        return switch (s.getValue(FACING)) {
            case NORTH -> Block.box(0, 0, 14, 16, 14, 16);
            case SOUTH -> Block.box(0, 0, 0, 16, 14, 2);
            case EAST -> Block.box(0, 0, 0, 2, 14, 16);
            default -> Block.box(14, 0, 0, 16, 14, 16);
        };
    }
    public Vec3 rimCenter(BlockPos pos, BlockState s) {
        Direction d = s.getValue(FACING);
        return new Vec3(pos.getX() + 0.5 + d.getStepX() * 0.094, pos.getY() + RIM_Y, pos.getZ() + 0.5 + d.getStepZ() * 0.094);
    }
}
