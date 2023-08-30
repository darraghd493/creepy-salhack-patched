package me.ionar.salhack.command;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.main.Wrapper;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class Command {
    protected final Minecraft mc = Wrapper.GetMC();
    protected final List<String> CommandChunks = new ArrayList<String>();
    private final String Name;
    private final String Description;

    public Command(String name1, String description1) {
        Name = name1;
        Description = description1;
    }

    public String GetName() {
        return Name;
    }

    public String GetDescription() {
        return Description;
    }

    public void ProcessCommand(String args) {
    }


    protected void SendToChat(String desc) {
        SalHack.SendMessage(String.format("%s[%s]: %s", ChatFormatting.LIGHT_PURPLE, GetName(), ChatFormatting.YELLOW + desc));
    }

    public List<String> GetChunks() {
        return CommandChunks;
    }

    public String GetHelp() {
        return Description;
    }
}
