package com.basketball;

import net.minecraft.world.level.block.Block;

/** Suelo de cancha. "bounce" = cuanto rebota el balon (parquet > goma > cemento > asfalto). */
public class CourtBlock extends Block {
    public final double bounce;
    public CourtBlock(Properties p, double bounce) { super(p); this.bounce = bounce; }
}
