package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.network.NetworkPlayerInfo;

public class PingComponent extends HudComponentItem {
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;
    public PingComponent() {
        super("Ping", 2, 230);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.world == null || mc.player == null || mc.player.getUniqueID() == null)
            return;

        final NetworkPlayerInfo playerInfo = mc.getConnection().getPlayerInfo(mc.player.getUniqueID());

        if (playerInfo == null)
            return;

        final String ping = hud.Rainbow.getValue() ? "Ping " + playerInfo.getResponseTime() + "ms" : ChatFormatting.GRAY + "Ping " + ChatFormatting.WHITE + playerInfo.getResponseTime() + "ms";

        this.SetWidth(RenderUtil.getStringWidth(ping));
        this.SetHeight(RenderUtil.getStringHeight(ping) + 1);

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(ping, this.GetX(), this.GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);
    }

}
