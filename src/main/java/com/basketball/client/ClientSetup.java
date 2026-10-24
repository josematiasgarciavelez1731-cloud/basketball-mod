package com.basketball.client;

import com.basketball.BasketballMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = BasketballMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    public static final String CAT = "key.categories.basketball";
    public static final KeyMapping DRIBBLE = new KeyMapping("key.basketball.dribble", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CAT);
    public static final KeyMapping CYCLE = new KeyMapping("key.basketball.cycle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CAT);
    public static final KeyMapping MOVE = new KeyMapping("key.basketball.move", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CAT);

    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e) { e.register(DRIBBLE); e.register(CYCLE); e.register(MOVE); }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(BasketballMod.BALL_ENTITY.get(), ctx -> new ThrownItemRenderer<>(ctx, 1.4f, true));
        e.registerBlockEntityRenderer(BasketballMod.SCREEN_BE.get(), ScreenRenderer::new);
    }
    @SubscribeEvent public static void overlays(RegisterGuiOverlaysEvent e) { e.registerAboveAll("shot_meter", new MeterOverlay()); }
}
