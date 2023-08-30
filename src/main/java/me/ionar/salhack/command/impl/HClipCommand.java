package me.ionar.salhack.command.impl;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class HClipCommand extends Command {
    public HClipCommand() {
        super("HClip", "Allows you to hclip x blocks");
    }

    @Override
    public void ProcessCommand(String args) {
        String[] split = args.split(" ");

        if (split == null || split.length <= 1) {
            SendToChat("Invalid Input");
            return;
        }

        final double number = Double.parseDouble(split[1]);

        final Vec3d direction = MathUtil.direction(mc.player.rotationYaw);

        if (direction != null) {
            Entity entity = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player;

            entity.setPosition(mc.player.posX + direction.x * number, mc.player.posY, mc.player.posZ + direction.z * number);

            SendToChat(String.format("Teleported you %s blocks forward", number));
        }
    }

    @Override
    public String GetHelp() {
        return "Allows you teleport forward x amount of blocks.";
    }
}
