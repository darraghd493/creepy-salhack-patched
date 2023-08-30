package me.ionar.salhack.mixin.client;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.render.EventRenderUpdateEquippedItem;
import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
    @Inject(method = "updateEquippedItem", at = @At("HEAD"), cancellable = true)
    public void updateEquippedItem(CallbackInfo info) {
        EventRenderUpdateEquippedItem event = new EventRenderUpdateEquippedItem();
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }
}
