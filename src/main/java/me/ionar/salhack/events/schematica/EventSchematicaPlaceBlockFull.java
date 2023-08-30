package me.ionar.salhack.events.schematica;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class EventSchematicaPlaceBlockFull extends EventSchematicaPlaceBlock {
    public boolean Result = true;
    public Item ItemStack;

    public EventSchematicaPlaceBlockFull(BlockPos pos, Item itemStack) {
        super(pos);
        itemStack = ItemStack;
    }

    public boolean GetResult() {
        return Result;
    }
}
