package me.ionar.salhack.events.player;

import me.ionar.salhack.events.MinecraftEvent;

public class EventPlayerPushOutOfBlocks extends MinecraftEvent {
    public double X, Y, Z;

    public EventPlayerPushOutOfBlocks(double x1, double y1, double z1) {
        super();

        X = x1;
        Y = y1;
        Z = z1;
    }
}
