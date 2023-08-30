package me.ionar.salhack.gui.hud;

import me.ionar.salhack.gui.SalGuiScreen;
import me.ionar.salhack.managers.HudManager;
import me.ionar.salhack.module.ui.HudEditorModule;
import me.ionar.salhack.util.render.RenderUtil;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiHudEditor extends SalGuiScreen {
    private final HudEditorModule HudEditor;
    private boolean Clicked = false;
    private boolean Dragging = false;
    private int ClickMouseX = 0;
    private int ClickMouseY = 0;
    public GuiHudEditor(HudEditorModule hudEditor1) {
        super();

        HudEditor = hudEditor1;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawDefaultBackground();

        GL11.glPushMatrix();

        HudComponentItem lastHovered = null;

        for (HudComponentItem item : HudManager.Get().Items) {
            if (!item.IsHidden() && item.Render(mouseX, mouseY, partialTicks))
                lastHovered = item;
        }

        if (lastHovered != null) {
            /// Add to the back of the list for rendering
            HudManager.Get().Items.remove(lastHovered);
            HudManager.Get().Items.add(lastHovered);
        }

        if (Clicked) {
            final float mouseX1 = Math.min(ClickMouseX, mouseX);
            final float mouseX2 = Math.max(ClickMouseX, mouseX);
            final float mouseY1 = Math.min(ClickMouseY, mouseY);
            final float mouseY2 = Math.max(ClickMouseY, mouseY);

            RenderUtil.drawOutlineRect(mouseX2, mouseY2, mouseX1, mouseY1, 1, 0x75056EC6);
            RenderUtil.drawRect(mouseX1, mouseY1, mouseX2, mouseY2, 0x56EC6, 205);

            HudManager.Get().Items.forEach(item ->
            {
                if (!item.IsHidden()) {
                    if (item.IsInArea(mouseX1, mouseX2, mouseY1, mouseY2))
                        item.SetSelected(true);
                    else if (item.IsSelected())
                        item.SetSelected(false);
                }
            });
        }

        GL11.glPopMatrix();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for (HudComponentItem item : HudManager.Get().Items) {
            if (!item.IsHidden()) {
                if (item.OnMouseClick(mouseX, mouseY, mouseButton))
                    return;
            }
        }

        Clicked = true;
        ClickMouseX = mouseX;
        ClickMouseY = mouseY;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        HudManager.Get().Items.forEach(item ->
        {
            if (!item.IsHidden()) {
                item.OnMouseRelease(mouseX, mouseY, state);

                item.SetMultiSelectedDragging(item.IsSelected());
            }
        });

        Clicked = false;
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        if (HudEditor.isEnabled())
            HudEditor.toggle();

        Clicked = false;
        Dragging = false;
        ClickMouseX = 0;
        ClickMouseY = 0;
    }
}
