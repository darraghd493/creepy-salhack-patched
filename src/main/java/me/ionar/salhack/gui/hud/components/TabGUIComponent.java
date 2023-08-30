package me.ionar.salhack.gui.hud.components;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.util.render.RenderUtil;

public class TabGUIComponent extends HudComponentItem {
    public TabGUIComponent() {
        super("TabGUI", 3, 12);
        //   SetHidden(false);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        int y = 0;

        RenderUtil.drawRect(GetX(), GetY(), GetX() + GetWidth(), GetY() + GetHeight(), 0x99443F3F);
        RenderUtil.drawOutlineRect(GetX(), GetY(), GetX() + GetWidth(), GetY() + GetHeight(), 3, 0x443F3F);

        String[] array = {"Combat", "Exploits", "Miscellaneous", "Movement", "Render", "World"};

        float maxWidth = 0;

        String hovered = "Combat";

        for (String string : array) {
            if (hovered == string) {
                RenderUtil.drawRect(GetX(), GetY(), GetX() + GetWidth(), GetY() + 12, 0x9902C9FF);
                RenderUtil.drawOutlineRect(GetX(), GetY(), GetX() + GetWidth(), GetY() + 12, 3, 0x99443F3F);
            }

            float width = RenderUtil.drawStringWithShadow(string, GetX() + 2, GetY() + y, 0xD1D1D1);

            if (width >= maxWidth)
                maxWidth = width;

            y += 11;
        }

        SetWidth(maxWidth + 3.5f);
        SetHeight(y);
    }
}
