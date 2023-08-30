package me.ionar.salhack.mixin.client;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.entity.EventEntityAdded;
import me.ionar.salhack.events.entity.EventEntityRemoved;
import me.ionar.salhack.events.render.EventRenderRainStrength;
import me.ionar.salhack.events.world.EventWorldSetBlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class MixinWorld {
    @Inject(method = "getRainStrength", at = @At("HEAD"), cancellable = true)
    public void getRainStrength(float delta, CallbackInfoReturnable<Float> callback) {
        EventRenderRainStrength event = new EventRenderRainStrength();

        SalHackMod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            callback.cancel();
            callback.setReturnValue(0.0f);
        }
    }

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    public void setBlockState(BlockPos pos, IBlockState newState, int flags, CallbackInfoReturnable<Boolean> callBack) {
        EventWorldSetBlockState event = new EventWorldSetBlockState(pos, newState, flags);

        SalHackMod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            callBack.cancel();
            callBack.setReturnValue(false);
        }
    }

    @Inject(method = "onEntityAdded", at = @At("HEAD"), cancellable = true)
    public void onEntityAdded(Entity entity, CallbackInfo info) {
        EventEntityAdded event = new EventEntityAdded(entity);

        SalHackMod.EVENT_BUS.post(event);

        if (event.isCancelled())
            info.cancel();
    }

    @Inject(method = "onEntityRemoved", at = @At("HEAD"), cancellable = true)
    public void onEntityRemoved(Entity entity, CallbackInfo info) {
        EventEntityRemoved event = new EventEntityRemoved(entity);

        SalHackMod.EVENT_BUS.post(event);

        if (event.isCancelled())
            info.cancel();
    }
}
