package me.ionar.salhack.mixin.client;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.MoverType;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends MixinAbstractClientPlayer {
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
    public void closeScreen(EntityPlayerSP entityPlayerSP) {
        // if (ModuleManager.isModuleEnabled("PortalChat"))
        // return;
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void closeScreen(Minecraft minecraft, GuiScreen screen) {
        // if (ModuleManager.isModuleEnabled("PortalChat"))
        // return;
    }

    //  public void move(MoverType type, double x, double y, double z)
    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void move(MoverType type, double x, double y, double z, CallbackInfo info) {
        EventPlayerMove event = new EventPlayerMove(type, x, y, z);
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            super.move(type, event.X, event.Y, event.Z);
            info.cancel();
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void OnPreUpdateWalkingPlayer(CallbackInfo info) {
        EventPlayerMotionUpdate event = new EventPlayerMotionUpdate(Era.PRE);
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"), cancellable = true)
    public void OnPostUpdateWalkingPlayer(CallbackInfo info) {
        EventPlayerMotionUpdate event = new EventPlayerMotionUpdate(Era.POST);
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true)
    public void onUpdate(CallbackInfo info) {
        EventPlayerUpdate event = new EventPlayerUpdate();
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    public void swingArm(EnumHand hand, CallbackInfo info) {
        EventPlayerSwingArm event = new EventPlayerSwingArm(hand);
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }

    @Inject(method = "pushOutOfBlocks(DDD)Z", at = @At("HEAD"), cancellable = true)
    public void pushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> callbackInfo) {
        EventPlayerPushOutOfBlocks event = new EventPlayerPushOutOfBlocks(x, y, z);
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            callbackInfo.setReturnValue(false);
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void swingArm(String message, CallbackInfo info) {
        EventPlayerSendChatMessage event = new EventPlayerSendChatMessage(message);
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }

    @Override
    public void jump() {
        try {
            EventPlayerJump event = new EventPlayerJump(motionX, motionZ);

            SalHackMod.EVENT_BUS.post(event);

            if (!event.isCancelled())
                super.jump();
        } catch (Exception v3) {
            v3.printStackTrace();
        }
    }

    /*@Inject(method = "displayGUIChest", at = @At("HEAD"), cancellable = true)
    public void displayGUIChest(IInventory inventory, CallbackInfo ci)
    {
        /// @todo move to events
        String id = inventory instanceof IInteractionObject ? ((IInteractionObject) inventory).getGuiID() : "minecraft:container";
        if (id.equals("minecraft:chest"))
        {
            Wrapper.GetMC().displayGuiScreen(new SalGuiChest(Wrapper.GetPlayer().inventory, inventory));
            ci.cancel();
        }
    }*/
}
