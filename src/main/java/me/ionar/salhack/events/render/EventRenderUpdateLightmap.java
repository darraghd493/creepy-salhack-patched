package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;

public class EventRenderUpdateLightmap extends MinecraftEvent {
    public float PartialTicks;

    public EventRenderUpdateLightmap(float partialTicks) {
        super();
        PartialTicks = partialTicks;
    }
}
