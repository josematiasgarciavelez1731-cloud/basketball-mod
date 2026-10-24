package com.basketball;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ShoeItem extends ArmorItem {
    private static final UUID SPEED_ID = UUID.fromString("b4a5c1e2-6d7f-4a10-9c3b-5e2f8a1d7c01");
    public final ShoeTier tier;

    public ShoeItem(ShoeTier t) { super(t, ArmorItem.Type.BOOTS, new Item.Properties()); tier = t; }

    @Override public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> base = super.getDefaultAttributeModifiers(slot);
        if (slot != EquipmentSlot.FEET || tier == null) return base;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
        b.putAll(base);
        b.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(SPEED_ID, "Shoe speed", tier.speed, AttributeModifier.Operation.MULTIPLY_BASE));
        return b.build();
    }

    @Override public void appendHoverText(ItemStack s, Level l, List<Component> tip, TooltipFlag f) {
        tip.add(Component.literal("Velocidad +" + Math.round(tier.speed * 100) + "%").withStyle(ChatFormatting.GREEN));
        if (tier.jump >= 0) tip.add(Component.literal("Salto +" + (tier.jump + 1)).withStyle(ChatFormatting.GREEN));
        tip.add(Component.literal("Punteria +" + Math.round(tier.accuracy * 100) + "%").withStyle(ChatFormatting.AQUA));
        tip.add(Component.literal("Manejo +" + Math.round(tier.handling * 100) + "% (menos espera entre movimientos)").withStyle(ChatFormatting.AQUA));
    }

    public static ShoeTier worn(Player p) {
        return p.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ShoeItem si ? si.tier : null;
    }
}
