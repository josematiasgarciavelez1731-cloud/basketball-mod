package com.basketball;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Net {
    static final String V = "1";
    public static final SimpleChannel CH = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BasketballMod.MODID, "main"), () -> V, V::equals, V::equals);

    public static void register() {
        CH.registerMessage(0, ActionPacket.class, ActionPacket::encode, ActionPacket::decode, ActionPacket::handle);
    }
    /** 0 = empezar a driblar, 1 = parar, 2 = cambiar movimiento, 3 = ejecutar movimiento */
    public static void send(int action) { CH.sendToServer(new ActionPacket(action)); }

    public record ActionPacket(int action) {
        public void encode(FriendlyByteBuf b) { b.writeByte(action); }
        public static ActionPacket decode(FriendlyByteBuf b) { return new ActionPacket(b.readByte()); }
        public void handle(Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> { ServerPlayer p = c.getSender(); if (p != null) PlayerData.onAction(p, action); });
            c.setPacketHandled(true);
        }
    }
}
