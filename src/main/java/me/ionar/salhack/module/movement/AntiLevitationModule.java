package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.player.EventPlayerIsPotionActive;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.init.MobEffects;

public final class AntiLevitationModule extends Module {
    @EventHandler
    private final Listener<EventPlayerIsPotionActive> IsPotionActive = new Listener<>(event ->
    {
        if (event.potion == MobEffects.LEVITATION)
            event.cancel();
    });

    public AntiLevitationModule() {
        super("AntiLevitation", new String[]
                {"NoLevitate"}, "Prevents you from levitating", "NONE", 0xC224DB, ModuleType.MOVEMENT);
    }
}
