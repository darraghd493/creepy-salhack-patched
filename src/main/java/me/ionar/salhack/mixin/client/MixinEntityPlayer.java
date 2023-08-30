package me.ionar.salhack.mixin.client;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.player.EventPlayerApplyCollision;
import me.ionar.salhack.events.player.EventPlayerPushedByWater;
import me.ionar.salhack.events.player.EventPlayerTravel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityPlayer.class, priority = Integer.MAX_VALUE)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase {
    public MixinEntityPlayer() {
        super();
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(float strafe, float vertical, float forward, CallbackInfo info) {
        EventPlayerTravel event = new EventPlayerTravel(strafe, vertical, forward);
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            move(MoverType.SELF, motionX, motionY, motionZ);
            info.cancel();
        }
    }

    @Inject(method = "applyEntityCollision", at = @At("HEAD"), cancellable = true)
    public void applyEntityCollision(Entity entity, CallbackInfo info) {
        EventPlayerApplyCollision event = new EventPlayerApplyCollision(entity);
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }

    @Inject(method = "isPushedByWater()Z", at = @At("HEAD"), cancellable = true)
    public void isPushedByWater(CallbackInfoReturnable<Boolean> ci) {
        EventPlayerPushedByWater event = new EventPlayerPushedByWater();
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            ci.setReturnValue(false);
    }
}
