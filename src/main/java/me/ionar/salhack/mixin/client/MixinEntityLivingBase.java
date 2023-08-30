package me.ionar.salhack.mixin.client;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.player.EventPlayerIsPotionActive;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity {
    public MixinEntityLivingBase() {
        super();
    }

    @Shadow
    public void jump() {
    }

    @Inject(method = "isPotionActive", at = @At("HEAD"), cancellable = true)
    public void isPotionActive(Potion potionIn, final CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        EventPlayerIsPotionActive event = new EventPlayerIsPotionActive(potionIn);
        SalHackMod.EVENT_BUS.post(event);

        if (event.isCancelled())
            callbackInfoReturnable.setReturnValue(false);
    }
}
