package me.ionar.salhack.mixin.client;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.player.EventPlayerUpdateMoveState;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MovementInputFromOptions.class, priority = 10000) ///< wwe has 9999, we should be atleast 1 above
public abstract class MixinMovementInputFromOptions extends MovementInput {
    @Inject(method = "updatePlayerMoveState", at = @At("RETURN"))
    public void updatePlayerMoveStateReturn(CallbackInfo callback) {
        SalHackMod.EVENT_BUS.post(new EventPlayerUpdateMoveState());
    }
}
