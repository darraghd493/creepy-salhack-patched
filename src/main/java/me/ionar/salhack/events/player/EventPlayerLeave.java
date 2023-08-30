package me.ionar.salhack.events.player;

import me.ionar.salhack.events.MinecraftEvent;

public class EventPlayerLeave extends MinecraftEvent {
    private final String _name;
    private final String _id;

    public EventPlayerLeave(String name, String id) {
        _name = name;
        _id = id;
    }

    public String getName() {
        return _name;
    }

    public String getId() {
        return _id;
    }
}
