package me.ionar.salhack.events.particles;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;

public class EventParticleEmitParticleAtEntity extends MinecraftEvent {
    public Entity entity;
    public EnumParticleTypes Type;
    public int Amount;

    public EventParticleEmitParticleAtEntity(Entity entity, EnumParticleTypes type, int amount) {
        entity = entity;
        Type = type;
        Amount = amount;
    }
}
