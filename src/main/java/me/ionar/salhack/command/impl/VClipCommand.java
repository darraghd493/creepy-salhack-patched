package me.ionar.salhack.command.impl;

import me.ionar.salhack.command.Command;
import net.minecraft.entity.Entity;

public class VClipCommand extends Command {
    public VClipCommand() {
        super("VClip", "Allows you to vclip x blocks");
    }

    @Override
    public void ProcessCommand(String args) {
        String[] split = args.split(" ");

        if (split == null || split.length <= 1) {
            SendToChat("Invalid Input");
            return;
        }

        final double number = Double.parseDouble(split[1]);

        Entity entity = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player;

        entity.setPosition(mc.player.posX, mc.player.posY + number, mc.player.posZ);

        SendToChat(String.format("Teleported you %s blocks up", number));
    }

    @Override
    public String GetHelp() {
        return "Allows you teleport up x amount of blocks.";
    }
}
