package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;

public class PlayerCountComponent extends HudComponentItem {
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;
    public PlayerCountComponent() {
        super("PlayerCount", 2, 185);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final String playerCount = hud.Rainbow.getValue() ? "Players: " + mc.player.connection.getPlayerInfoMap().size() : ChatFormatting.GRAY + "Players: " + ChatFormatting.WHITE + mc.player.connection.getPlayerInfoMap().size();

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(playerCount, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);

        SetWidth(RenderUtil.getStringWidth(playerCount));
        SetHeight(RenderUtil.getStringHeight(playerCount) + 1);
    }

}
