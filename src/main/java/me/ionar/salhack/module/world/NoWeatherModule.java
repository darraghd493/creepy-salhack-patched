package me.ionar.salhack.module.world;

import me.ionar.salhack.events.render.EventRenderRainStrength;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public final class NoWeatherModule extends Module {

    @EventHandler
    private final Listener<EventRenderRainStrength> OnRainStrength = new Listener<>(event ->
    {
        if (mc.world == null)
            return;

        event.cancel();
    });

    public NoWeatherModule() {
        super("NoWeather", new String[]
                {"AntiWeather"}, "Allows you to control the weather client-side", "NONE", -1, ModuleType.WORLD);
    }

}
