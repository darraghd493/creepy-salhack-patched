package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.settings.GameSettings;

//Thanks to Huzuni for at: https://github.com/kale2524/Huzuni for this module. (Ok buddy -bleepo)
public class GlideModule extends Module {

    @EventHandler
    private final Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event -> {
        if (GlideModule.this.isEnabled())
            if (shouldGlide()) {
                mc.player.motionY = -0.125;
                mc.player.jumpMovementFactor = mc.player.jumpMovementFactor * 1.21337F;
            }
    });

    public GlideModule() {
        super("GlideModule", new String[]{"Glide"}, "Throwback to 2015 clients; you can glide.", "NONE", 0xFFFB11, ModuleType.MOVEMENT);
    }

    private boolean shouldGlide() {
        return !GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && mc.player.motionY != 0 && !mc.player.onGround && mc.player.fallDistance != 0 && !mc.player.isInWater() && !mc.player.isOnLadder() && !mc.player.isInLava() && !mc.player.collidedVertically;
    }

}
