package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.entity.EventHorseSaddled;
import me.ionar.salhack.events.entity.EventSteerEntity;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public class EntityControlModule extends Module {
    @EventHandler
    private final Listener<EventSteerEntity> OnSteerEntity = new Listener<>(event ->
    {
        event.cancel();
    });
    @EventHandler
    private final Listener<EventHorseSaddled> OnHorseSaddled = new Listener<>(event ->
    {
        event.cancel();
    });

    public EntityControlModule() {
        super("EntityControl", new String[]
                {"AntiSaddle", "EntityRide", "NoSaddle"}, "Allows you to control llamas, horses, pigs without a saddle/carrot", "NONE", -1, ModuleType.MOVEMENT);
    }
}
