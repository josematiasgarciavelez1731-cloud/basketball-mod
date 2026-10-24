package com.basketball.client;

import com.basketball.ScreenBlock;
import com.basketball.ScreenBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class ScreenRenderer implements BlockEntityRenderer<ScreenBlockEntity> {
    private final Font font;
    public ScreenRenderer(BlockEntityRendererProvider.Context c) { font = c.getFont(); }

    @Override
    public void render(ScreenBlockEntity be, float pt, PoseStack ps, MultiBufferSource buf, int light, int overlay) {
        String[] lines;
        int[] cols;
        if (be.hoop == null) {
            lines = new String[] { "SIN ARO" };
            cols = new int[] { 0xFFFF5555 };
        } else {
            String n = be.last.length() > 10 ? be.last.substring(0, 10) : be.last;
            lines = new String[] { "MARCADOR",
                    be.total == 0 ? "Sin canastas" : "Ultimo: " + n + " +" + be.lastPts,
                    "Total aro: " + be.total,
                    be.match.isEmpty() ? "Sin partido" : be.match };
            cols = new int[] { 0xFFFFAA00, 0xFFFFFFFF, 0xFF55FF55, 0xFF55FFFF };
        }
        Direction f = be.getBlockState().getValue(ScreenBlock.FACING);
        ps.pushPose();
        ps.translate(0.5, 0.5, 0.5);
        ps.mulPose(Axis.YP.rotationDegrees(-f.toYRot()));
        ps.translate(0, 0, 0.51);
        float s = 0.009f;
        ps.scale(s, -s, s);
        float y0 = -(lines.length * 14) / 2f + 2;
        for (int i = 0; i < lines.length; i++)
            font.drawInBatch(lines[i], -font.width(lines[i]) / 2f, y0 + i * 14, cols[i], false, ps.last().pose(), buf,
                    Font.DisplayMode.NORMAL, 0, 15728880);
        ps.popPose();
    }
}
