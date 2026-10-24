package com.basketball;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class HoopFinder {
    /** Aro mas cercano a una posicion (para conectar pantallas). */
    public static BlockPos nearest(Level lv, BlockPos c, int r) {
        BlockPos best = null;
        double bd = Double.MAX_VALUE;
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) for (int y = -8; y <= 8; y++) {
            BlockPos p = c.offset(x, y, z);
            if (!lv.hasChunkAt(p) || !(lv.getBlockState(p).getBlock() instanceof HoopBlock)) continue;
            double d = p.distSqr(c);
            if (d < bd) { bd = d; best = p; }
        }
        return best;
    }

    public record Hoop(BlockPos pos, Vec3 rim, double dist) {}

    /** Busca el aro que el jugador está mirando (ángulo horizontal) y al que está de frente. */
    public static Hoop find(Level lv, Vec3 from, Vec3 look, double range, double minDot) {
        BlockPos c = BlockPos.containing(from);
        double lh = Math.hypot(look.x, look.z);
        if (lh < 1e-4) return null;
        double lx = look.x / lh, lz = look.z / lh;
        int r = (int) range;
        Hoop best = null;
        double bestDot = minDot;
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) for (int y = -5; y <= 9; y++) {
            BlockPos p = c.offset(x, y, z);
            BlockState s = lv.getBlockState(p);
            if (!(s.getBlock() instanceof HoopBlock hb)) continue;
            Vec3 rim = hb.rimCenter(p, s);
            double dx = rim.x - from.x, dz = rim.z - from.z, hd = Math.hypot(dx, dz);
            if (hd > range || hd < 0.01) continue;
            Direction f = s.getValue(HoopBlock.FACING);
            if (-(dx * f.getStepX() + dz * f.getStepZ()) <= 0) continue; // hay que estar delante del tablero
            double dot = (dx * lx + dz * lz) / hd;
            if (dot > bestDot) { bestDot = dot; best = new Hoop(p, rim, hd); }
        }
        return best;
    }
}
