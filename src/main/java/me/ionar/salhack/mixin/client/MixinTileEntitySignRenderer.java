package me.ionar.salhack.mixin.client;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.render.EventRenderSign;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.tileentity.TileEntitySign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntitySignRenderer.class)
public class MixinTileEntitySignRenderer {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(TileEntitySign te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo info) {
        EventRenderSign event = new EventRenderSign();
        SalHackMod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            // destroyStage = 0;
            info.cancel();
        }
    }
}
