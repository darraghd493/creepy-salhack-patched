package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.entity.EventEntityAdded;
import me.ionar.salhack.events.entity.EventEntityRemoved;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.managers.NotificationManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class VisualRangeModule extends Module {
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]{"M"}, "Mode of notifying to use", Modes.Both);
    public final Value<Boolean> Friends = new Value<Boolean>("Friends", new String[]
            {"Friend"}, "Notifies if a friend comes in range", true);
    public final Value<Boolean> Enter = new Value<Boolean>("Enter", new String[]
            {"Enters"}, "Notifies when the entity enters range", true);
    public final Value<Boolean> Leave = new Value<Boolean>("Leave", new String[]
            {"Leaves"}, "Notifies when the entity leaves range", true);
    private final List<String> Entities = new ArrayList<String>();
    @EventHandler
    private final Listener<EventEntityAdded> OnEntityAdded = new Listener<>(event ->
    {
        if (!Enter.getValue())
            return;

        if (!VerifyEntity(event.GetEntity()))
            return;

        if (!Entities.contains(event.GetEntity().getName())) {
            Entities.add(event.GetEntity().getName());
            Notify(String.format("%s has entered your visual range.", event.GetEntity().getName()));
        }
    });
    @EventHandler
    private final Listener<EventEntityRemoved> OnEntityRemove = new Listener<>(event ->
    {
        if (!Leave.getValue())
            return;

        if (!VerifyEntity(event.GetEntity()))
            return;

        if (Entities.contains(event.GetEntity().getName())) {
            Entities.remove(event.GetEntity().getName());
            Notify(String.format("%s has left your visual range.", event.GetEntity().getName()));
        }
    });

    public VisualRangeModule() {
        super("VisualRange", new String[]
                {"VR"}, "Notifies you when one enters or leaves your visual range.", "NONE", -1, Module.ModuleType.MISC);
    }

    @Override
    public String getMetaData() {
        return String.valueOf(Mode.getValue());
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Entities.clear();
    }

    private boolean VerifyEntity(Entity entity) {
        if (!(entity instanceof EntityPlayer))
            return false;

        if (entity == mc.player)
            return false;

        return Friends.getValue() || !FriendManager.Get().IsFriend(entity);
    }

    private void Notify(String msg) {
        switch (Mode.getValue()) {
            case Chat:
                SendMessage(msg);
                break;
            case Notification:
                NotificationManager.Get().AddNotification("VisualRange", msg);
                break;
            case Both:
                SendMessage(msg);
                NotificationManager.Get().AddNotification("VisualRange", msg);
                break;
        }
    }

    private enum Modes {
        Chat,
        Notification,
        Both,
    }
}
