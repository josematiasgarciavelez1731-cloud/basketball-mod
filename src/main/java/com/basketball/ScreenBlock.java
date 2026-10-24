package com.basketball;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Pantalla de marcador: se conecta sola al aro mas cercano (16 bloques). Clic = reconectar, agachado+clic = reiniciar. */
public class ScreenBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ScreenBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c) {
        return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
    }
    @Override public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState s) { return new ScreenBlockEntity(pos, s); }

    private static void msg(Player p, String s) { if (p instanceof ServerPlayer sp) sp.displayClientMessage(Component.literal(s), true); }

    @Override public void setPlacedBy(Level l, BlockPos pos, BlockState s, LivingEntity e, ItemStack st) {
        if (!l.isClientSide && l.getBlockEntity(pos) instanceof ScreenBlockEntity be)
            msg((Player) (e instanceof Player ? e : null), be.link() ? "Pantalla conectada al aro mas cercano" : "No hay aro a menos de 16 bloques (clic derecho para reconectar)");
    }

    @Override public InteractionResult use(BlockState s, Level l, BlockPos pos, Player pl, InteractionHand h, BlockHitResult r) {
        if (!l.isClientSide && l.getBlockEntity(pos) instanceof ScreenBlockEntity be) {
            if (pl.isShiftKeyDown()) { be.reset(); msg(pl, "Marcador reiniciado"); }
            else msg(pl, be.link() ? "Pantalla conectada al aro mas cercano" : "No hay aro a menos de 16 bloques");
        }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
