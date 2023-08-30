package me.ionar.salhack.managers;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.command.impl.*;
import me.ionar.salhack.command.util.ModuleCommandListener;
import me.ionar.salhack.main.SalHack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager {
    private final ArrayList<Command> Commands = new ArrayList<Command>();

    public CommandManager() {
    }

    public static CommandManager Get() {
        return SalHack.GetCommandManager();
    }

    public void InitalizeCommands() {
        Commands.add(new FriendCommand());
        Commands.add(new HelpCommand());
        Commands.add(new SoundReloadCommand());
        Commands.add(new HClipCommand());
        Commands.add(new VClipCommand());
        Commands.add(new ToggleCommand());
        Commands.add(new BindCommand());
        Commands.add(new UnbindCommand());
        Commands.add(new ResetGUICommand());
        Commands.add(new FontCommand());
        Commands.add(new PresetsCommand());
        Commands.add(new WaypointCommand());
        Commands.add(new GitHubCommand());

        ModuleManager.Get().GetModuleList().forEach(mod1 ->
        {
            ModuleCommandListener listener = new ModuleCommandListener() {
                @Override
                public void OnHide() {
                    mod1.setHidden(!mod1.isHidden());
                }

                @Override
                public void OnToggle() {
                    mod1.toggle();
                }

                @Override
                public void OnRename(String newName) {
                    mod1.setDisplayName(newName);
                }
            };

            Commands.add(new ModuleCommand(mod1.getDisplayName(), mod1.getDesc(), listener, mod1.getValueList()));
        });

        HudManager.Get().Items.forEach(item ->
        {
            ModuleCommandListener listener = new ModuleCommandListener() {
                @Override
                public void OnHide() {
                    item.SetHidden(!item.IsHidden());
                }

                @Override
                public void OnToggle() {
                    item.SetHidden(!item.IsHidden());
                }

                @Override
                public void OnRename(String newName) {
                    item.SetDisplayName(newName, true);
                }
            };

            Commands.add(new ModuleCommand(item.GetDisplayName(), "NYI", listener, item.ValueList));
        });

        /// Sort by alphabet
        Commands.sort(Comparator.comparing(Command::GetName));
    }

    public final ArrayList<Command> GetCommands() {
        return Commands;
    }

    public final List<Command> GetCommandsLike(String like) {
        return Commands.stream()
                .filter(command -> command.GetName().toLowerCase().startsWith(like.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Command GetCommandLike(String like) {
        for (Command command : Commands) {
            if (command.GetName().toLowerCase().startsWith(like.toLowerCase()))
                return command;
        }

        return null;
    }

    public void Reload() {
        Commands.clear();
        InitalizeCommands();
    }
}
