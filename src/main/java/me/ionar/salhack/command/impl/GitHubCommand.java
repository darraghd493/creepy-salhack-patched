package me.ionar.salhack.command.impl;

import me.ionar.salhack.command.Command;

public class GitHubCommand extends Command {

    public GitHubCommand() {
        super("GitHub", "Sends GitHub link.");
    }

    @Override
    public void ProcessCommand(String args) {
        String[] split = args.split(" ");

        if (split == null || split.length <= 1) {
            mc.player.sendChatMessage("https://github.com/CreepyOrb924/creepy-salhack/releases");
        }
    }

    @Override
    public String GetHelp() {
        return "Sends the Creepy-SalHack GitHub link to chat.";
    }

}
