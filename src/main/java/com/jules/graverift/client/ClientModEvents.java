package com.jules.graverift.client;

import com.jules.graverift.GraveRiftMod;
import com.jules.graverift.client.renderer.GraveRiftRenderer;
import com.jules.graverift.init.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = GraveRiftMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.GRAVE_RIFT.get(), GraveRiftRenderer::new);
    }
}
