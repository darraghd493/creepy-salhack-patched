package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.item.ItemStack;

public class EventRenderTooltip extends MinecraftEvent {
    private final ItemStack Item;
    private final int X;
    private final int Y;

    public EventRenderTooltip(ItemStack stack, int x1, int y1) {
        Item = stack;
        X = x1;
        Y = y1;
    }

    public ItemStack getItemStack() {
        return Item;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

}
