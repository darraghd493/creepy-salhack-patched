package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;

/**
 * Author Seth 5/1/2019 @ 7:56 PM.
 */
public final class YawModule extends Module {
    public final Value<Boolean> yawLock = new Value<Boolean>("Yaw", new String[]
            {"Y"}, "Lock the player's rotation yaw if enabled.", true);
    public final Value<Boolean> pitchLock = new Value<Boolean>("Pitch", new String[]
            {"P"}, "Lock the player's rotation pitch if enabled.", false);
    public final Value<Boolean> Cardinal = new Value<Boolean>("Cardinal", new String[]
            {"C"}, "Locks the yaw to one of the cardinal directions", false);

    private float Yaw;
    private float Pitch;
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (event.getEra() != Era.PRE)
            return;

        Entity entity = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player;

        if (this.yawLock.getValue())
            entity.rotationYaw = Yaw;

        if (this.pitchLock.getValue())
            entity.rotationPitch = Pitch;

        if (Cardinal.getValue())
            entity.rotationYaw = Math.round((entity.rotationYaw + 1.0f) / 45.0f) * 45.0f;
    });

    public YawModule() {
        super("Yaw", new String[]
                {"RotLock", "Rotation"}, "Locks you rotation for precision", "NONE", 0xDA24DB, ModuleType.MOVEMENT);
    }

    @Override
    public String getMetaData() {
        if (Cardinal.getValue())
            return "Cardinal";

        return "One";
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (mc.player != null) {
            Yaw = mc.player.rotationYaw;
            Pitch = mc.player.rotationPitch;
        }
    }

    @Override
    public void toggleNoSave() {
        /// override don't trigger on logic, we access player at enable
    }
}
