package me.ionar.salhack.module.combat;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.misc.MiddleClickFriendsModule;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;

//This code was taken from Seppuku Developement here: https://github.com/seppukudevelopment/seppuku. I removed the check to see if you are facing the ground and the ability to throw middle clicking a player if MiddleClickPlayer is enabled.
public class MiddleClickPearlModule extends Module {

    public final Value<Boolean> MiddleClickFriend = new Value<Boolean>("MiddleClickFriend", new String[]{"MDF"}, "Throw a pearl if middle click friend module is on.", false);
    private boolean clicked;
    private MiddleClickFriendsModule _mcf;
    @EventHandler
    private final Listener<EventPlayerUpdate> listener = new Listener<>(event -> {
        if (mc.currentScreen == null && Mouse.isButtonDown(2)) {
            if (!this.clicked) {

                if (!MiddleClickFriend.getValue() && mcfEnabled()) {
                    final RayTraceResult result = mc.objectMouseOver;

                    if (result == null || result.entityHit instanceof EntityPlayer) {
                        return;
                    }
                }

                final int pearlSLot = findPearlInHotbar();
                if (pearlSLot != -1) {
                    final int oldSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = pearlSLot;
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                    mc.player.inventory.currentItem = oldSlot;
                }
            }
            this.clicked = true;
        } else {
            this.clicked = false;
        }
    });

    public MiddleClickPearlModule() {
        super("MiddleClickPearl", new String[]{"mcp", "autopearl"}, "Throws a when if you middle-click.", "NONE", -1, ModuleType.COMBAT);
    }

    private boolean isItemStackPearl(final ItemStack itemStack) {
        return itemStack.getItem() instanceof ItemEnderPearl;
    }

    private int findPearlInHotbar() {
        for (int index = 0; InventoryPlayer.isHotbar(index); index++) {
            if (isItemStackPearl(mc.player.inventory.getStackInSlot(index))) return index;
        }
        return -1;
    }

    @Override
    public void init() {
        _mcf = (MiddleClickFriendsModule) ModuleManager.Get().GetMod(MiddleClickFriendsModule.class);
    }

    private boolean mcfEnabled() {
        return _mcf.isEnabled();
    }
}
