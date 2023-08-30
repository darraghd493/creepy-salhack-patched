package me.ionar.salhack.command.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.command.Command;
import me.ionar.salhack.managers.CommandManager;

import java.util.List;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("Help", "Gives you help for commands");
    }

    @Override
    public void ProcessCommand(String args) {
        String[] split = args.split(" ");

        if (split == null || split.length <= 1) {
            SendToChat(GetHelp());
            return;
        }

        Command command = CommandManager.Get().GetCommandLike(split[1]);

        if (command == null)
            SendToChat(String.format("Couldn't find any command named like %s", split[1]));
        else
            SendToChat(command.GetHelp());
    }

    @Override
    public String GetHelp() {
        final List<Command> commands = CommandManager.Get().GetCommands();

        String commandString = "Available commands: (" + commands.size() + ")" + ChatFormatting.WHITE + " [";

        for (int i = 0; i < commands.size(); ++i) {
            Command command = commands.get(i);

            if (i == commands.size() - 1)
                commandString += command.GetName() + "]";
            else
                commandString += command.GetName() + ", ";
        }

        return commandString;
    }
}
