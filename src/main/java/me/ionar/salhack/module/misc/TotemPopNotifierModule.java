package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.managers.NotificationManager;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;

import java.util.HashMap;

public class TotemPopNotifierModule extends Module {
    private final HashMap<String, Integer> TotemPopContainer = new HashMap<String, Integer>();
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();

            if (packet.getOpCode() == 35) ///< Opcode check the packet 35 is totem, thxmojang
            {
                Entity entity = packet.getEntity(mc.world);

                if (entity == null)
                    return;

                int count = 1;

                if (TotemPopContainer.containsKey(entity.getName())) {
                    count = TotemPopContainer.get(entity.getName()).intValue();
                    TotemPopContainer.put(entity.getName(), ++count);
                } else {
                    TotemPopContainer.put(entity.getName(), count);
                }

                NotificationManager.Get().AddNotification("TotemPop", entity.getName() + " popped " + count + " totem(s)!");
                SendMessage(entity.getName() + " popped " + count + " totem(s)!");
            }
        }
    });
    @EventHandler
    private final Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        for (EntityPlayer player : mc.world.playerEntities) {
            if (!TotemPopContainer.containsKey(player.getName()))
                continue;

            if (player.isDead || player.getHealth() <= 0.0f) {
                int count = TotemPopContainer.get(player.getName()).intValue();

                TotemPopContainer.remove(player.getName());

                NotificationManager.Get().AddNotification("TotemPop", player.getName() + " died after popping " + count + " totem(s)!");
                SendMessage(player.getName() + " died after popping " + count + " totem(s)!");
            }
        }
    });

    public TotemPopNotifierModule() {
        super("TotemPopNotifier", new String[]{"TPN"}, "Notifys when someone pops a totem!", "NONE", 0x2482DB, ModuleType.MISC);
    }
}