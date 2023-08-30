package me.ionar.salhack.events.salhack;

import me.ionar.salhack.events.MinecraftEvent;
import me.ionar.salhack.module.Module;

public class EventSalHackModule extends MinecraftEvent {
    public final Module Mod;

    public EventSalHackModule(final Module mod1) {
        super();
        Mod = mod1;
    }
}
