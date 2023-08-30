package me.ionar.salhack.command.impl;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;

public class BindCommand extends Command {
    public BindCommand() {
        super("Bind", "Allows you to bind a mod to a key");

        CommandChunks.add("<module>");
        CommandChunks.add("<module> <key>");
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
            if (split.length <= 2) {
                SendToChat(String.format("The key of %s is %s", mod.getDisplayName(), mod.getKey()));
                return;
            }

            mod.setKey(split[2].toUpperCase());
            SendToChat(String.format("Set the key of %s to %s", mod.getDisplayName(), mod.getKey()));
        } else {
            SendToChat(String.format("Could not find the module named %s", split[1]));
        }
    }

    @Override
    public String GetHelp() {
        return "Allows you to Bind a mod";
    }
}
