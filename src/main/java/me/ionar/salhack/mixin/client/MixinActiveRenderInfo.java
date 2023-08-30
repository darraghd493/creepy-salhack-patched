package me.ionar.salhack.mixin.client;

import net.minecraft.client.renderer.ActiveRenderInfo;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ActiveRenderInfo.class)
public class MixinActiveRenderInfo {
    /*@Inject(method = "updateRenderInfo", at = @At("RETURN"))
    private static void updateRenderInfo(EntityPlayer entityplayerIn, boolean 74583_1_, CallbackInfo info)
    {
        RenderUtil.updateModelViewProjectionMatrix();
    }*/
}
