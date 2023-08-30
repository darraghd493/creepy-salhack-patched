package me.ionar.salhack.command.impl;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.managers.PresetsManager;

public class PresetsCommand extends Command {
    public PresetsCommand() {
        super("Presets", "Allows you to create custom presets");

        CommandChunks.add("create <name>");
        CommandChunks.add("delete <name>");
        CommandChunks.add("list");
    }

    @Override
    public void ProcessCommand(String args) {
        String[] split = args.split(" ");

        if (split == null || split.length <= 1) {
            SendToChat("Invalid Input");
            return;
        }

        if (split[1].toLowerCase().startsWith("c")) {
            if (split.length > 1) {
                String presetName = split[2].toLowerCase();

                if (!presetName.equalsIgnoreCase("Deault")) {
                    PresetsManager.Get().CreatePreset(presetName);
                    SendToChat("Created a preset named " + presetName);
                } else
                    SendToChat("Default preset is reserved!");

            } else {
                SendToChat("Usage: preset create <name>");
            }
        } else if (split[1].toLowerCase().startsWith("d")) {
            if (split.length > 1) {
                String presetName = split[2].toLowerCase();

                if (!presetName.equalsIgnoreCase("Deault")) {
                    PresetsManager.Get().RemovePreset(presetName);
                    SendToChat("Removed a preset named " + presetName);
                } else
                    SendToChat("Default preset is reserved!");

            } else {
                SendToChat("Usage: preset remove <name>");
            }
        } else if (split[1].toLowerCase().startsWith("l")) {
            PresetsManager.Get().GetItems().forEach(p ->
            {
                SendToChat(p.getName());
            });
        }
    }

    @Override
    public String GetHelp() {
        return "Allows you to create, remove and list the presets";
    }
}
