package me.ionar.salhack.events.entity;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.entity.Entity;

public class EventEntityRemoved extends MinecraftEvent {
    private final Entity m_Entity;

    public EventEntityRemoved(Entity entity) {
        m_Entity = entity;
    }

    public Entity GetEntity() {
        return m_Entity;
    }
}
