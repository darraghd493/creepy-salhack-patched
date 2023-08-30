package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public final class NoFallModule extends Module {
    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]
            {"Mode", "M"}, "NoFall Module to use", Mode.Packet);

    public final Value<Boolean> NoVoid = new Value<Boolean>("NoVoid", new String[]
            {"NoVoid"}, "Prevents you from falling into the void", true);
    private boolean SendElytraPacket = false;
    private boolean SendInvPackets = false;
    private int ElytraSlot = -1;
    private final Timer ReplaceChestTimer = new Timer();
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (event.getEra() != Era.PRE)
            return;

        if (mc.player.isElytraFlying()) {
            if (SendElytraPacket && ReplaceChestTimer.passed(1000)) {
                SendElytraPacket = false;
                SendInvPackets = false;

                /// Replace it with old if need
                if (ElytraSlot != -1) {
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, ElytraSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, ElytraSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.updateController();

                    ElytraSlot = -1;
                    // SalHack.INSTANCE.logChat("Ez");
                }
            }

            return;
        }

        if (NoVoid.getValue()) {
            if (mc.player.posY <= 5.0f) {
                final RayTraceResult trace = mc.world.rayTraceBlocks(mc.player.getPositionVector(), new Vec3d(mc.player.posX, 0, mc.player.posZ), false, false, false);

                if (trace == null || trace.typeOfHit != RayTraceResult.Type.BLOCK) {
                    mc.player.setVelocity(0, 0, 0);
                }
            }
        }

        if (mc.player.fallDistance >= 3.0f) {
            int collisionHeight = -1;

            int distanceCheck = mode.getValue() == Mode.Bucket ? 5 : 8;

            for (int i = (int) mc.player.posY; i > mc.player.posY - distanceCheck; --i) {
                if (mc.world.isAirBlock(new BlockPos(mc.player.posX, i, mc.player.posZ)))
                    continue;

                if (mc.world.getBlockState(new BlockPos(mc.player.posX, i, mc.player.posZ)).getMaterial() == Material.WATER)
                    continue;

                collisionHeight = i;
                break;
                // getBlockState(pos).getBlock().isAir(this.getBlockState(pos), this, pos);
            }

            if (collisionHeight != -1) {
                if (mode.getValue() == Mode.Bucket) {
                    if (mc.player.getHeldItemMainhand().getItem() != Items.WATER_BUCKET) {
                        for (int i = 0; i < 9; ++i) {
                            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.WATER_BUCKET) {
                                mc.player.inventory.currentItem = i;
                                mc.playerController.updateController();
                                break;
                            }
                        }
                    }

                    mc.player.rotationPitch = 90;
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                } else if (mode.getValue() == Mode.Elytra) {
                    if (mc.player.fallDistance > 3.0f && !mc.player.onGround) {
                        /// Player must be wearing an elytra.
                        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
                            for (int i = 0; i < 44; ++i) {
                                ItemStack stack = mc.player.inventory.getStackInSlot(i);

                                if (stack.isEmpty() || stack.getItem() != Items.ELYTRA)
                                    continue;

                                ItemElytra elytra = (ItemElytra) stack.getItem();

                                if (elytra.getDurabilityForDisplay(stack) == 0)
                                    continue;

                                ElytraSlot = i;
                                break;
                            }

                            if (ElytraSlot != -1 && !SendInvPackets) {
                                SendInvPackets = true;
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, ElytraSlot, 0, ClickType.PICKUP, mc.player);
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, ElytraSlot, 0, ClickType.PICKUP, mc.player);
                                mc.playerController.updateController();
                                // SalHack.INSTANCE.logChat("ElytraSlot: " + ElytraSlot);
                            }

                            /// check again next tick
                            return;
                        }

                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_FALL_FLYING));
                        SendElytraPacket = true;
                        ReplaceChestTimer.reset();
                    }
                }
            }
        } else {
            SendInvPackets = false;
            SendElytraPacket = false;
            ReplaceChestTimer.reset();
        }
    });
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (event.getPacket() instanceof CPacketPlayer) {
            if (mc.player.isElytraFlying())
                return;

            final CPacketPlayer packet = (CPacketPlayer) event.getPacket();

            switch (mode.getValue()) {
                case Packet:
                    if (mc.player.fallDistance > 3.0f)
                        packet.onGround = true;
                    break;
                case Anti:
                    if (mc.player.fallDistance > 3.0f)
                        packet.y = mc.player.posY + 0.1f;
                    break;
                /*
                 * case AAC: if (mc.player.fallDistance > 3.0f) { mc.player.onGround = true; mc.player.capabilities.isFlying = true; mc.player.capabilities.allowFlying = true; packet.onGround = false;
                 * mc.player.velocityChanged = true; mc.player.capabilities.isFlying = false; mc.player.jump(); } break;
                 */
            }
        }
    });

    public NoFallModule() {
        super("NoFall", new String[]
                {"NoFallDamage"}, "Prevents fall damage", "NONE", 0x24DB95, ModuleType.MOVEMENT);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        SendElytraPacket = false;
        SendInvPackets = false;
    }

    @Override
    public String getMetaData() {
        return mode.getValue().toString();
    }

    private enum Mode {
        None,
        Packet,
        Anti,
        Bucket,
        Elytra
    }

}
