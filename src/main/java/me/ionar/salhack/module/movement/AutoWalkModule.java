package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.player.EventPlayerUpdateMoveState;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.world.AutoTunnelModule;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public final class AutoWalkModule extends Module {
    private AutoTunnelModule _autoTunnel;
    @EventHandler
    private final Listener<EventPlayerUpdateMoveState> OnUpdateMoveState = new Listener<>(event ->
    {
        if (!NeedPause())
            mc.player.movementInput.moveForward++;
    });

    public AutoWalkModule() {
        super("AutoWalk", new String[]
                {"AW"}, "Automatically walks forward", "NONE", 0xC224DB, ModuleType.MOVEMENT);
    }

    @Override
    public void init() {
        _autoTunnel = (AutoTunnelModule) ModuleManager.Get().GetMod(AutoTunnelModule.class);
    }

    private boolean NeedPause() {
        return _autoTunnel.PauseAutoWalk();
    }
}
