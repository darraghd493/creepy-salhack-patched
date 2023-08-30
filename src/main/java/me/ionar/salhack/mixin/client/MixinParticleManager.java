package me.ionar.salhack.mixin.client;

import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ParticleManager.class)
public class MixinParticleManager {
    /*@Inject(method = "emitParticleAtEntity", at = @At("HEAD"), cancellable = true)
    public void emitParticleAtEntity(Entity entity, EnumParticleTypes type, int amount, CallbackInfo info)
    {
        EventParticleEmitParticleAtEntity event = new EventParticleEmitParticleAtEntity(entity, type, amount);
        
        SalHackMod.EVENT_BUS.post(event);
        
        if (event.isCancelled())
            event.cancel();
    }*/
}
