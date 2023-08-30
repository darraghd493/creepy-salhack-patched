package me.ionar.salhack.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class Hole extends Vec3i {
    private final BlockPos blockPos;
    private boolean tall;
    private HoleTypes HoleType;

    public Hole(int x, int y, int z, final BlockPos pos, HoleTypes type) {
        super(x, y, z);
        blockPos = pos;
        SetHoleType(type);
    }

    public Hole(int x, int y, int z, final BlockPos pos, HoleTypes type, boolean tall) {
        super(x, y, z);
        blockPos = pos;
        this.tall = true;
        SetHoleType(type);
    }

    public boolean isTall() {
        return tall;
    }

    public BlockPos GetBlockPos() {
        return blockPos;
    }

    /**
     * @return the holeType
     */
    public HoleTypes GetHoleType() {
        return HoleType;
    }

    /**
     * @param holeType the holeType to set
     */
    public void SetHoleType(HoleTypes holeType) {
        HoleType = holeType;
    }

    public enum HoleTypes {
        None,
        Normal,
        Obsidian,
        Bedrock,
    }
}