package me.ionar.salhack.gui.click.component;

import me.ionar.salhack.gui.click.component.item.ComponentItem;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.managers.FontManager;
import me.ionar.salhack.managers.ImageManager;
import me.ionar.salhack.module.ui.ClickGuiModule;
import me.ionar.salhack.module.ui.ColorsModule;
import me.ionar.salhack.util.imgs.SalDynamicTexture;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class MenuComponent {
    final float BorderLength = 15.0f;
    final float Padding = 3;
    protected ArrayList<ComponentItem> Items = new ArrayList<ComponentItem>();
    private final String DisplayName;
    private final float DefaultX;
    private final float DefaultY;
    private float X;
    private float Y;
    private float Height;
    private final float Width;
    private boolean Dragging = false;
    private float DeltaX = 0;
    private float DeltaY = 0;
    private ComponentItem HoveredItem = null;
    private boolean Minimized = false;
    private boolean IsMinimizing = false;
    private float RemainingMinimizingY;
    private boolean IsMaximizing = false;
    private float RemainingMaximizingY;
    private int MousePlayAnim;
    private SalDynamicTexture BarTexture = null;
    private final ColorsModule Colors;
    private final ClickGuiModule ClickGUI;

    public MenuComponent(String displayName, float x1, float y1, float height1, float width1, String image, ColorsModule colors, ClickGuiModule clickGui) {
        DisplayName = displayName;
        DefaultX = x1;
        DefaultY = y1;
        X = x1;
        Y = y1;
        Height = height1;
        Width = width1;
        RemainingMinimizingY = 0;
        RemainingMaximizingY = 0;
        MousePlayAnim = 0;

        if (image != null) {
            BarTexture = ImageManager.Get().GetDynamicTexture(image);
        }

        Colors = colors;
        ClickGUI = clickGui;
    }

    public void AddItem(ComponentItem item) {
        Items.add(item);
    }

    public boolean Render(int mouseX, int mouseY, boolean canHover, boolean allowsOverflow, float offsetY) {
        if (Dragging) {
            X = mouseX - DeltaX;
            Y = mouseY - DeltaY;
        }

        if (!allowsOverflow) {
            ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

            /// Don't allow too much to right, or left
            if (X + GetWidth() >= res.getScaledWidth())
                X = res.getScaledWidth() - GetWidth();
            else if (X < 0)
                X = 0;

            if (Y + GetHeight() >= res.getScaledHeight())
                Y = res.getScaledHeight() - GetHeight();
            else if (Y < 0)
                Y = 0;
        }

        for (ComponentItem item : Items)
            item.OnMouseMove(mouseX, mouseY, GetX(), GetY() - offsetY);

        if (IsMinimizing) {
            if (RemainingMinimizingY > 0) {
                RemainingMinimizingY -= 20;

                RemainingMinimizingY = Math.max(RemainingMinimizingY, 0);

                if (RemainingMinimizingY == 0) {
                    Minimized = true;
                    IsMinimizing = false;
                    Height = 0;
                }
            }
        } else if (IsMaximizing) {
            if (RemainingMaximizingY < 500) {
                RemainingMaximizingY += 20;

                RemainingMaximizingY = Math.min(RemainingMaximizingY, 500);

                if (RemainingMaximizingY == 500) {
                    IsMaximizing = false;
                    Height = 0;
                }
            }
        }

        RenderUtil.drawGradientRect(GetX(), GetY() + 17 - offsetY, GetX() + GetWidth(), GetY() + GetHeight(), 0x992A2A2A, 0x992A2A2A);

        RenderUtil.drawRect(GetX(), GetY() - offsetY, GetX() + GetWidth(), GetY() + 17 - offsetY, 0x99000000); /// top
        FontManager.Get().TwCenMtStd28.drawStringWithShadow(GetDisplayName(), GetX() + 2, GetY() + 1 - offsetY + 2.50f, GetTextColor());


        if (BarTexture != null) {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();

            float x = GetX() + GetWidth() - 15;

            Wrapper.GetMC().renderEngine.bindTexture(BarTexture.GetResourceLocation());
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GlStateManager.color(Colors.ImageRed.getValue(), Colors.ImageGreen.getValue(), Colors.ImageBlue.getValue(), Colors.ImageAlpha.getValue());
            GlStateManager.enableTexture2D();
            //   public static void drawTexture(float x, float y, float width, float height, float u, float v, float t, float s)
            RenderUtil.drawTexture(x, GetY() + 3 - offsetY, BarTexture.GetWidth() / 3, BarTexture.GetHeight() / 3, 0, 0, 1, 1);
            
            /*
            
            //SalHack.INSTANCE.logChat("Opacity is " + Opacity);            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0FF);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            
            Wrapper.GetMC().renderEngine.bindTexture(BarTexture.GetResourceLocation());
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
    
            /// public static void blit(int x, int y, float minU, float minV, int width, int height, int uScale, int vScale)
          //  AbstractGui.blit(GetX(), GetY(), 1f, 1f, GetX(), GetY(), 1, 1);
            
            int x = (int) (GetX()+GetWidth()-15);

            AbstractGui.blit((int) x, (int) GetY()+3, BarTexture.GetWidth()/3, BarTexture.GetHeight()/3, 0.0f, 0.0f,  BarTexture.GetWidth()/4, BarTexture.GetHeight()/4,
                    BarTexture.GetWidth()/4, BarTexture.GetHeight()/4);
            
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.4F);
            GlStateManager.disableBlend();*/

            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }

        if (!Minimized) {
            float y = GetY() + 5 - offsetY;

            HoveredItem = null;

            boolean break1 = false;

            for (ComponentItem item : Items) {
                y = DisplayComponentItem(item, y, mouseX, mouseY, canHover, false, IsMinimizing ? RemainingMinimizingY : (IsMaximizing ? RemainingMaximizingY : 0));

                float menuY = Math.abs(Y - y - BorderLength);

                if (IsMinimizing && menuY >= RemainingMinimizingY)
                    break1 = true;
                else if (IsMaximizing && menuY >= RemainingMaximizingY)
                    break1 = true;

                if (break1)
                    break;
            }

            if (!break1)
            {
                IsMinimizing = false;
                IsMaximizing = false;
            }

            if (HoveredItem != null && (ClickGUI != null ? ClickGUI.HoverDescriptions.getValue() : true)) {
                if (HoveredItem.GetDescription() != null && HoveredItem.GetDescription() != "") {
                    RenderUtil.drawRect(mouseX + 15, mouseY, mouseX + 19 + RenderUtil.getStringWidth(HoveredItem.GetDescription()), mouseY + RenderUtil.getStringHeight(HoveredItem.GetDescription()) + 3, 0x90000000);
                    RenderUtil.drawStringWithShadow(HoveredItem.GetDescription(), mouseX + 17, mouseY, 0xFFFFFF);
                }
            }

            Height = Math.abs(Y - y - 12);
        }

        if (MousePlayAnim > 0) {
            MousePlayAnim--;

            RenderUtil.DrawPolygon(mouseX, mouseY, MousePlayAnim, 360, 0x99FFFFFF);
        }

        return canHover && mouseX > GetX() && mouseX < GetX() + GetWidth() && mouseY > GetY() - offsetY && mouseY < GetY() + GetHeight() - offsetY;
    }

    public float DisplayComponentItem(ComponentItem item, float y1, int mouseX, int mouseY, boolean canHover, boolean displayExtendedLine, final float maxY) {
        y1 += item.GetHeight();

        item.OnMouseMove(mouseX, mouseY, GetX(), GetY());
        item.Update();

        if (item.HasState(ComponentItem.Extended)) {
            RenderUtil.drawRect(X + 1, y1, X + item.GetWidth() - 3, y1 + RenderUtil.getStringHeight(item.GetDisplayText()) + 3, 0x080808);
        }

        int color = 0xFFFFFF;

        boolean hovered = canHover && mouseX > X && mouseX < X + item.GetWidth() && mouseY > y1 && mouseY < y1 + item.GetHeight();

        boolean dropDown = item.HasState(ComponentItem.Extended);

        if (hovered) {
            if (!dropDown)
                RenderUtil.drawGradientRect(GetX(), y1, GetX() + item.GetWidth(), y1 + 11, 0x99040404, 0x99000000);
            color = (item.HasState(ComponentItem.Clicked) && !item.HasFlag(ComponentItem.DontDisplayClickableHighlight)) ? GetTextColor() : color;// - commented for issue #27
            HoveredItem = item;

            item.AddState(ComponentItem.Hovered);
        } else {
            if (item.HasState(ComponentItem.Clicked) && !item.HasFlag(ComponentItem.DontDisplayClickableHighlight))
                color = GetTextColor();

            item.RemoveState(ComponentItem.Hovered);
        }

        if (dropDown)
            RenderUtil.drawGradientRect(GetX(), y1, GetX() + item.GetWidth(), y1 + 11, 0x99040404, 0x99000000);

        if (item.HasFlag(ComponentItem.RectDisplayAlways) || (item.HasFlag(ComponentItem.RectDisplayOnClicked) && item.HasState(ComponentItem.Clicked)))
            RenderUtil.drawRect(GetX(), y1, GetX() + item.GetCurrentWidth(), y1 + 11, GetColor());

        RenderUtil.drawStringWithShadow(item.GetDisplayText(), X + Padding, y1, color);

        /*if (item.HasFlag(ComponentItem.HasValues))
        {
            RenderUtil.drawLine(X + item.GetWidth() - 1, y1, X + item.GetWidth() - 1, y1 + 11, 5, 0x9945B5E4);
        }*/

        if (item.HasState(ComponentItem.Extended) || displayExtendedLine) {
            RenderUtil.drawLine(X + item.GetWidth() - 1, y1, X + item.GetWidth() - 1, y1 + 11, 3, GetColor());
        }

        if (item.HasState(ComponentItem.Extended)) {
            for (ComponentItem valItem : item.DropdownItems) {
                y1 = DisplayComponentItem(valItem, y1, mouseX, mouseY, canHover, true, maxY);

                if (maxY > 0) {
                    float menuY = Math.abs(Y - y1 - BorderLength);

                    if (menuY >= maxY)
                        break;
                }
            }
        }

        return y1;
    }

    public boolean MouseClicked(int mouseX, int mouseY, int mouseButton, float offsetY) {
        if (mouseX > GetX() && mouseX < GetX() + GetWidth() && mouseY > GetY() - offsetY && mouseY < GetY() + BorderLength - offsetY) {
            /// Dragging (Top border)
            if (mouseButton == 0) {
                Dragging = true;
                DeltaX = mouseX - X;
                DeltaY = mouseY - Y;
            } else if (mouseButton == 1) {
                /// Right click
                if (!Minimized) {
                    IsMinimizing = true;
                    RemainingMinimizingY = Height;

                    IsMaximizing = false;
                    RemainingMaximizingY = 0;
                } else {
                    Minimized = false;

                    IsMinimizing = false;
                    RemainingMinimizingY = 0;

                    IsMaximizing = true;
                    RemainingMaximizingY = 0;
                }
            }
        }

        if (HoveredItem != null) {
            HoveredItem.OnMouseClick(mouseX, mouseY, mouseButton);

            if (mouseButton == 0)
                MousePlayAnim = 20;
            return true;
        }

        return Dragging;
    }

    public void MouseReleased(int mouseX, int mouseY, int state) {
        if (Dragging)
            Dragging = false;

        for (ComponentItem item : Items) {
            HandleMouseReleaseCompItem(item, mouseX, mouseY);
        }
    }

    public void HandleMouseReleaseCompItem(ComponentItem item, int mouseX, int mouseY) {
        item.OnMouseRelease(mouseX, mouseY);

        for (ComponentItem item1 : item.DropdownItems) {
            item1.OnMouseRelease(mouseX, mouseY);
        }
    }

    public void MouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (ComponentItem item : Items) {
            HandleMouseClickMoveCompItem(item, mouseX, mouseY, clickedMouseButton);
        }
    }

    private void HandleMouseClickMoveCompItem(ComponentItem item, int mouseX, int mouseY, int clickedMouseButton) {
        item.OnMouseClickMove(mouseX, mouseY, clickedMouseButton);

        for (ComponentItem item2 : item.DropdownItems) {
            item2.OnMouseClickMove(mouseX, mouseY, clickedMouseButton);
        }
    }

    public String GetDisplayName() {
        return DisplayName;
    }

    public float GetX() {
        return X;
    }

    public float GetY() {
        return Y;
    }

    public float GetWidth() {
        return Width;
    }

    public float GetHeight() {
        return Height;
    }

    public void SetX(float x1) {
        X = x1;
    }

    public void SetY(float y1) {
        Y = y1;
    }

    public void keyTyped(char typedChar, int keyCode) {
        for (ComponentItem item : Items)
            HandleKeyTypedForItem(item, typedChar, keyCode);
    }

    public void HandleKeyTypedForItem(ComponentItem item, char typedChar, int keyCode) {
        item.keyTyped(typedChar, keyCode);

        for (ComponentItem item1 : item.DropdownItems)
            HandleKeyTypedForItem(item1, typedChar, keyCode);
    }

    private int GetColor() {
        return (Colors.Alpha.getValue() << 24) & 0xFF000000 | (Colors.Red.getValue() << 16) & 0x00FF0000 | (Colors.Green.getValue() << 8) & 0x0000FF00 | Colors.Blue.getValue() & 0x000000FF;
    }

    public int GetTextColor() {
        return (Colors.Red.getValue() << 16) & 0x00FF0000 | (Colors.Green.getValue() << 8) & 0x0000FF00 | Colors.Blue.getValue() & 0x000000FF;
    }

    public void Default() {
        X = DefaultX;
        Y = DefaultY;

        Items.forEach(comp ->
        {
            if (comp.HasState(ComponentItem.Extended))
                comp.RemoveState(ComponentItem.Extended);
        });
    }
}
