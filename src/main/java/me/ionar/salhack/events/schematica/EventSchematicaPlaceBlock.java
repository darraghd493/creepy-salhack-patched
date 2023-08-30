package me.ionar.salhack.events.schematica;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.util.math.BlockPos;

public class EventSchematicaPlaceBlock extends MinecraftEvent {
    public BlockPos Pos;

    public EventSchematicaPlaceBlock(BlockPos pos) {
        Pos = pos;
    }
}
