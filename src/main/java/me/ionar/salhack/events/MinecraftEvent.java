package me.ionar.salhack.events;

import me.ionar.salhack.main.Wrapper;
import me.zero.alpine.fork.event.type.Cancellable;

public class MinecraftEvent extends Cancellable {
    private final float partialTicks;
    private Era era = Era.PRE;

    public MinecraftEvent() {
        partialTicks = Wrapper.GetMC().getRenderPartialTicks();
    }

    public MinecraftEvent(Era era1) {
        partialTicks = Wrapper.GetMC().getRenderPartialTicks();
        era = era1;
    }

    public Era getEra() {
        return era;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public enum Era {
        PRE,
        PERI,
        POST
    }

}
