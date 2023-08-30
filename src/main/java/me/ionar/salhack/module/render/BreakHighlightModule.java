package me.ionar.salhack.module.render;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class BreakHighlightModule extends Module {
    public final Value<HoleModes> HighlightMode = new Value<HoleModes>("HighlightModes", new String[]{"HM"}, "Mode for highlighting blocks", HoleModes.Full);
    public final Value<Float> ObsidianRed = new Value<Float>("Red", new String[]{"oRed"}, "Red for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianGreen = new Value<Float>("Green", new String[]{"oGreen"}, "Green for rendering", 1f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianBlue = new Value<Float>("Blue", new String[]{"oBlue"}, "Blue for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianAlpha = new Value<Float>("Alpha", new String[]{"oAlpha"}, "Alpha for rendering", 0.5f, 0f, 1.0f, 0.1f);
    public final Value<Boolean> DebugMsgs = new Value<Boolean>("Debug", new String[]{"Debug"}, "Allows for debugging this module", false);
    private final ICamera camera = new Frustum();
    private final ArrayList<BlockPos> BlocksBeingBroken = new ArrayList<BlockPos>();
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (event.getPacket() instanceof SPacketBlockBreakAnim) {
            SPacketBlockBreakAnim packet = (SPacketBlockBreakAnim) event.getPacket();

            /// @todo: we can convert this to hashmap and have player breaking block.
            if (!BlocksBeingBroken.contains(packet.getPosition()) && (packet.getProgress() > 0 && packet.getProgress() <= 10)) {
                SendMessage(String.format("added: SPacketBlockBreakAnim %s %s %s", packet.getBreakerId(), packet.getPosition(), packet.getProgress()));
                BlocksBeingBroken.add(packet.getPosition());
            } else if (packet.getProgress() <= 0 || packet.getProgress() > 10) {
                SendMessage(String.format("removed: SPacketBlockBreakAnim %s %s %s", packet.getBreakerId(), packet.getPosition(), packet.getProgress()));
                BlocksBeingBroken.remove(packet.getPosition());
            }
        }
    });
    @EventHandler
    private final Listener<RenderEvent> OnRenderEvent = new Listener<>(event ->
    {
        if (mc.getRenderManager() == null)
            return;

        for (BlockPos pos : new ArrayList<BlockPos>(BlocksBeingBroken)) {
            if (pos == null)
                continue;

            if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR) {
                BlocksBeingBroken.remove(pos);
                continue;
            }

            final AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - mc.getRenderManager().viewerPosX,
                    pos.getY() - mc.getRenderManager().viewerPosY, pos.getZ() - mc.getRenderManager().viewerPosZ,
                    pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                    pos.getY() + (1) - mc.getRenderManager().viewerPosY,
                    pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY,
                    mc.getRenderViewEntity().posZ);

            if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                    bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY,
                    bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                glEnable(GL_LINE_SMOOTH);
                glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
                glLineWidth(1.5f);

                Render(bb, ObsidianRed.getValue(), ObsidianGreen.getValue(), ObsidianBlue.getValue(), ObsidianAlpha.getValue());

                glDisable(GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    });

    public BreakHighlightModule() {
        super("BreakHighlight", new String[]
                        {"BreakHighlights"}, "Highlights the blocks being broken around you",
                "NONE", -1, ModuleType.RENDER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        BlocksBeingBroken.clear();
    }

    @Override
    public void SendMessage(String msg) {
        if (DebugMsgs.getValue())
            super.SendMessage(msg);
    }

    private void Render(final AxisAlignedBB bb, float red, float green, float blue, float alpha) {
        switch (HighlightMode.getValue()) {
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

    private enum HoleModes {
        FlatOutline,
        Flat,
        Outline,
        Full,
    }
}
