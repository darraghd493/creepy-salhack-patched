package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.module.world.CoordsSpooferModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;

import java.text.DecimalFormat;

public class CoordsComponent extends HudComponentItem {
    public final Value<Boolean> NetherCoords = new Value<Boolean>("NetherCoords", new String[]
            {"NC"}, "Displays nether coords.", true);
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]
            {"Mode"}, "Mode of displaying coordinates", Modes.Inline);
    final DecimalFormat Formatter = new DecimalFormat("#.#");
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;
    private final CoordsSpooferModule _getCoords = (CoordsSpooferModule) ModuleManager.Get().GetMod(CoordsSpooferModule.class);
    public CoordsComponent() {
        super("Coords", 2, 245);
    }

    public String format(double input) {
        String result = Formatter.format(input);

        if (!result.contains("."))
            result += ".0";

        return result;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        switch (Mode.getValue()) {
            case Inline:
                String coords = hud.Rainbow.getValue() ? String.format("XYZ %s, %s, %s",
                        format(getX()), format(mc.player.posY), format(getZ()))
                        : String.format("%sXYZ %s%s, %s, %s", ChatFormatting.GRAY, ChatFormatting.WHITE,
                        format(getX()), format(mc.player.posY), format(getZ()));

                if (NetherCoords.getValue()) {
                    coords += hud.Rainbow.getValue() ? String.format(" [%s, %s]",
                            mc.player.dimension != -1 ? format(getX() / 8) : format(getX() * 8),
                            mc.player.dimension != -1 ? format(getZ() / 8) : format(getZ() * 8))
                            : String.format(" %s[%s%s, %s%s]", ChatFormatting.GRAY, ChatFormatting.WHITE,
                            mc.player.dimension != -1 ? format(getX() / 8) : format(getX() * 8),
                            mc.player.dimension != -1 ? format(getZ() / 8) : format(getZ() * 8),
                            ChatFormatting.GRAY);
                }
                SetWidth(RenderUtil.getStringWidth(coords));
                SetHeight(RenderUtil.getStringHeight(coords));

                Rainbow.OnRender();
                RenderUtil.drawStringWithShadow(coords, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);

                break;
            case NextLine:
                String x = hud.Rainbow.getValue() ? String.format("X: %s [%s]", format(getX()), NetherCoords.getValue() ? mc.player.dimension != -1 ? format(getX() / 8) : format(getX() * 8) : "") : String.format("%sX: %s%s [%s]", ChatFormatting.GRAY, ChatFormatting.WHITE, format(getX()), NetherCoords.getValue() ? mc.player.dimension != -1 ? format(getX() / 8) : format(getX() * 8) : "");
                String y = hud.Rainbow.getValue() ? String.format("Y: %s [%s]", format(mc.player.posY), NetherCoords.getValue() ? format(mc.player.posY) : "") : String.format("%sY: %s%s [%s]", ChatFormatting.GRAY, ChatFormatting.WHITE, format(mc.player.posY), NetherCoords.getValue() ? format(mc.player.posY) : "");
                String z = hud.Rainbow.getValue() ? String.format("Z: %s [%s]", format(getZ()), NetherCoords.getValue() ? mc.player.dimension != -1 ? format(getZ() / 8) : format(getZ() * 8) : "") : String.format("%sZ: %s%s [%s]", ChatFormatting.GRAY, ChatFormatting.WHITE, format(getZ()), NetherCoords.getValue() ? mc.player.dimension != -1 ? format(getZ() / 8) : format(getZ() * 8) : "");
                Rainbow.OnRender();
                RenderUtil.drawStringWithShadow(x, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);
                RenderUtil.drawStringWithShadow(y, GetX(), GetY() + RenderUtil.getStringHeight(x), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);
                RenderUtil.drawStringWithShadow(z, GetX(), GetY() + RenderUtil.getStringHeight(x) + RenderUtil.getStringHeight(y), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);

                SetWidth(RenderUtil.getStringWidth(x));
                SetHeight(RenderUtil.getStringHeight(x) * 3);
                break;
            default:
                break;
        }

    }

    private Boolean getCoordSpoofer() {
        return _getCoords.isEnabled();
    }

    private int randX() {
        int i = (int) (Math.random() * 2) + 1;
        if (i == 1) {
            i = (int) ((Math.random() * 30000000) + 0) * -1;
        } else {
            i = (int) ((Math.random() * 30000000) + 0);
        }
        return i;
    }

    private int randZ() {
        int i = (int) (Math.random() * 2) + 1;
        if (i == 1) {
            i = (int) ((Math.random() * 30000000) + 0) * -1;
        } else {
            i = (int) ((Math.random() * 30000000) + 0);
        }
        return i;
    }

    private double getX() {
        if (getCoordSpoofer()) {
            return mc.player.posX + randX();
        }
        return mc.player.posX;
    }

    private double getZ() {
        if (getCoordSpoofer()) {
            return mc.player.posZ + randZ();
        }
        return mc.player.posZ;
    }

    public enum Modes {
        Inline, NextLine,
    }

}
