package me.ionar.salhack.events.network;

import net.minecraft.network.Packet;

public class EventNetworkPostPacketEvent extends EventNetworkPacketEvent {
    public EventNetworkPostPacketEvent(Packet packet) {
        super(packet);
    }
}
