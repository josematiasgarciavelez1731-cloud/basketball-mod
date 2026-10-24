package com.basketball;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class ShotLogic {
    public static final double G = 0.04;          // gravedad del balon
    public static final double RIM_RANGE = 16;    // alcance para buscar aro
    public static final double MAX_RANGE = 20;    // distancia que alcanza con potencia 100%
    public static final double AIM_DOT = 0.8;     // cono de apuntado (~37 grados)
    public static final double THREE_DIST = 6.8;  // distancia de triple

    /** Bandeja: corriendo o saltando, cerca del aro y mirandolo -> sin medidor de fuerza. */
    public static boolean isLayup(boolean airborne, double speed, HoopFinder.Hoop h) {
        return h != null && h.dist() <= 5.5 && (airborne || speed > 0.12);
    }

    /** Velocidad inicial para llegar a "t" en T ticks, con la fisica exacta de ThrowableProjectile. */
    public static Vec3 solve(Vec3 s, Vec3 t, double horiz) {
        int T = (int) Mth.clamp(14 + horiz, 14, 40);
        for (;; T++) {
            double d = Math.pow(0.99, T), S = (1 - d) / 0.01;
            double vy = ((t.y - s.y) + G / 0.01 * (T - S)) / S;
            double vyT = d * vy - G * (1 - d) / 0.01;
            if (vyT < -0.05 || T >= 70) return new Vec3((t.x - s.x) / S, vy, (t.z - s.z) / S);
        }
    }

    /** @return true si se consumio el balon */
    public static boolean shoot(ServerPlayer p, int charge) {
        ServerLevel lv = p.serverLevel();
        PlayerData.State st = PlayerData.state(p);
        Vec3 eye = p.getEyePosition(), look = p.getLookAngle();
        HoopFinder.Hoop h = HoopFinder.find(lv, eye, look, RIM_RANGE, AIM_DOT);
        boolean air = !p.onGround();
        boolean layup = isLayup(air, st.speed, h);
        long now = lv.getGameTime();

        // Mate
        if (h != null && air && h.dist() <= 2.6 && PlayerData.has(p, Move.DUNK) && eye.y > h.rim().y - 1.6) {
            awardBasket(p, 2, h.rim(), h.pos());
            lv.sendParticles(ParticleTypes.CRIT, h.rim().x, h.rim().y, h.rim().z, 20, 0.3, 0.2, 0.3, 0.3);
            lv.playSound(null, h.rim().x, h.rim().y, h.rim().z, SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, 1f, 0.7f);
            return false; // el balon no se gasta
        }
        if (!layup && charge < 4) return false;
        double power = Math.min(1.0, charge / (double) BasketballItem.MAX_CHARGE);

        BasketballEntity b = new BasketballEntity(lv, p);
        Vec3 start = layup
                ? new Vec3(p.getX() + look.x * 0.3, p.getY() + p.getBbHeight() + 0.35, p.getZ() + look.z * 0.3)
                : eye.add(look.scale(0.4));
        b.setPos(start);
        b.layup = layup;
        if (h != null) {
            Vec3 hd = new Vec3(h.rim().x - start.x, 0, h.rim().z - start.z);
            double d = hd.length();
            Vec3 dir = d > 1e-4 ? hd.scale(1 / d) : look;
            double err, spread;
            if (layup) { err = 0; spread = now < st.layupBonusUntil ? 0.0 : 0.09; }
            else {
                double ideal = h.dist() / MAX_RANGE;           // potencia "perfecta" para esta distancia
                err = (power - ideal) * 4.0;                   // error de potencia -> bloques de mas/menos
                spread = 0.08 + 0.012 * h.dist();
                if (now < st.shotBonusUntil) spread *= 0.4;    // step-back / fadeaway
                if (Math.abs(power - ideal) < 0.05) spread *= 0.5;
                b.shotDist = h.dist();
            }
            ShoeTier shoe = ShoeItem.worn(p);
            if (shoe != null) { spread *= (1 - shoe.accuracy); err *= (1 - shoe.accuracy * 0.5); }
            var rnd = p.getRandom();
            Vec3 target = new Vec3(h.rim().x + dir.x * err + (rnd.nextDouble() - 0.5) * 2 * spread, h.rim().y,
                    h.rim().z + dir.z * err + (rnd.nextDouble() - 0.5) * 2 * spread);
            b.setDeltaMovement(solve(start, target, d));
        } else {
            b.setDeltaMovement(look.scale(0.45 + power * 1.2).add(0, 0.12, 0));
        }
        lv.addFreshEntity(b);
        lv.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6f, 0.6f);
        st.dribbling = false;
        return true;
    }

    public static void awardBasket(ServerPlayer p, int pts, Vec3 rim, net.minecraft.core.BlockPos hoop) {
        PlayerData.addPoints(p, pts);
        String ml = MatchManager.onScore(p, pts);
        var msg = Component.literal("¡CANASTA! +" + pts).withStyle(ChatFormatting.GOLD)
                .append(Component.literal("  (" + PlayerData.points(p) + " pts)").withStyle(ChatFormatting.GRAY));
        if (ml != null) msg.append(Component.literal("  |  " + ml).withStyle(ChatFormatting.AQUA));
        p.displayClientMessage(msg, true);
        ServerLevel lv = p.serverLevel();
        lv.playSound(null, rim.x, rim.y, rim.z, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.6f);
        lv.sendParticles(ParticleTypes.FIREWORK, rim.x, rim.y, rim.z, 12, 0.2, 0.1, 0.2, 0.05);
        ScreenBlockEntity.onScore(lv, hoop, p.getGameProfile().getName(), pts, ml);
    }
}
