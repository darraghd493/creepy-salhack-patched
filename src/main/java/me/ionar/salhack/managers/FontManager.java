package me.ionar.salhack.managers;

import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.util.render.SalFontRenderer;

public class FontManager {
    public SalFontRenderer[] FontRenderers = new SalFontRenderer[25];

    public SalFontRenderer TWCenMt18 = null;
    public SalFontRenderer TwCenMtStd28 = null;
    public SalFontRenderer VerdanaBold = null;

    public FontManager() {
    }

    public static FontManager Get() {
        return SalHack.GetFontManager();
    }

    public void Load() {
        TWCenMt18 = new SalFontRenderer("Tw Cen MT", 18);
        TwCenMtStd28 = new SalFontRenderer("Tw Cen MT Std", 28.14f);
        VerdanaBold = new SalFontRenderer("VerdanaBold", 20f);

        for (int i = 0; i < FontRenderers.length; ++i)
            FontRenderers[i] = new SalFontRenderer("Tw Cen MT", i);
    }

    public void LoadCustomFont(String customFont) {
        for (int i = 0; i < FontRenderers.length; ++i)
            FontRenderers[i] = new SalFontRenderer(customFont, i);
    }

    public SalFontRenderer GetFontBySize(int size) {
        if (size > FontRenderers.length)
            size = FontRenderers.length - 1;

        return FontRenderers[size];
    }

    public float DrawStringWithShadow(String name1, float x1, float y1, int color) {
        return FontRenderers[22].drawStringWithShadow(name1, x1, y1, color);
    }

    public float GetStringHeight(String name1) {
        return FontRenderers[22].getStringHeight(name1);
    }

    public float GetStringWidth(String name1) {
        return FontRenderers[22].getStringWidth(name1);
    }
}
