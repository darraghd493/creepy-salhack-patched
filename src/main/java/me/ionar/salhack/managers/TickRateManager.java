package me.ionar.salhack.managers;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.main.SalHack;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listenable;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;

public class TickRateManager implements Listenable {
    private long prevTime;
    private final float[] ticks = new float[20];
    private int currentTick;
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (event.GetPacket() instanceof SPacketTimeUpdate) {
            if (this.prevTime != -1) {
                this.ticks[this.currentTick % this.ticks.length] = MathHelper.clamp((20.0f / ((float) (System.currentTimeMillis() - this.prevTime) / 1000.0f)), 0.0f, 20.0f);
                this.currentTick++;
            }

            this.prevTime = System.currentTimeMillis();
        }
    });

    public TickRateManager() {
        this.prevTime = -1;

        for (int i = 0, len = this.ticks.length; i < len; i++) {
            this.ticks[i] = 0.0f;
        }

        SalHackMod.EVENT_BUS.subscribe(this);
    }

    public static TickRateManager Get() {
        return SalHack.GetTickRateManager();
    }

    public float getTickRate() {
        int tickCount = 0;
        float tickRate = 0.0f;

        for (int i = 0; i < this.ticks.length; i++) {
            final float tick = this.ticks[i];

            if (tick > 0.0f) {
                tickRate += tick;
                tickCount++;
            }
        }

        return MathHelper.clamp((tickRate / tickCount), 0.0f, 20.0f);
    }
}
