package me.ionar.salhack.module.movement;


import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;


public class HorseJump extends Module {

    @EventHandler
    private final Listener<EventPlayerUpdate> listener = new Listener<>(event -> {
        mc.player.horseJumpPower = 1;
        mc.player.horseJumpPowerCounter = -10;
    });

    public HorseJump() {
        super("HorseJump", new String[]
                {"HorseJump"}, "Modifies how high a horse jump.", "NONE", 0x24DB3E, ModuleType.MOVEMENT);
    }
}
