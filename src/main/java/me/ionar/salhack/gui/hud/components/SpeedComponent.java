package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;

//I got lazy and took this from https://github.com/pleasegivesource/SalHackSkid.
public class SpeedComponent extends HudComponentItem {
    public final Value<UnitList> SpeedUnit = new Value<UnitList>("Speed Unit", new String[]{"SpeedUnit"}, "Unit of speed. Note that 1 metre = 1 block", UnitList.BPS);
    final DecimalFormat FormatterBPS = new DecimalFormat("#.#");
    final DecimalFormat FormatterKMH = new DecimalFormat("#.#");
    private double PrevPosX;
    private double PrevPosZ;
    private final Timer timer = new Timer();
    private String speed = "";
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;
    public SpeedComponent() {
        super("Speed", 2, 80);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (timer.passed(1000)) {
            PrevPosX = mc.player.prevPosX;
            PrevPosZ = mc.player.prevPosZ;
        }

        final double deltaX = mc.player.posX - PrevPosX;
        final double deltaZ = mc.player.posZ - PrevPosZ;

        float distance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double bPS = distance * 20;
        double kMH = Math.floor((distance / 1000.0f) / (0.05f / 3600.0f));

        if (SpeedUnit.getValue() == UnitList.BPS) {
            String formatterBPS = FormatterBPS.format(bPS);

            //TODO Change BPS to m/s? 1 minecraft block is 1 real life metre iirc.
            speed = hud.Rainbow.getValue() ? "Speed: " + formatterBPS + " BPS" : ChatFormatting.GRAY + "Speed: " + ChatFormatting.WHITE + formatterBPS + " BPS";

        } else if (SpeedUnit.getValue() == UnitList.KMH) {
            String formatterKMH = FormatterKMH.format(kMH);

            speed = hud.Rainbow.getValue() ? "Speed " + formatterKMH + "km/h" : ChatFormatting.GRAY + "Speed " + ChatFormatting.WHITE + formatterKMH + "km/h";

        }

        SetWidth(RenderUtil.getStringWidth(speed));
        SetHeight(RenderUtil.getStringHeight(speed) + 1);

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(speed, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);
    }

    public enum UnitList {
        BPS,
        KMH,
    }
}
