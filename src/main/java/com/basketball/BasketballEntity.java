package com.basketball;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BasketballEntity extends ThrowableItemProjectile {
    public boolean layup;
    public double shotDist;
    private boolean scored;
    private int idle;

    public BasketballEntity(EntityType<? extends BasketballEntity> t, Level l) { super(t, l); }
    public BasketballEntity(Level l, LivingEntity owner) { super(BasketballMod.BALL_ENTITY.get(), owner, l); }

    @Override protected Item getDefaultItem() { return BasketballMod.BALL.get(); }
    @Override protected float getGravity() { return (float) ShotLogic.G; }
    @Override protected boolean canHitEntity(Entity e) { return false; }

    @Override protected void onHitBlock(BlockHitResult r) {
        super.onHitBlock(r);
        Vec3 d = getDeltaMovement();
        Direction dir = r.getDirection();
        double nx = d.x, ny = d.y, nz = d.z, e = 0.72;
        if (dir.getAxis() == Direction.Axis.Y)
            e = level().getBlockState(r.getBlockPos()).getBlock() instanceof CourtBlock cb ? cb.bounce : 0.5;
        switch (dir.getAxis()) {
            case X -> nx = -d.x * e;
            case Z -> nz = -d.z * e;
            default -> { ny = -d.y * e; if (Math.abs(ny) < 0.08) ny = 0; nx *= 0.85; nz *= 0.85; }
        }
        setDeltaMovement(nx, ny, nz);
        Vec3 loc = r.getLocation();
        setPos(loc.x + dir.getStepX() * 0.13, loc.y + dir.getStepY() * 0.13, loc.z + dir.getStepZ() * 0.13);
        if (!level().isClientSide && d.length() > 0.12)
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.WOOD_HIT, SoundSource.NEUTRAL,
                    (float) Math.min(1.0, d.length()), 0.8f);
    }

    @Override public void tick() {
        Vec3 prev = position();
        super.tick();
        if (level().isClientSide) return;
        Vec3 now = position();
        if (!scored && prev.y > now.y) checkHoops(prev, now);
        Vec3 d = getDeltaMovement();
        idle = (d.horizontalDistanceSqr() < 0.0009 && Math.abs(d.y) < 0.06) ? idle + 1 : 0;
        if (tickCount > 12) {
            for (Player pl : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(0.9, 0.6, 0.9), x -> !x.isSpectator())) {
                if (pl.getInventory().add(new ItemStack(BasketballMod.BALL.get()))) {
                    level().playSound(null, getX(), getY(), getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4f, 1.2f);
                    discard();
                    return;
                }
            }
        }
        if (idle > 40 || tickCount > 240) {
            level().addFreshEntity(new ItemEntity(level(), getX(), getY(), getZ(), new ItemStack(BasketballMod.BALL.get())));
            discard();
        } else if (getY() < level().getMinBuildHeight()) discard();
    }

    private void checkHoops(Vec3 prev, Vec3 now) {
        BlockPos n = BlockPos.containing(now);
        int y0 = Mth.floor(now.y), y1 = Mth.floor(prev.y);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) for (int y = y0; y <= y1; y++) {
            BlockPos p = new BlockPos(n.getX() + x, y, n.getZ() + z);
            BlockState s = level().getBlockState(p);
            if (!(s.getBlock() instanceof HoopBlock hb)) continue;
            double plane = p.getY() + HoopBlock.RIM_Y;
            if (!(prev.y >= plane && now.y < plane)) continue;
            double t = (prev.y - plane) / (prev.y - now.y);
            double cx = prev.x + (now.x - prev.x) * t, cz = prev.z + (now.z - prev.z) * t;
            Vec3 c = hb.rimCenter(p, s), d = getDeltaMovement();
            double dist = Math.hypot(cx - c.x, cz - c.z);
            if (dist < 0.3) {
                scored = true;
                int pts = (!layup && shotDist >= ShotLogic.THREE_DIST) ? 3 : 2;
                if (getOwner() instanceof ServerPlayer sp) ShotLogic.awardBasket(sp, pts, c, p);
                setDeltaMovement(d.x * 0.2, d.y, d.z * 0.2);
                return;
            } else if (dist < 0.48) { // rebote en el aro
                Vec3 away = new Vec3(cx - c.x, 0, cz - c.z).normalize();
                setDeltaMovement(away.x * 0.12 + d.x * 0.4, Math.abs(d.y) * 0.55, away.z * 0.12 + d.z * 0.4);
                setPos(cx, plane + 0.1, cz);
                level().playSound(null, cx, plane, cz, SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL, 1f, 1.2f);
                return;
            }
        }
    }
}
