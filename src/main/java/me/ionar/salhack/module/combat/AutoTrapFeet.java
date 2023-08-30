package me.ionar.salhack.module.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import me.ionar.salhack.util.MathUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;

public class AutoTrapFeet extends Module {
    public final Value<Float> Distance = new Value<Float>("Distance", new String[]{"Dist"}, "Distance to start searching for targets", 5.0f, 0.0f, 10.0f, 1.0f);
    public final Value<Boolean> rotate = new Value<Boolean>("rotate", new String[]
            {"rotate"}, "Rotate", true);
    public final Value<Integer> BlocksPerTick = new Value<Integer>("BlocksPerTick", new String[]{"BPT"}, "Blocks per tick", 4, 1, 10, 1);
    public final Value<Boolean> Toggles = new Value<Boolean>("Toggles", new String[]
            {"Toggles"}, "Toggles off after a trap", false);
    EntityPlayer Target = null;
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (event.getEra() != Era.PRE)
            return;

        if (Target == null) {
            return;
        }

        if (IsCurrentTargetTrapped()) {
            if (Toggles.getValue()) {
                toggle();
                SalHack.SendMessage(ChatFormatting.LIGHT_PURPLE + "[AutoTrapFeet]: Current target is trapped. Toggling");
            }
            return;
        }

        if (!HasObsidian())
            return;

        DecimalFormat format = new DecimalFormat("#.###");

        final Vec3d pos = new Vec3d(Double.valueOf(format.format(Target.posX)), Target.posY, Double.valueOf(format.format(Target.posZ)));
        final float playerSpeed = (float) MathUtil.getDistance(pos, Target.posX, Target.posY, Target.posZ);

        final BlockPos interpPos = new BlockPos(pos.x, pos.y, pos.z);

        BlockPos northAbove = interpPos.north().up();
        BlockPos southAbove = interpPos.south().up();
        BlockPos eastAbove = interpPos.east().up();
        BlockPos westAbove = interpPos.west().up();

        BlockPos topBlock = interpPos.up().up();

        final BlockPos[] array = {northAbove, southAbove, eastAbove, westAbove, topBlock};

