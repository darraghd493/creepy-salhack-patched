package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.tileentity.TileEntityChest;

/// @todo: Needs enum options

public class ChestCountComponent extends HudComponentItem {
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;
    public ChestCountComponent() {
        super("ChestCount", 2, 245);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        long chest = mc.world.loadedTileEntityList.stream()
                .filter(e -> e instanceof TileEntityChest).count();

        final String chests = hud.Rainbow.getValue() ? "Chests: " + chest : ChatFormatting.GRAY + "Chests: " + ChatFormatting.WHITE + chest;

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(chests, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);

        SetWidth(RenderUtil.getStringWidth(chests));
        SetHeight(RenderUtil.getStringHeight(chests));
    }
}
