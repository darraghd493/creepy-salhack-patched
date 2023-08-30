package me.ionar.salhack.mixin.client;

import com.google.common.base.Predicate;
import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.render.*;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    public void setupFog(int startCoords, float partialTicks, CallbackInfo info) {
        EventRenderSetupFog event = new EventRenderSetupFog(startCoords, partialTicks);
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, Predicate predicate) {
        EventRenderGetEntitiesINAABBexcluding event = new EventRenderGetEntitiesINAABBexcluding(worldClient, entityIn, boundingBox, predicate);
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            return new ArrayList<>();
        else
            return worldClient.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void renderHand(float partialTicks, int pass, CallbackInfo info) {
        EventRenderHand event = new EventRenderHand(partialTicks, pass);
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffect(float ticks, CallbackInfo info) {
        EventRenderHurtCameraEffect event = new EventRenderHurtCameraEffect(ticks);

        SalHackMod.EVENT_BUS.post(event);

        if (event.isCancelled())
            info.cancel();
    }

    @Inject(method = "updateLightmap", at = @At("HEAD"), cancellable = true)
    private void updateLightmap(float partialTicks, CallbackInfo info) {
        EventRenderUpdateLightmap event = new EventRenderUpdateLightmap(partialTicks);

        SalHackMod.EVENT_BUS.post(event);

        if (event.isCancelled())
            info.cancel();
    }

    @Inject(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z", shift = At.Shift.AFTER))
    private void renderWorldPassPost(int pass, float partialTicks, long finishTimeNano, CallbackInfo callbackInfo) {
        RenderUtil.updateModelViewProjectionMatrix();
    }

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"), expect = 0)
    private RayTraceResult rayTraceBlocks(WorldClient worldClient, Vec3d start, Vec3d end) {
        EventRenderOrientCamera event = new EventRenderOrientCamera();
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            return null;
        else
            return worldClient.rayTraceBlocks(start, end);
    }
}
