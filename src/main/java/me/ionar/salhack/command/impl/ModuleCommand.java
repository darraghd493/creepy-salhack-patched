package me.ionar.salhack.command.impl;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.command.util.ModuleCommandListener;
import me.ionar.salhack.module.Value;

import java.util.List;

public class ModuleCommand extends Command {
    private final List<Value> Values;
    private final ModuleCommandListener Listener;

    public ModuleCommand(String name1, String description1, ModuleCommandListener listener, final List<Value> values) {
        super(name1, description1);
        Listener = listener;
        Values = values;

        CommandChunks.add("hide");
        CommandChunks.add("toggle");
        CommandChunks.add("rename <newname>");

        /// TODO: Add enum names, etc
        for (Value val : Values)
            CommandChunks.add(String.format("%s <%s>", val.getName(), "value"));
    }

    @Override
    public void ProcessCommand(String args) {
        String[] split = args.split(" ");

        if (split == null || split.length <= 1) {
            /// Print values
            for (Value val : Values) {
                SendToChat(String.format("%s : %s", val.getName(), val.getValue()));
            }
            return;
        }

        if (split[1].equalsIgnoreCase("hide")) {
            Listener.OnHide();
            return;
        }

        if (split[1].equalsIgnoreCase("toggle")) {
            Listener.OnHide();
            return;
        }

        if (split[1].equalsIgnoreCase("rename")) {
            if (split.length <= 3)
                Listener.OnRename(split[2]);

            return;
        }

        for (Value val : Values) {
            if (val.getName().toLowerCase().startsWith(split[1].toLowerCase())) {
                if (split.length <= 2)
                    break;

                String value = split[2].toLowerCase();

                if (val.getValue() instanceof Number && !(val.getValue() instanceof Enum)) {
                    if (val.getValue() instanceof Integer)
                        val.SetForcedValue(Integer.parseInt(value));
                    else if (val.getValue() instanceof Float)
                        val.SetForcedValue(Float.parseFloat(value));
                    else if (val.getValue() instanceof Double)
                        val.SetForcedValue(Double.parseDouble(value));
                } else if (val.getValue() instanceof Boolean) {
                    val.SetForcedValue(value.equalsIgnoreCase("true"));
                } else if (val.getValue() instanceof Enum) {
                    val.SetForcedValue(val.GetEnumReal(value));
                } else if (val.getValue() instanceof String)
                    val.SetForcedValue(value);

                SendToChat(String.format("Set the value of %s to %s", val.getName(), val.getValue()));

                break;
            }
        }
    }

    @Override
    public String GetHelp() {
        return GetDescription();
    }
}
