package com.basketball;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CourtBuilder {
    public static final Map<String, Supplier<Block>> TYPES = new LinkedHashMap<>();
    static {
        TYPES.put("parquet", BasketballMod.COURT::get);
        TYPES.put("cemento", BasketballMod.COURT_CONCRETE::get);
        TYPES.put("asfalto", BasketballMod.COURT_ASPHALT::get);
        TYPES.put("goma", BasketballMod.COURT_RUBBER::get);
    }

    private static boolean line(int x, int z) {
        int ax = Math.abs(x), az = Math.abs(z);
        if ((ax == 7 && az <= 14) || (az == 14 && ax <= 7)) return true;          // bordes
        if (z == 0) return true;                                                  // media cancha
        if (Math.round(Math.sqrt(x * x + z * z)) == 2) return true;               // circulo central
        if ((ax == 2 && az >= 9 && az <= 14) || (az == 9 && ax <= 2)) return true; // pintura
        return az < 14 && Math.round(Math.sqrt(x * x + (az - 14) * (az - 14))) == 7; // linea de triple
    }

    /** origin = bloque sobre el que se construye el suelo (justo debajo de los pies). */
    public static void build(ServerLevel lv, BlockPos o, Block floor) {
        BlockState wood = floor.defaultBlockState();
        BlockState ln = BasketballMod.COURT_LINE.get().defaultBlockState();
        BlockState edge = Blocks.POLISHED_ANDESITE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int x = -8; x <= 8; x++) for (int z = -16; z <= 16; z++) {
            boolean in = Math.abs(x) <= 7 && Math.abs(z) <= 14;
            BlockPos f = o.offset(x, 0, z);
            lv.setBlock(f, in ? (line(x, z) ? ln : wood) : edge, 2);
            for (int dy = 1; dy <= 8; dy++) lv.setBlock(f.above(dy), air, 2);
        }
        for (int sg = -1; sg <= 1; sg += 2) {
            for (int dy = 1; dy <= 4; dy++) lv.setBlock(o.offset(0, dy, 15 * sg), Blocks.GRAY_CONCRETE.defaultBlockState(), 2);
            lv.setBlock(o.offset(0, 4, 14 * sg), BasketballMod.HOOP.get().defaultBlockState()
                    .setValue(HoopBlock.FACING, sg < 0 ? Direction.SOUTH : Direction.NORTH), 2);
        }
    }
}
