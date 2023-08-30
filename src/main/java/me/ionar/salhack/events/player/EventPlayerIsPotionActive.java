package me.ionar.salhack.events.player;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.potion.Potion;

public class EventPlayerIsPotionActive extends MinecraftEvent {
    public Potion potion;

    public EventPlayerIsPotionActive(Potion potion) {
        super();

        potion = potion;
    }
}
