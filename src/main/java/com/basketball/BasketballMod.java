package com.basketball;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(BasketballMod.MODID)
public class BasketballMod {
    public static final String MODID = "basketball";
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<Block> HOOP = BLOCKS.register("hoop",
            () -> new HoopBlock(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.METAL).noOcclusion()));
    public static final RegistryObject<Block> COURT = BLOCKS.register("court",
            () -> new CourtBlock(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.WOOD), 0.80));
    public static final RegistryObject<Block> COURT_LINE = BLOCKS.register("court_line",
            () -> new CourtBlock(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.WOOD), 0.78));

    public static final RegistryObject<Item> BALL = ITEMS.register("basketball",
            () -> new BasketballItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> HOOP_ITEM = ITEMS.register("hoop", () -> new BlockItem(HOOP.get(), new Item.Properties()));
    public static final RegistryObject<Item> COURT_ITEM = ITEMS.register("court", () -> new BlockItem(COURT.get(), new Item.Properties()));
    public static final RegistryObject<Item> LINE_ITEM = ITEMS.register("court_line", () -> new BlockItem(COURT_LINE.get(), new Item.Properties()));

    public static final RegistryObject<Block> COURT_CONCRETE = BLOCKS.register("court_concrete",
            () -> new CourtBlock(BlockBehaviour.Properties.of().strength(2f).sound(SoundType.STONE), 0.72));
    public static final RegistryObject<Block> COURT_ASPHALT = BLOCKS.register("court_asphalt",
            () -> new CourtBlock(BlockBehaviour.Properties.of().strength(2f).sound(SoundType.STONE), 0.66));
    public static final RegistryObject<Block> COURT_RUBBER = BLOCKS.register("court_rubber",
            () -> new CourtBlock(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.WOOL), 0.75));
    public static final RegistryObject<Item> CONCRETE_ITEM = ITEMS.register("court_concrete", () -> new BlockItem(COURT_CONCRETE.get(), new Item.Properties()));
    public static final RegistryObject<Item> ASPHALT_ITEM = ITEMS.register("court_asphalt", () -> new BlockItem(COURT_ASPHALT.get(), new Item.Properties()));
    public static final RegistryObject<Item> RUBBER_ITEM = ITEMS.register("court_rubber", () -> new BlockItem(COURT_RUBBER.get(), new Item.Properties()));

    public static final RegistryObject<Block> COIN_ORE = BLOCKS.register("coin_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of().strength(3f, 3f).requiresCorrectToolForDrops().sound(SoundType.STONE), UniformInt.of(0, 2)));
    public static final RegistryObject<Item> COIN_ORE_ITEM = ITEMS.register("coin_ore", () -> new BlockItem(COIN_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> COIN = ITEMS.register("coin", () -> new Item(new Item.Properties()));

    public static final Map<ShoeTier, RegistryObject<Item>> SHOES = new EnumMap<>(ShoeTier.class);
    static { for (ShoeTier t : ShoeTier.values()) SHOES.put(t, ITEMS.register("shoes_" + t.id, () -> new ShoeItem(t))); }

    public static final DeferredRegister<BlockEntityType<?>> BES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final RegistryObject<Block> SCREEN = BLOCKS.register("scoreboard",
            () -> new ScreenBlock(BlockBehaviour.Properties.of().strength(2f).sound(SoundType.METAL).lightLevel(s -> 7)));
    public static final RegistryObject<Item> SCREEN_ITEM = ITEMS.register("scoreboard", () -> new BlockItem(SCREEN.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<ScreenBlockEntity>> SCREEN_BE = BES.register("scoreboard",
            () -> BlockEntityType.Builder.of(ScreenBlockEntity::new, SCREEN.get()).build(null));

    public static final RegistryObject<EntityType<BasketballEntity>> BALL_ENTITY = ENTITIES.register("basketball",
            () -> EntityType.Builder.<BasketballEntity>of(BasketballEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(8).updateInterval(2).build("basketball"));

    public BasketballMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        BES.register(bus);
        bus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(Net::register));
        bus.addListener((BuildCreativeModeTabContentsEvent e) -> {
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) { e.accept(BALL); e.accept(HOOP_ITEM); e.accept(SCREEN_ITEM); }
            if (e.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
                e.accept(COURT_ITEM); e.accept(CONCRETE_ITEM); e.accept(ASPHALT_ITEM); e.accept(RUBBER_ITEM); e.accept(LINE_ITEM); }
            if (e.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) e.accept(COIN_ORE_ITEM);
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) e.accept(COIN);
            if (e.getTabKey() == CreativeModeTabs.COMBAT) SHOES.values().forEach(e::accept);
        });
    }
}
