package me.ionar.salhack.command.impl;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;

public class UnbindCommand extends Command {
    public UnbindCommand() {
        super("Unbind", "Allows you to unbind a mod to a key");

        CommandChunks.add("<module>");
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
            mod.setKey("NONE");
            SendToChat(String.format("Unbound %s", mod.getDisplayName()));
        } else {
            SendToChat(String.format("Could not find the module named %s", split[1]));
        }
    }

    @Override
    public String GetHelp() {
        return "Allows you to unbind a mod";
    }
}
