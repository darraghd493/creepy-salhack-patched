package me.ionar.salhack.events.player;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.util.EnumHand;

public class EventPlayerSwingArm extends MinecraftEvent {
    public EnumHand Hand;

    public EventPlayerSwingArm(EnumHand hand) {
        super();
        Hand = hand;
    }
}
