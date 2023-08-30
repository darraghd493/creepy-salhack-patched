package me.ionar.salhack.module.world;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

import java.text.DecimalFormat;

public final class TimerModule extends Module {

    public final Value<Float> speed = new Value<Float>("Speed", new String[]
            {"Spd"}, "Tick-rate multiplier. [(20tps/second) * (this value)]", 4.0f, 0.1f, 10.0f, 0.1f);
    public final Value<Boolean> Accelerate = new Value<Boolean>("Accelerate", new String[]
            {"Acc"}, "Accelerate's from 1.0 until the anticheat lags you back", false);
    public final Value<Boolean> TPSSync = new Value<Boolean>("TPSSync", new String[]
            {"TPS"}, "Syncs the game time to the current TPS", false);

    private final Timer timer = new Timer();
    private float OverrideSpeed = 1.0f;
    /// store this as member to save cpu
    private final DecimalFormat format = new DecimalFormat("#.#");
    @EventHandler
    private final Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (OverrideSpeed != 1.0f && OverrideSpeed > 0.1f) {
            mc.timer.tickLength = 50.0f / OverrideSpeed;
            return;
        }

        if (TPSSync.getValue()) {
            float tPS = TickRateManager.Get().getTickRate();

            mc.timer.tickLength = Math.min(500, 50.0f * (20 / tPS));
        } else
            mc.timer.tickLength = 50.0f / GetSpeed();

        if (Accelerate.getValue() && timer.passed(2000)) {
            timer.reset();
            speed.setValue(speed.getValue() + 0.1f);
        }
    });
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (event.getPacket() instanceof SPacketPlayerPosLook && Accelerate.getValue()) {
            speed.setValue(1.0f);
        }
    });

    public TimerModule() {
        super("Timer", new String[]
                {"Time", "Tmr"}, "Speeds up the client tick rate", "NONE", 0x24DBA3, ModuleType.WORLD);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.timer.tickLength = 50;
    }

    @Override
    public String getMetaData() {
        if (OverrideSpeed != 1.0f)
            return String.valueOf(OverrideSpeed);

        if (TPSSync.getValue()) {
            float tPS = TickRateManager.Get().getTickRate();

            return format.format((tPS / 20));
        }

        return format.format(GetSpeed());
    }

    private float GetSpeed() {
        return Math.max(speed.getValue(), 0.1f);
    }

    public void SetOverrideSpeed(float f) {
        OverrideSpeed = f;
    }

}
