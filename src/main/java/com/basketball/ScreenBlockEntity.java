package com.basketball;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ScreenBlockEntity extends BlockEntity {
    public BlockPos hoop;
    public String last = "-", match = "";
    public int lastPts, total;

    public ScreenBlockEntity(BlockPos p, BlockState s) { super(BasketballMod.SCREEN_BE.get(), p, s); }

    @Override protected void saveAdditional(CompoundTag t) {
        super.saveAdditional(t);
        if (hoop != null) t.putLong("hoop", hoop.asLong());
        t.putString("last", last); t.putString("match", match);
        t.putInt("lastPts", lastPts); t.putInt("total", total);
    }
    @Override public void load(CompoundTag t) {
        super.load(t);
        hoop = t.contains("hoop") ? BlockPos.of(t.getLong("hoop")) : null;
        last = t.getString("last"); match = t.getString("match");
        lastPts = t.getInt("lastPts"); total = t.getInt("total");
    }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    public void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    public boolean link() { hoop = HoopFinder.nearest(level, worldPosition, 16); sync(); return hoop != null; }
    public void reset() { last = "-"; lastPts = 0; total = 0; match = ""; sync(); }

    /** Avisa a todas las pantallas conectadas a ese aro. */
    public static void onScore(ServerLevel lv, BlockPos hoop, String name, int pts, String match) {
        for (int x = -16; x <= 16; x++) for (int z = -16; z <= 16; z++) for (int y = -8; y <= 8; y++) {
            BlockPos p = hoop.offset(x, y, z);
            if (!lv.hasChunkAt(p) || !(lv.getBlockState(p).getBlock() instanceof ScreenBlock)) continue;
            if (lv.getBlockEntity(p) instanceof ScreenBlockEntity be && hoop.equals(be.hoop)) {
                be.last = name; be.lastPts = pts; be.total += pts; be.match = match == null ? "" : match;
                be.sync();
            }
        }
    }
}
