package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.client.gui.ScaledResolution;

public class EventRenderGameOverlay extends MinecraftEvent {
    public float PartialTicks;
    public ScaledResolution scaledResolution;

    public EventRenderGameOverlay(float partialTicks, ScaledResolution res) {
        super();
        PartialTicks = partialTicks;
        scaledResolution = res;
    }

    public ScaledResolution getScaledResolution() {
        return scaledResolution;
    }

}
