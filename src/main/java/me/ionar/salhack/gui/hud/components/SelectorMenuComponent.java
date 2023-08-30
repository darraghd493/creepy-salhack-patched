package me.ionar.salhack.gui.hud.components;

import me.ionar.salhack.gui.click.component.menus.mods.MenuComponentHUDList;
import me.ionar.salhack.gui.hud.HudComponentItem;

public class SelectorMenuComponent extends HudComponentItem {
    MenuComponentHUDList component = new MenuComponentHUDList("Selector", 300, 300);

    public SelectorMenuComponent() {
        super("Selector", 300, 300);
        SetHidden(false);
        AddFlag(HudComponentItem.OnlyVisibleInHudEditor);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        component.Render(mouseX, mouseY, true, true, 0);

        SetWidth(component.GetWidth());
        SetHeight(component.GetHeight());
        SetX(component.GetX());
        SetY(component.GetY());
    }

    @Override
    public boolean OnMouseClick(int mouseX, int mouseY, int mouseButton) {
        return component.MouseClicked(mouseX, mouseY, mouseButton, 0);
    }

    @Override
    public void OnMouseRelease(int mouseX, int mouseY, int state) {
        super.OnMouseRelease(mouseX, mouseY, state);
        component.MouseReleased(mouseX, mouseY, state);
    }
}
