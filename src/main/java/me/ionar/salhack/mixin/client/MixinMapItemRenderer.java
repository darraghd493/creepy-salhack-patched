package me.ionar.salhack.mixin.client;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.render.EventRenderMap;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapItemRenderer.class)
public class MixinMapItemRenderer {
    @Inject(method = "renderMap", at = @At("HEAD"), cancellable = true)
    public void render(MapData mapdataIn, boolean noOverlayRendering, CallbackInfo callback) {
        EventRenderMap event = new EventRenderMap();

        SalHackMod.EVENT_BUS.post(event);

        if (event.isCancelled())
            callback.cancel();
    }
}
