package com.basketball;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BasketballMod.MODID)
public class ForgeEvents {
    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase == TickEvent.Phase.END && e.player instanceof ServerPlayer p) PlayerData.tick(p);
    }
    @SubscribeEvent
    public static void clone(PlayerEvent.Clone e) {
        CompoundTag old = e.getOriginal().getPersistentData();
        if (old.contains(Player.PERSISTED_NBT_TAG))
            e.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, old.getCompound(Player.PERSISTED_NBT_TAG).copy());
    }
    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp)
            sp.awardRecipes(sp.getServer().getRecipeManager().getRecipes().stream()
                    .filter(r -> r.getId().getNamespace().equals(BasketballMod.MODID)).toList());
    }
    @SubscribeEvent
    public static void trades(VillagerTradesEvent e) {
        var t = e.getTrades();
        var prof = e.getType();
        ItemStack coin = new ItemStack(BasketballMod.COIN.get());
        if (prof == VillagerProfession.LEATHERWORKER || prof == VillagerProfession.ARMORER) {
            t.get(1).add(new BasicItemListing(new ItemStack(Items.EMERALD, 2), new ItemStack(BasketballMod.COIN.get(), 3), 12, 5, 0.05f));
            t.get(2).add(new BasicItemListing(new ItemStack(BasketballMod.COIN.get(), 4), new ItemStack(Items.EMERALD, 2), 12, 5, 0.05f));
        }
        if (prof == VillagerProfession.LEATHERWORKER) {
            ShoeTier[] list = { ShoeTier.ROOKIE, ShoeTier.STREET, ShoeTier.PRO, ShoeTier.ALLSTAR };
            for (int i = 0; i < list.length; i++)
                t.get(2 + i).add(new BasicItemListing(new ItemStack(BasketballMod.COIN.get(), list[i].price),
                        new ItemStack(BasketballMod.SHOES.get(list[i]).get()), 3, 10, 0.05f));
        }
        if (prof == VillagerProfession.ARMORER)
            t.get(5).add(new BasicItemListing(new ItemStack(BasketballMod.COIN.get(), ShoeTier.LEGEND.price),
                    new ItemStack(BasketballMod.SHOES.get(ShoeTier.LEGEND).get()), 2, 20, 0.05f));
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e) { PlayerData.STATES.remove(e.getEntity().getUUID());
        if (e.getEntity() instanceof ServerPlayer sp) MatchManager.leave(sp); }
    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) { BasketCommands.register(e.getDispatcher()); }
}
