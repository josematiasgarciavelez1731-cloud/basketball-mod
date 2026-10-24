package com.basketball;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/** Zapatillas: mejor nivel = mas caras y mejores estadisticas. */
public enum ShoeTier implements ArmorMaterial {
    //        id        nombre        vel   salto punt.  manejo dur  precio
    ROOKIE ("rookie",  "Novato",     0.03, -1, 0.05, 0.05, 120, 6,  () -> Ingredient.of(Items.LEATHER)),
    STREET ("street",  "Street",     0.06, -1, 0.10, 0.10, 200, 14, () -> Ingredient.of(Items.IRON_INGOT)),
    PRO    ("pro",     "Pro",        0.09,  0, 0.18, 0.15, 350, 28, () -> Ingredient.of(Items.GOLD_INGOT)),
    ALLSTAR("allstar", "All-Star",   0.12,  0, 0.25, 0.22, 550, 48, () -> Ingredient.of(Items.DIAMOND)),
    LEGEND ("legend",  "Leyenda",    0.16,  1, 0.35, 0.30, 900, 64, () -> Ingredient.of(Items.NETHERITE_INGOT));

    public final String id, label;
    public final double speed, accuracy, handling;
    public final int jump, durability, price;
    private final Supplier<Ingredient> repair;

    ShoeTier(String id, String label, double speed, int jump, double acc, double handling, int dur, int price, Supplier<Ingredient> repair) {
        this.id = id; this.label = label; this.speed = speed; this.jump = jump; this.accuracy = acc;
        this.handling = handling; this.durability = dur; this.price = price; this.repair = repair;
    }

    @Override public int getDurabilityForType(ArmorItem.Type t) { return durability; }
    @Override public int getDefenseForType(ArmorItem.Type t) { return 1; }
    @Override public int getEnchantmentValue() { return 10; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_LEATHER; }
    @Override public Ingredient getRepairIngredient() { return repair.get(); }
    @Override public String getName() { return BasketballMod.MODID + ":" + id; }
    @Override public float getToughness() { return 0; }
    @Override public float getKnockbackResistance() { return 0; }
}
