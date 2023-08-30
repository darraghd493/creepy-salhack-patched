package me.ionar.salhack.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.events.salhack.EventSalHackModuleDisable;
import me.ionar.salhack.events.salhack.EventSalHackModuleEnable;
import me.ionar.salhack.managers.NotificationManager;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public class ChatNotifierModule extends Module {
    @EventHandler
    private final Listener<EventSalHackModuleEnable> OnModEnable = new Listener<>(event ->
    {
        if (event.Mod.getDisplayName() == "ManualDupe")
            return;

        String msg = String.format("%s was enabled.",
                ChatFormatting.GREEN + event.Mod.getDisplayName() + ChatFormatting.AQUA);

        SendMessage(msg);
        NotificationManager.Get().AddNotification("ChatNotifier", msg);
    });
    @EventHandler
    private final Listener<EventSalHackModuleDisable> OnModDisable = new Listener<>(event ->
    {
        if (event.Mod.getDisplayName() == "ManualDupe")
            return;

        String msg = String.format("%s was disabled.",
                ChatFormatting.RED + event.Mod.getDisplayName() + ChatFormatting.AQUA);

        SendMessage(msg);
        NotificationManager.Get().AddNotification("ChatNotifier", msg);
    });

    public ChatNotifierModule() {
        super("ChatNotifier", new String[]
                        {""}, "Notifiys you in chat and notification system when a mod is enabled/disabled", "NONE", -1,
                ModuleType.MISC);
    }
}
