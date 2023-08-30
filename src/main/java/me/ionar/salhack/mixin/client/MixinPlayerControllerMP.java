package me.ionar.salhack.mixin.client;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.blocks.EventGetBlockReachDistance;
import me.ionar.salhack.events.player.EventPlayerClickBlock;
import me.ionar.salhack.events.player.EventPlayerDamageBlock;
import me.ionar.salhack.events.player.EventPlayerDestroyBlock;
import me.ionar.salhack.events.player.EventPlayerResetBlockRemoving;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    @Inject(method = "getBlockReachDistance", at = @At("HEAD"), cancellable = true)
    public void getBlockReachDistance(CallbackInfoReturnable<Float> callback) {
        EventGetBlockReachDistance event = new EventGetBlockReachDistance();
        SalHackMod.EVENT_BUS.post(event);
        if (event.BlockReachDistance > 0.0f) {
            callback.setReturnValue(event.BlockReachDistance);
            callback.cancel();
        }
    }

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
    public void onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> info) {
        EventPlayerDamageBlock event = new EventPlayerDamageBlock(posBlock, directionFacing);
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.setReturnValue(false);
            info.cancel();
        }
    }

    @Inject(method = "resetBlockRemoving", at = @At("HEAD"), cancellable = true)
    public void resetBlockRemoving(CallbackInfo info) {
        EventPlayerResetBlockRemoving event = new EventPlayerResetBlockRemoving();

        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method = "clickBlock", at = @At("HEAD"), cancellable = true)
    public void clickBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> callback) {
        EventPlayerClickBlock event = new EventPlayerClickBlock(loc, face);

        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            callback.setReturnValue(false);
            callback.cancel();
        }
    }

    @Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"), cancellable = true)
    public void onPlayerDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        EventPlayerDestroyBlock event = new EventPlayerDestroyBlock(pos);

        SalHackMod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}
