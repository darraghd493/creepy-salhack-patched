package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;

public final class SneakModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]
            {"Mode", "M"}, "The sneak mode to use.", Mode.NCP);
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (event.getEra() != Era.PRE)
            return;

        switch (this.mode.getValue()) {
            case Vanilla:
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                break;
            case NCP:
                if (!mc.player.isSneaking()) {
                    if (this.isMoving()) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                    } else {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                    }
                }
                break;
            case Always:
                mc.gameSettings.keyBindSneak.pressed = true;
                break;
        }
    });
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (this.mode.getValue() != Mode.Always) {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && !mc.player.isSneaking()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
        }
    });

    public SneakModule() {
        super("Sneak", new String[]
                {"Sneek"}, "Allows you to sneak at full speed", "NONE", 0xDB2493, ModuleType.MOVEMENT);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.world != null && !mc.player.isSneaking()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    private boolean isMoving() {
        return GameSettings.isKeyDown(mc.gameSettings.keyBindForward) || GameSettings.isKeyDown(mc.gameSettings.keyBindLeft) || GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
                || GameSettings.isKeyDown(mc.gameSettings.keyBindBack);
    }

    private enum Mode {
        Vanilla,
        NCP,
        Always
    }

}
