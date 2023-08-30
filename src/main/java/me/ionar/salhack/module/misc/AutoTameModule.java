package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

public class AutoTameModule extends Module {
    public final Value<Float> Delay = new Value<Float>("Delay", new String[]{"D"}, "Delay to remount", 0.1f, 0.0f, 1.0f, 0.1f);
    private AbstractHorse EntityToTame = null;
    private final Timer timer = new Timer();
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketUseEntity) {
            if (EntityToTame != null)
                return;

            final CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();

            Entity entity = packet.getEntityFromWorld(mc.world);

            if (entity instanceof AbstractHorse) {
                if (!((AbstractHorse) entity).isTame()) {
                    EntityToTame = (AbstractHorse) entity;
                    SendMessage("Will try to tame " + entity.getName());
                }
            }
        }
    });
    @EventHandler
    private final Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event -> {
        if (EntityToTame == null)
            return;

        if (EntityToTame.isTame()) {
            SendMessage("Successfully tamed " + EntityToTame.getName() + ", disabling.");
            toggle();
            return;
        }

        if (mc.player.isRiding())
            return;

        if (mc.player.getDistance(EntityToTame) > 5.0f)
            return;

        if (!timer.passed(Delay.getValue() * 1000))
            return;

        timer.reset();
        mc.getConnection().sendPacket(new CPacketUseEntity(EntityToTame, EnumHand.MAIN_HAND));
    });

    public AutoTameModule() {
        super("AutoTame", new String[]{""}, "Automatically tames the animal you click", "NONE", 0xDB24C4, ModuleType.MISC);
    }

    @Override
    public void toggleNoSave() {

    }

    @Override
    public String getMetaData() {
        if (EntityToTame == null)
            return null;

        return EntityToTame.getName();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        SendMessage("Right click an animal you want to tame");

        EntityToTame = null;
    }
}
