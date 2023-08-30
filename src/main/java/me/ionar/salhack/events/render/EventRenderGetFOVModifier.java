package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;

public class EventRenderGetFOVModifier extends MinecraftEvent {
    public float PartialTicks;
    public boolean UseFOVSetting;
    private float FOV;

    public EventRenderGetFOVModifier(float partialTicks, boolean useFOVSetting) {
        super();
        PartialTicks = partialTicks;
        UseFOVSetting = useFOVSetting;
    }

    public void SetFOV(float fOV) {
        FOV = fOV;
    }

    public float GetFOV() {
        return FOV;
    }

}
