package com.basketball;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

public enum Move {
    CROSSOVER("Crossover", 4),
    BETWEEN_LEGS("Entre las piernas", 8),
    SPIN("Spin move", 8),
    HESITATION("Hesitation", 10),
    BEHIND_BACK("Detras de la espalda", 12),
    STEPBACK("Step-back", 12),
    EUROSTEP("Eurostep", 16),
    FADEAWAY("Fadeaway", 16),
    DUNK("Mate (dunk)", 20); // pasivo: salta cerca del aro y suelta el balon

    public final String label;
    public final int cost;
    Move(String l, int c) { label = l; cost = c; }
    public boolean active() { return this != DUNK; }

    private static void dash(ServerPlayer p, Vec3 v, double up) {
        p.setDeltaMovement(v.x, up > 0 ? up : p.getDeltaMovement().y, v.z);
        p.hurtMarked = true;
    }

    public void execute(ServerPlayer p, PlayerData.State s) {
        double yaw = Math.toRadians(p.getYRot());
        Vec3 f = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        Vec3 r = new Vec3(-f.z, 0, f.x);
        double side = s.flip ? -1 : 1;
        s.flip = !s.flip;
        long now = p.level().getGameTime();
        switch (this) {
            case CROSSOVER -> dash(p, r.scale(0.95 * side).add(f.scale(0.25)), 0);
            case BETWEEN_LEGS -> { dash(p, f.scale(1.0), 0);
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, false, false, false)); }
            case SPIN -> { dash(p, f.scale(0.9), 0); s.spinTicks = 12;
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12, 4, false, false, false)); }
            case HESITATION -> { dash(p, f.scale(0.5), 0);
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 25, 2, false, false, false)); }
            case BEHIND_BACK -> dash(p, r.scale(1.15 * side).add(f.scale(-0.1)), 0);
            case STEPBACK -> { dash(p, f.scale(-0.95), 0.28); s.shotBonusUntil = now + 60; }
            case EUROSTEP -> { dash(p, f.scale(0.6).add(r.scale(0.8 * side)), 0.42); s.layupBonusUntil = now + 40; }
            case FADEAWAY -> { dash(p, f.scale(-0.7), 0.5); s.shotBonusUntil = now + 60; }
            default -> {}
        }
        var lv = p.serverLevel();
        lv.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.6f, 1.4f);
        lv.sendParticles(ParticleTypes.CLOUD, p.getX(), p.getY() + 0.1, p.getZ(), 8, 0.3, 0.05, 0.3, 0.02);
    }
}
