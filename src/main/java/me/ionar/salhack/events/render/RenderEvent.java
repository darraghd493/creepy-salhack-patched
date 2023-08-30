package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;

public class RenderEvent extends MinecraftEvent {
    private final float _partialTicks;

    public RenderEvent(float partialTicks) {
        _partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return _partialTicks;
    }
}
