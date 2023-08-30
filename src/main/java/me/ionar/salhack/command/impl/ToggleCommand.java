package me.ionar.salhack.command.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.command.Command;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("Toggle", "Allows you to toggle a mod");
    }

    @Override
    public void ProcessCommand(String args) {
        String[] split = args.split(" ");

        if (split == null || split.length <= 1) {
            SendToChat("Invalid Input");
            return;
        }

        Module mod = ModuleManager.Get().GetModLike(split[1]);

        if (mod != null) {
            mod.toggle();

            SendToChat(String.format("%sToggled %s", mod.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED, mod.GetArrayListDisplayName()));
        } else {
            SendToChat(String.format("Could not find the module named %s", split[1]));
        }
    }

    @Override
    public String GetHelp() {
        return "Allows you to toggle a mod";
    }
}
