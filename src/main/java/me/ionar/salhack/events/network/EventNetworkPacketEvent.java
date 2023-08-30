package me.ionar.salhack.events.network;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.network.Packet;

public class EventNetworkPacketEvent extends MinecraftEvent {
    public Packet m_Packet;

    public EventNetworkPacketEvent(Packet packet) {
        super();
        m_Packet = packet;
    }

    public Packet GetPacket() {
        return m_Packet;
    }

    public Packet getPacket() {
        return m_Packet;
    }
}
