package me.ionar.salhack.events.player;

import me.ionar.salhack.events.MinecraftEvent;
import me.ionar.salhack.mixin.client.MixinAbstractClientPlayer;
import net.minecraft.util.ResourceLocation;

public class EventPlayerGetLocationSkin extends MinecraftEvent {
    public MixinAbstractClientPlayer Player;
    private ResourceLocation m_Location = null;

    public EventPlayerGetLocationSkin(MixinAbstractClientPlayer player) {
        super();

        Player = player;
    }

    public void SetResourceLocation(ResourceLocation location) {
        m_Location = location;
    }

    public ResourceLocation GetResourceLocation() {
        return m_Location;
    }
}
