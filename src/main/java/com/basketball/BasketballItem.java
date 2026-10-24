package com.basketball;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Mantener clic derecho = cargar el tiro; soltar = tirar. */
public class BasketballItem extends Item {
    public static final int MAX_CHARGE = 30; // ticks (1.5 s) para potencia 100%

    public BasketballItem(Properties p) { super(p); }

    @Override public InteractionResultHolder<ItemStack> use(Level l, Player p, InteractionHand h) {
        p.startUsingItem(h);
        return InteractionResultHolder.consume(p.getItemInHand(h));
    }
    @Override public UseAnim getUseAnimation(ItemStack s) { return UseAnim.BOW; }
    @Override public int getUseDuration(ItemStack s) { return 72000; }

    @Override public void releaseUsing(ItemStack stack, Level l, LivingEntity e, int timeLeft) {
        if (l.isClientSide || !(e instanceof ServerPlayer p)) return;
        int charge = getUseDuration(stack) - timeLeft;
        if (ShotLogic.shoot(p, charge) && !p.getAbilities().instabuild) stack.shrink(1);
    }
}
