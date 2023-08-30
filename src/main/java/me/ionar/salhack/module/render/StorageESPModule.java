package me.ionar.salhack.module.render;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class StorageESPModule extends Module {
    public final Value<Boolean> EnderChests = new Value<Boolean>("EnderChests", new String[]{"S"}, "Highlights EnderChests", true);
    public final Value<Boolean> Chests = new Value<Boolean>("Chests", new String[]{"S"}, "Highlights Chests", true);
    public final Value<Boolean> Shulkers = new Value<Boolean>("Shulkers", new String[]{"S"}, "Highlights Shulkers", true);
    public final List<StorageBlockPos> Storages = new ArrayList<>();
    private final ICamera camera = new Frustum();
    @EventHandler
    private final Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        Storages.clear();

        mc.world.loadedTileEntityList.forEach(tile ->
        {
            if (tile instanceof TileEntityEnderChest && EnderChests.getValue())
                Storages.add(new StorageBlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), StorageType.Ender));
            else if (tile instanceof TileEntityChest && Chests.getValue())
                Storages.add(new StorageBlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), StorageType.Chest));
            else if (tile instanceof TileEntityShulkerBox && Shulkers.getValue())
                Storages.add(new StorageBlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), StorageType.Shulker));
        });
    });
    @EventHandler
    private final Listener<RenderEvent> OnRenderEvent = new Listener<>(event ->
    {
        if (mc.getRenderManager() == null || mc.getRenderManager().options == null)
            return;

        new ArrayList<StorageBlockPos>(Storages).forEach(pos ->
        {
            final AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - mc.getRenderManager().viewerPosX, pos.getY() - mc.getRenderManager().viewerPosY,
                    pos.getZ() - mc.getRenderManager().viewerPosZ, pos.getX() + 1 - mc.getRenderManager().viewerPosX, pos.getY() + 1 - mc.getRenderManager().viewerPosY,
                    pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

            if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX, bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY, bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                GlStateManager.pushMatrix();
                switch (pos.GetType()) {
                    case Chest:
                        RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, 0.94f, 1.0f, 0f, 0.6f);
                        break;
                    case Ender:
                        RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, 0.65f, 0f, 0.93f, 0.6f);
                        break;
                    case Shulker:
                        RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, 1.0f, 0.0f, 0.59f, 0.6f);
                        break;
                    default:
                        break;
                }

                GlStateManager.popMatrix();
            }
        });
    });

    public StorageESPModule() {
        super("StorageESP", new String[]{""}, "Highlights different kind of storages", "NONE", -1, ModuleType.RENDER);
    }

    public enum StorageType {
        Chest,
        Shulker,
        Ender,
    }

    public class StorageBlockPos extends BlockPos {
        public StorageType Type;

        public StorageBlockPos(int x, int y, int z, StorageType type) {
            super(x, y, z);

            Type = type;
        }

        public StorageType GetType() {
            return Type;
        }
    }
}
