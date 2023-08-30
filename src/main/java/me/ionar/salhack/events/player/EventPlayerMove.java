package me.ionar.salhack.events.player;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.entity.MoverType;

public class EventPlayerMove extends MinecraftEvent {
    public MoverType Type;
    public double X;
    public double Y;
    public double Z;

    public EventPlayerMove(MoverType type, double x1, double y1, double z1) {
        Type = type;
        X = x1;
        Y = y1;
        Z = z1;
    }
}