        int lastSlot;
        final int slot = findStackHotbar(Blocks.OBSIDIAN);
        if (hasStack(Blocks.OBSIDIAN) || slot != -1) {
            if ((mc.player.onGround && playerSpeed <= 0.005f)) {
                lastSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();

                int blocksPerTick = BlocksPerTick.getValue();

                for (BlockPos pos1 : array) {
                    ValidResult result = BlockInteractionHelper.valid(pos1);

                    /// if already a block there, ignore it, it's satisfied
                    if (result == ValidResult.AlreadyBlockThere)
                        continue;

                    if (result == ValidResult.NoNeighbors) {
                        final BlockPos[] test = {pos1.north(), pos1.south(), pos1.east(), pos1.west(), pos1.up(), pos1.down()};

                        PlaceResult placeResult = PlaceResult.CantPlace;

                        for (BlockPos pos2 : test) {
                            ValidResult result2 = BlockInteractionHelper.valid(pos2);

                            if (result2 == ValidResult.NoNeighbors || result2 == ValidResult.NoEntityCollision)
                                continue;

                            placeResult = BlockInteractionHelper.place(pos2, Distance.getValue(), rotate.getValue(), false);
                            break;
                        }

                        if (placeResult != PlaceResult.CantPlace)
                            break;
                    }

                    PlaceResult resultPlace = BlockInteractionHelper.place(pos1, Distance.getValue(), rotate.getValue(), false);

                    if (resultPlace == PlaceResult.Placed) {
                        if (--blocksPerTick <= 0)
                            break;
                    }
                    /*else if (resultPlace == PlaceResult.CantPlace)
                    {
                        final BlockPos[] test = { pos.north(), pos.south(), pos.east(), pos.west(), pos.up(), pos.down() };

                        for (BlockPos pos2 : test)
                        {
                            final BlockPos[] test2 = { pos2.north(), pos2.south(), pos2.east(), pos2.west(), pos2.up(), pos2.down() };
                            for (BlockPos pos3 : test2)
                            {
                                ValidResult result2 = BlockInteractionHelper.valid(pos3);

                                if (result2 != ValidResult.Ok)
                                {
                                    final BlockPos[] test3 = { pos2.north(), pos2.south(), pos2.east(), pos2.west(), pos2.up(), pos2.down() };
                                    for (BlockPos pos4 : test3)
                                    {
                                        ValidResult result3 = BlockInteractionHelper.valid(pos4);

                                        if (result3 != ValidResult.Ok)
                                        {

                                            continue;
                                        }

                                        BlockInteractionHelper.place (pos4, Distance.getValue(), rotate.getValue());
                                    }
                                    continue;
                                }

                                BlockInteractionHelper.place (pos3, Distance.getValue(), rotate.getValue());
                            }
                        }

                    }*/
                    else {
                        //    SalHack.SendMessage(String.format("Can't place at %s because of %s", pos.toString(), resultPlace.toString()));
                    }
                }

                if (!slotEqualsBlock(lastSlot, Blocks.OBSIDIAN)) {
                    mc.player.inventory.currentItem = lastSlot;
                }
                mc.playerController.updateController();
            }
        }
    });

    public AutoTrapFeet() {
        super("AutoTrapFeet", new String[]
                {"AutoTrapFeet"}, "Traps enemies with holes at their feet", "NONE", 0x24DB78, ModuleType.COMBAT);
    }

    @Override
    public void toggleNoSave() {

    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (mc.world == null || mc.player == null) {
            toggle();
            return;
        }

        float lastDist = 100.0f;

        for (EntityPlayer player : mc.world.playerEntities) {
            if (player == null || player == mc.player)
                continue;

            if (FriendManager.Get().IsFriend(player))
                continue;

            float dist = player.getDistance(mc.player);

            if (dist > Distance.getValue())
                continue;

            if (lastDist > dist) {
                Target = player;
                lastDist = dist;
            }
        }

        // Target = mc.world.playerEntities.stream()
        //         .filter(entity -> Target != mc.player && Target.getName() != mc.player.getName())
        //         .min(Comparator.comparing(entity -> mc.player.getDistance(entity)))
        //         .orElse(null);

        if (Target != null) {
            SalHack.SendMessage("[AutoTrapFeet]: Found target " + Target.getName());
        }
    }

    public boolean IsCurrentTargetTrapped() {
        if (Target == null)
            return true;

        DecimalFormat format = new DecimalFormat("#.###");

        final Vec3d playerPos = new Vec3d(Double.valueOf(format.format(Target.posX)), Target.posY, Double.valueOf(format.format(Target.posZ)));

        final BlockPos interPos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);

        BlockPos north = interPos.north().up();
        BlockPos south = interPos.south().up();
        BlockPos east = interPos.east().up();
        BlockPos west = interPos.west().up();

        BlockPos top = interPos.up().up();

        final BlockPos[] array = {north, south, east, west, top};

        for (BlockPos pos : array) {
            if (BlockInteractionHelper.valid(pos) != ValidResult.AlreadyBlockThere) {
                return false;
            }
        }

        return true;
    }

    public boolean hasStack(Block type) {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemBlock) {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getCurrentItem().getItem();
            return block.getBlock() == type;
        }
        return false;
    }

    public boolean slotEqualsBlock(int slot, Block type) {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock) {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }

        return false;
    }

    public int findStackHotbar(Block type) {
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock) {
                final ItemBlock block = (ItemBlock) stack.getItem();

                if (block.getBlock() == type) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean HasObsidian() {
        return findStackHotbar(Blocks.OBSIDIAN) != -1;
    }
}
