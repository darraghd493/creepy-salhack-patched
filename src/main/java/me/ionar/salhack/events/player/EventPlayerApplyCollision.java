package me.ionar.salhack.events.player;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.entity.Entity;

public class EventPlayerApplyCollision extends MinecraftEvent {
    public Entity entity;

    public EventPlayerApplyCollision(Entity entity) {
        super();

        entity = entity;
    }
}
