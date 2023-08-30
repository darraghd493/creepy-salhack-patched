package me.ionar.salhack.gui.hud.components;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class BiomeComponent extends HudComponentItem {
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;
    public BiomeComponent() {
        super("Biome", 2, 95);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final BlockPos pos = mc.player.getPosition();
        final Chunk chunk = mc.world.getChunk(pos);
        final Biome biome = chunk.getBiome(pos, mc.world.getBiomeProvider());

        SetWidth(RenderUtil.getStringWidth(biome.getBiomeName()));
        SetHeight(RenderUtil.getStringHeight(biome.getBiomeName()));

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(biome.getBiomeName(), GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);
    }
}
