package me.ionar.salhack.events.player;

import me.ionar.salhack.events.MinecraftEvent;

public class EventPlayerTravel extends MinecraftEvent {
    public float Strafe;
    public float Vertical;
    public float Forward;

    public EventPlayerTravel(float strafe, float vertical, float forward) {
        Strafe = strafe;
        Vertical = vertical;
        Forward = forward;
    }
}