package me.ionar.salhack.events.player;

import me.ionar.salhack.events.MinecraftEvent;

public class EventPlayerJump extends MinecraftEvent {
    public double MotionX;
    public double MotionY;

    public EventPlayerJump(double motionX, double motionY) {
        super();
        MotionX = motionX;
        MotionY = motionY;
    }
}
