package com.basketball.client;

import com.basketball.BasketballMod;
import com.basketball.Net;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BasketballMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    private static boolean wasDribble;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            if (wasDribble) { Net.send(1); wasDribble = false; }
            return;
        }
        boolean d = ClientSetup.DRIBBLE.isDown();
        if (d != wasDribble) { wasDribble = d; Net.send(d ? 0 : 1); }
        while (ClientSetup.CYCLE.consumeClick()) Net.send(2);
        while (ClientSetup.MOVE.consumeClick()) Net.send(3);
    }
}
