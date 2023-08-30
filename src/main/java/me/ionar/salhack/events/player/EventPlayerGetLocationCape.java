package me.ionar.salhack.events.player;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;

public class EventPlayerGetLocationCape extends MinecraftEvent {
    public AbstractClientPlayer Player;
    private ResourceLocation m_Location = null;

    public EventPlayerGetLocationCape(AbstractClientPlayer abstractClientPlayer) {
        super();

        Player = abstractClientPlayer;
    }

    public void SetResourceLocation(ResourceLocation location) {
        m_Location = location;
    }

    public ResourceLocation GetResourceLocation() {
        return m_Location;
    }
}
