package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

//Thanks to Wolfram at https://www.wolframclient.net/ for this Module. (what the fuck is wolfram -bleepo)
public class ParkourJump extends Module {

    @EventHandler
    private final Listener<EventPlayerUpdate> onUpdate = new Listener<>(event -> {
        if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, -0.5, 0.0).expand(0.001, 0.0, 0.001)).isEmpty() && mc.player.onGround && !mc.player.isSneaking()) {
            mc.player.jump();
        }
    });

    public ParkourJump() {
        super("ParkourJump", new String[]
                {"ParkourJump"}, "Jumps at the edge of a block.", "NONE", 0x2460DB, ModuleType.MOVEMENT);
    }
}
