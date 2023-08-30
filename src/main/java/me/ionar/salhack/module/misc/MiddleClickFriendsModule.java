package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;

public class MiddleClickFriendsModule extends Module {
    private boolean Clicked = false;
    @EventHandler
    private final Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (mc.currentScreen != null)
            return;

        if (!Mouse.isButtonDown(2)) {
            Clicked = false;
            return;
        }

        if (!Clicked) {
            Clicked = true;

            final RayTraceResult result = mc.objectMouseOver;

            if (result == null || result.typeOfHit != RayTraceResult.Type.ENTITY)
                return;

            Entity entity = result.entityHit;

            if (entity == null || !(entity instanceof EntityPlayer))
                return;

            if (FriendManager.Get().IsFriend(entity)) {
                FriendManager.Get().RemoveFriend(entity.getName().toLowerCase());
                SendMessage(String.format("%s has been removed.", entity.getName()));
            } else {
                FriendManager.Get().AddFriend(entity.getName().toLowerCase());
                SendMessage(String.format("%s has been added.", entity.getName()));
            }
        }
    });

    public MiddleClickFriendsModule() {
        super("MiddleClick", new String[]{"MCF", "MiddleClickF"}, "Middle click friends", "NONE", -1, ModuleType.MISC);
    }
}
