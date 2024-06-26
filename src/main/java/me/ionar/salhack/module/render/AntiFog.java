package me.ionar.salhack.module.render;

import me.ionar.salhack.events.render.EventRenderSetupFog;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;

public class AntiFog extends Module {
    @EventHandler
    private final Listener<EventRenderSetupFog> SetupFog = new Listener<>(event ->
    {
        event.cancel();

        mc.entityRenderer.setupFogColor(false);
        GlStateManager.glNormal3f(0.0F, -1.0F, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.colorMaterial(1028, 4608);
    });

    public AntiFog() {
        super("AntiFog", new String[]{"NoFog"}, "Prevents fog from being rendered", "NONE", 0xDB24AB, ModuleType.RENDER);
    }
}
