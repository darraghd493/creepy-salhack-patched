package me.ionar.salhack.mixin.client;

import io.netty.channel.ChannelHandlerContext;
import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.network.EventNetworkPostPacketEvent;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
        EventNetworkPacketEvent event = new EventNetworkPacketEvent(packet);
        SalHackMod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        EventNetworkPacketEvent event = new EventNetworkPacketEvent(packet);
        SalHackMod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("RETURN"))
    private void onPostSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
        SalHackMod.EVENT_BUS.post(new EventNetworkPostPacketEvent(packet));
    }

    @Inject(method = "channelRead0", at = @At("RETURN"))
    private void onPostChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        SalHackMod.EVENT_BUS.post(new EventNetworkPostPacketEvent(packet));
    }
}
