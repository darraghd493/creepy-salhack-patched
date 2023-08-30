package me.ionar.salhack.module.world;

import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

import java.util.Comparator;

public class AutoNameTagModule extends Module {
    public final Value<Integer> Radius = new Value<Integer>("Radius", new String[]{"R"}, "Radius to search for entities", 4, 0, 10, 1);
    public final Value<Boolean> ReplaceOldNames = new Value<Boolean>("ReplaceOldNames", new String[]{""}, "Automatically replaces old names of the mobs if a previous nametag was used", true);
    public final Value<Boolean> AutoSwitch = new Value<Boolean>("AutoSwitch", new String[]{""}, "Automatically switches to a nametag in your hotbar", false);
    public final Value<Boolean> WithersOnly = new Value<Boolean>("WithersOnly", new String[]{""}, "Only renames withers", true);
    public final Value<Float> Delay = new Value<Float>("Delay", new String[]{"D"}, "Delay to use", 1.0f, 0.0f, 10.0f, 1.0f);
    private final Timer timer = new Timer();
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (mc.currentScreen != null)
            return;

        if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemNameTag)) {
            int slot = -1;

            if (AutoSwitch.getValue()) {
                for (int i = 0; i < 9; ++i) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);

                    if (stack.isEmpty())
                        continue;

                    if (stack.getItem() instanceof ItemNameTag) {
                        if (!stack.hasDisplayName())
                            continue;

                        slot = i;
                        mc.player.inventory.currentItem = slot;
                        mc.playerController.updateController();
                        break;
                    }
                }
            }

            if (slot == -1)
                return;
        }

        ItemStack stack = mc.player.getHeldItemMainhand();

        if (!stack.hasDisplayName())
            return;

        EntityLivingBase entity = mc.world.loadedEntityList.stream()
                .filter(entity1-> IsValidEntity(entity1, stack.getDisplayName()))
                .map(entity1 -> (EntityLivingBase) entity1)
                .min(Comparator.comparing(entity1 -> mc.player.getDistance(entity1)))
                .orElse(null);

        if (entity != null) {
            timer.reset();
            event.cancel();

            final double[] pos = EntityUtil.calculateLookAt(
                    entity.posX,
                    entity.posY,
                    entity.posZ,
                    mc.player);

            SendMessage(String.format("Gave %s the nametag of %s", entity.getName(), stack.getDisplayName()));

            mc.player.rotationYawHead = (float) pos[0];

            PlayerUtil.PacketFacePitchAndYaw((float) pos[1], (float) pos[0]);

            mc.getConnection().sendPacket(new CPacketUseEntity(entity, EnumHand.MAIN_HAND));
        }
    });

    public AutoNameTagModule() {
        super("AutoNameTag", new String[]{""}, "Automatically name tags entities in range, if they meet the requirements.", "NONE", -1, ModuleType.MISC);
    }

    private boolean IsValidEntity(Entity entity, final String name1) {
        if (!(entity instanceof EntityLivingBase))
            return false;

        if (entity.getDistance(mc.player) > Radius.getValue())
            return false;

        if (entity instanceof EntityPlayer)
            return false;

        if (!entity.getCustomNameTag().isEmpty() && !ReplaceOldNames.getValue())
            return false;

        if (ReplaceOldNames.getValue()) {
            if (!entity.getCustomNameTag().isEmpty() && entity.getName().equals(name1))
                return false;
        }

        return !WithersOnly.getValue() || entity instanceof EntityWither;
    }
}
