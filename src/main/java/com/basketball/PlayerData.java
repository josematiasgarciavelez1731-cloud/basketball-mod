package com.basketball;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class PlayerData {
    public static final Map<UUID, State> STATES = new HashMap<>();

    public static final class State {
        public boolean dribbling, flip, init;
        public int selected = -1, spinTicks;
        public long cooldownUntil, shotBonusUntil, layupBonusUntil, msgAt;
        public double speed, lx, lz;
    }

    public static State state(Player p) { return STATES.computeIfAbsent(p.getUUID(), k -> new State()); }

    // ---- datos persistentes (sobreviven a la muerte y al reinicio) ----
    private static CompoundTag tag(Player p) {
        CompoundTag root = p.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, 10)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        CompoundTag pt = root.getCompound(Player.PERSISTED_NBT_TAG);
        if (!pt.contains("basketball", 10)) pt.put("basketball", new CompoundTag());
        return pt.getCompound("basketball");
    }
    public static int points(Player p) { return tag(p).getInt("points"); }
    public static void addPoints(Player p, int n) { tag(p).putInt("points", Math.max(0, points(p) + n)); }
    public static boolean has(Player p, Move m) { return (tag(p).getInt("mask") & (1 << m.ordinal())) != 0; }
    public static void unlock(Player p, Move m) { CompoundTag t = tag(p); t.putInt("mask", t.getInt("mask") | (1 << m.ordinal())); }

    public static boolean holdingBall(Player p) {
        return p.getMainHandItem().is(BasketballMod.BALL.get()) || p.getOffhandItem().is(BasketballMod.BALL.get());
    }
    private static void msg(ServerPlayer p, Component c) { p.displayClientMessage(c, true); }

    // ---- tick del servidor: driblar, pasos, spin ----
    public static void tick(ServerPlayer p) {
        State s = state(p);
        if (s.init) s.speed = Math.hypot(p.getX() - s.lx, p.getZ() - s.lz);
        s.lx = p.getX(); s.lz = p.getZ(); s.init = true;
        boolean ball = holdingBall(p);
        if (!ball) s.dribbling = false;
        ServerLevel lv = p.serverLevel();
        ShoeTier shoe = ShoeItem.worn(p);
        if (shoe != null && shoe.jump >= 0 && p.tickCount % 5 == 0)
            p.addEffect(new MobEffectInstance(MobEffects.JUMP, 12, shoe.jump, false, false, false));
        if (ball && !p.isUsingItem()) {
            if (s.dribbling) {
                if (p.tickCount % (s.speed > 0.2 ? 7 : 10) == 0)
                    lv.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WOOD_HIT, SoundSource.PLAYERS, 0.7f, 0.7f);
            } else if (s.speed > 0.1 && p.onGround() && !p.isCreative()) {
                // "pasos": si caminas con el balon sin driblar, te frena
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6, 1, false, false, false));
                if (lv.getGameTime() - s.msgAt > 60) {
                    s.msgAt = lv.getGameTime();
                    msg(p, Component.literal("¡Pasos! Manten C para driblar").withStyle(ChatFormatting.RED));
                }
            }
        }
        if (s.spinTicks > 0) {
            double a = s.spinTicks * 0.9;
            for (int i = 0; i < 2; i++) {
                double aa = a + i * Math.PI;
                lv.sendParticles(ParticleTypes.CLOUD, p.getX() + Math.cos(aa) * 0.9, p.getY() + 0.9, p.getZ() + Math.sin(aa) * 0.9, 1, 0, 0, 0, 0.01);
            }
            s.spinTicks--;
        }
    }

    // ---- acciones que llegan por red ----
    public static void onAction(ServerPlayer p, int a) {
        State s = state(p);
        switch (a) {
            case 0 -> s.dribbling = holdingBall(p);
            case 1 -> s.dribbling = false;
            case 2 -> cycle(p, s);
            case 3 -> {
                if (!holdingBall(p) || s.selected < 0) return;
                Move m = Move.values()[s.selected];
                long now = p.level().getGameTime();
                if (!has(p, m) || now < s.cooldownUntil) return;
                ShoeTier sh = ShoeItem.worn(p);
                s.cooldownUntil = now + Math.round(25 * (1 - (sh == null ? 0 : sh.handling)));
                m.execute(p, s);
            }
            default -> {}
        }
    }

    private static void cycle(ServerPlayer p, State s) {
        Move[] all = Move.values();
        for (int i = 1; i <= all.length; i++) {
            int idx = (s.selected + i) % all.length;
            Move m = all[idx];
            if (m.active() && has(p, m)) {
                s.selected = idx;
                msg(p, Component.literal("Movimiento: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(m.label).withStyle(ChatFormatting.GOLD)));
                return;
            }
        }
        msg(p, Component.literal("Sin movimientos. Usa /basket moves").withStyle(ChatFormatting.RED));
    }
}
