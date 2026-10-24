package com.basketball.client;

import com.basketball.BasketballItem;
import com.basketball.HoopFinder;
import com.basketball.ShotLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** Medidor de tiro. Con bandeja no se muestra (no hace falta potencia). */
public class MeterOverlay implements IGuiOverlay {
    private HoopFinder.Hoop cache;
    private int last = -100;

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float pt, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || !p.isUsingItem() || !(p.getUseItem().getItem() instanceof BasketballItem)) return;
        if (p.tickCount - last >= 4) {
            cache = HoopFinder.find(p.level(), p.getEyePosition(), p.getLookAngle(), ShotLogic.RIM_RANGE, ShotLogic.AIM_DOT);
            last = p.tickCount;
        }
        double speed = Math.hypot(p.getX() - p.xo, p.getZ() - p.zo);
        if (ShotLogic.isLayup(!p.onGround(), speed, cache)) {
            g.drawCenteredString(mc.font, "BANDEJA", w / 2, h / 2 + 20, 0xFFFFAA00);
            return;
        }
        float power = Math.min(1f, p.getTicksUsingItem() / (float) BasketballItem.MAX_CHARGE);
        int bw = 80, bh = 6, x = w / 2 - bw / 2, y = h / 2 + 20;
        g.fill(x - 1, y - 1, x + bw + 1, y + bh + 1, 0xFF000000);
        g.fill(x, y, x + (int) (bw * power), y + bh, 0xFFFF8A1F);
        if (cache != null) {
            double ideal = cache.dist() / ShotLogic.MAX_RANGE;
            if (ideal <= 1.0) {
                int mx = x + (int) (bw * ideal);
                g.fill(mx - 1, y - 2, mx + 1, y + bh + 2, 0xFF30FF30);
            }
        }
    }
}
