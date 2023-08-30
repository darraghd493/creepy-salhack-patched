package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class HoleInfoComponent extends HudComponentItem {
    public HoleInfoComponent() {
        super("HoleInfo", 2, 170);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        SetWidth(100);
        SetHeight(20);

        String addon = "None";

        final Vec3d playerPos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());

        final BlockPos blockPos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);

        BlockPos[] positions = {blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west()/*, blockPos.down()*/}; /// todo check down ?

        int counter = 0;
        boolean allBedrock = true;

        for (BlockPos pos : positions) {
            Block block = mc.world.getBlockState(pos).getBlock();

            if (block == Blocks.AIR)
                break;

            if (block != Blocks.BEDROCK)
                allBedrock = false;

            if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK)
                ++counter;
        }

        if (counter == 4) /// 5 if down
        {
            if (allBedrock)
                addon = ChatFormatting.GREEN + "Safe";
            else
                addon = ChatFormatting.YELLOW + "Unsafe";
        }

        RenderUtil.drawStringWithShadow(String.format("Hole: %s", addon), GetX(), GetY(), 0xFFFFFF);
    }

}
