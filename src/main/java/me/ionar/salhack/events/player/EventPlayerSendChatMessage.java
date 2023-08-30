package me.ionar.salhack.events.player;

import me.ionar.salhack.events.MinecraftEvent;

public class EventPlayerSendChatMessage extends MinecraftEvent {
    public String Message;

    public EventPlayerSendChatMessage(String message) {
        super();

        Message = message;
    }

}
