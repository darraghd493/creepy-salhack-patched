package me.ionar.salhack.util.render;

import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.module.render.EntityESPModule;
import me.ionar.salhack.util.Hole.HoleTypes;
import me.ionar.salhack.util.entity.EntityUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ESPUtil {
    public static void ColorToGL(final Color color) {
        GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    public static void RenderCSGOShader(final EntityESPModule ESP, final Entity entity) {
        if (EntityUtil.IsVehicle(entity) && ESP.Vehicles.getValue())
            ESPUtil.ColorToGL(new Color(200, 100, 0, 255));
        if (EntityUtil.isPassive(entity) && ESP.Animals.getValue()) {
            final int n = 200;
            final int n2 = 0;
            ESPUtil.ColorToGL(new Color(n2, n, n2, 255));
        }
        if ((EntityUtil.isHostileMob(entity) || EntityUtil.isNeutralMob(entity)) && ESP.Monsters.getValue())
            ESPUtil.ColorToGL(new Color(200, 60, 60, 255));

        if (entity instanceof EntityEnderCrystal && ESP.Others.getValue()) {
            final int n3 = 100;
            final int n4 = 200;
            ESPUtil.ColorToGL(new Color(n4, n3, n4, 255));
        }
        if (entity instanceof EntityPlayer && ESP.Players.getValue()) {
            final EntityPlayer entityPlayer;
            if ((entityPlayer = (EntityPlayer) entity).isInvisible()) {
                ESPUtil.ColorToGL(new Color(133, 200, 178, 255));
            }
            final float distance = Wrapper.GetMC().player.getDistance(entityPlayer);
            int n5 = 0;
            if (distance >= 60.0f)
                n5 = 120;
            else {
                final int n6 = (int) distance;
                n5 = n6 + n6;
            }
            ESPUtil.ColorToGL(new Color(n5, 100, 50, 128));
        }
    }

    public static void RenderCSGO(ICamera camera, EntityESPModule ESP, RenderEvent event) {
        GL11.glPushMatrix();
        Wrapper.GetMC().world.loadedEntityList.forEach(entity ->
        {
            if (entity != null && !entity.isDead && entity != Wrapper.GetMC().player) {
                double d3 = Wrapper.GetMC().player.lastTickPosX + (Wrapper.GetMC().player.posX - Wrapper.GetMC().player.lastTickPosX) * (double) event.getPartialTicks();
                double d4 = Wrapper.GetMC().player.lastTickPosY + (Wrapper.GetMC().player.posY - Wrapper.GetMC().player.lastTickPosY) * (double) event.getPartialTicks();
                double d5 = Wrapper.GetMC().player.lastTickPosZ + (Wrapper.GetMC().player.posZ - Wrapper.GetMC().player.lastTickPosZ) * (double) event.getPartialTicks();

                camera.setPosition(d3, d4, d5);

                if (camera.isBoundingBoxInFrustum(entity.getEntityBoundingBox())) {
                    RenderCSGOShader(ESP, entity);
                    Wrapper.GetMC().getRenderManager().renderEntityStatic(entity, event.getPartialTicks(), false);
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
        });
        GL11.glPopMatrix();
    }

    public static void RenderOutline(ICamera camera, RenderEvent event) {
        GL11.glPushMatrix();
        Wrapper.GetMC().world.loadedEntityList.forEach(entity ->
        {
            if (entity != null && !entity.isDead && entity != Wrapper.GetMC().player) {
                double d3 = Wrapper.GetMC().player.lastTickPosX + (Wrapper.GetMC().player.posX - Wrapper.GetMC().player.lastTickPosX) * (double) event.getPartialTicks();
                double d4 = Wrapper.GetMC().player.lastTickPosY + (Wrapper.GetMC().player.posY - Wrapper.GetMC().player.lastTickPosY) * (double) event.getPartialTicks();
                double d5 = Wrapper.GetMC().player.lastTickPosZ + (Wrapper.GetMC().player.posZ - Wrapper.GetMC().player.lastTickPosZ) * (double) event.getPartialTicks();

                camera.setPosition(d3, d4, d5);

                if (camera.isBoundingBoxInFrustum(entity.getEntityBoundingBox())) {
                    RenderOutline(entity, 1, 1, 1, 255);
                    Wrapper.GetMC().getRenderManager().renderEntityStatic(entity, event.getPartialTicks(), false);
                }
            }
        });
        GL11.glPopMatrix();
    }

    public static void RenderShader(RenderEvent event) {

    }

    public static void RenderOutline(final Entity entity, final double n, final double n2, final double n3, final int n4) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) n, (float) n2 + entity.height + 0.5f, (float) n3);
        final float n5 = 1.0f;
        final float n6 = 0.0f;
        GL11.glNormal3f(n6, n5, n6);
        final float n7 = Wrapper.GetMC().getRenderManager().playerViewY;
        final float n8 = 1.0f;
        final float n9 = 0.0f;
        GL11.glRotatef(n7, n9, n8, n9);
        GL11.glScalef(-0.017f, -0.017f, 0.017f);
        GL11.glDepthMask(false);
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        int n10 = 0;
        if (entity.isSneaking()) {
            n10 = 4;
        }
        GL11.glDisable(3553);
        GL11.glPushMatrix();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glEnable(2848);
        GlStateManager.disableLighting();
        RenderBoundingBox(0.0, n10 + 19, 0.0, n10 + 21, -16777216);
        RenderBoundingBox(0.0, n10 + 21, 0.0, n10 + 46, -16777216);
        RenderBoundingBox(0.0, n10 + 21, 0.0, n10 + 25, n4);
        RenderBoundingBox(0.0, n10 + 25, 0.0, n10 + 48, n4);
        RenderBoundingBox(0.0, n10 + 19, 0.0, n10 + 21, -16777216);
        RenderBoundingBox(0.0, n10 + 21, 0.0, n10 + 46, -16777216);
        RenderBoundingBox(0.0, n10 + 21, 0.0, n10 + 25, n4);
        RenderBoundingBox(0.0, n10 + 25, 0.0, n10 + 48, n4);
        RenderBoundingBox(0.0, n10 + 140, 0.0, n10 + 142, -16777216);
        RenderBoundingBox(0.0, n10 + 115, 0.0, n10 + 140, -16777216);
        RenderBoundingBox(0.0, n10 + 136, 0.0, n10 + 140, n4);
        RenderBoundingBox(0.0, n10 + 113, 0.0, n10 + 140, n4);
        RenderBoundingBox(0.0, n10 + 140, 0.0, n10 + 142, -16777216);
        RenderBoundingBox(0.0, n10 + 115, 0.0, n10 + 140, -16777216);
        RenderBoundingBox(0.0, n10 + 136, 0.0, n10 + 140, n4);
        RenderBoundingBox(0.0, n10 + 113, 0.0, n10 + 140, n4);
        GlStateManager.enableLighting();
        GL11.glDisable(2848);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        final float n11 = 1.0f;
        final int n12 = 1;
        GL11.glColor4f((float) n12, (float) n12, n11, (float) n12);
        GL11.glPopMatrix();
    }

    public static void RenderBoundingBox(final double n, final double n2, final double n3, final double n4, final int n5) {
        final float n6 = (n5 >> 24 & 0xFF) / 255.0f;
        final float n7 = (n5 >> 16 & 0xFF) / 255.0f;
        final float n8 = (n5 >> 8 & 0xFF) / 255.0f;
        final float n9 = (n5 & 0xFF) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        GL11.glColor4f(n7, n8, n9, n6);
        GL11.glBegin(7);
        GL11.glVertex2d(n, n4);
        GL11.glVertex2d(n3, n4);
        GL11.glVertex2d(n3, n2);
        GL11.glVertex2d(n, n2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }

    public static boolean IsVoidHole(BlockPos blockPos, IBlockState blockState) {
        if (blockPos.getY() > 4 || blockPos.getY() <= 0)
            return false;

        BlockPos pos = blockPos;

        for (int i = blockPos.getY(); i >= 0; --i) {
            if (Wrapper.GetMC().world.getBlockState(pos).getBlock() != Blocks.AIR)
                return false;

            pos = pos.down();
        }

        return true;
    }

    public static HoleTypes isBlockValid(IBlockState blockState, BlockPos blockPos) {
        if (blockState.getBlock() != Blocks.AIR)
            return HoleTypes.None;

        if (Wrapper.GetMC().world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR)
            return HoleTypes.None;

        if (Wrapper.GetMC().world.getBlockState(blockPos.up(2)).getBlock() != Blocks.AIR) // ensure the area is
            // tall enough for
            // the player
            return HoleTypes.None;

        if (Wrapper.GetMC().world.getBlockState(blockPos.down()).getBlock() == Blocks.AIR)
            return HoleTypes.None;

        final BlockPos[] touchingBlocks = new BlockPos[]
                {blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west()};

        boolean bedrock = true;
        boolean obsidian = true;

        int validHorizontalBlocks = 0;
        for (BlockPos touching : touchingBlocks) {
            final IBlockState touchingState = Wrapper.GetMC().world.getBlockState(touching);
            if ((touchingState.getBlock() != Blocks.AIR) && touchingState.isFullBlock()) {
                validHorizontalBlocks++;

                if (touchingState.getBlock() != Blocks.BEDROCK && bedrock)
                    bedrock = false;

                if (!bedrock) {
                    if (touchingState.getBlock() != Blocks.OBSIDIAN && touchingState.getBlock() != Blocks.BEDROCK)
                        obsidian = false;
                }
            }
        }

        if (validHorizontalBlocks < 4)
            return HoleTypes.None;

        if (bedrock)
            return HoleTypes.Bedrock;
        if (obsidian)
            return HoleTypes.Obsidian;

        return HoleTypes.Normal;
    }

    public static void Render(HoleModes mode, final AxisAlignedBB bb, float red, float green, float blue, float alpha) {
        switch (mode) {
            case Flat:
                RenderGlobal.renderFilledBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, red, green, blue, alpha);
                break;
            case FlatOutline:
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, red, green, blue, alpha);
                break;
            case Full:
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, red, green, blue, alpha);
                RenderGlobal.renderFilledBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, red, green, blue, alpha);
                break;
            case Outline:
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, red, green, blue, alpha);
                break;
            default:
                break;
        }
    }

    public static void RenderOutline(RenderEvent event, BlockPos pos, float red, float green, float blue, float alpha) {
    }

    public enum HoleModes {
        None,
        FlatOutline,
        Flat,
        Outline,
        Full,
    }
}
