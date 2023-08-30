package me.ionar.salhack.gui.click.component.item;

import me.ionar.salhack.gui.click.component.listeners.ComponentItemListener;

import java.util.ArrayList;

public class ComponentItem {
    /// Flags
    public static int Clickable = 0x1;
    public static int Hoverable = 0x2;
    public static int Tooltip = 0x4;
    public static int HasValues = 0x8;
    public static int RectDisplayAlways = 0x10;
    public static int Slider = 0x20;
    public static int Boolean = 0x40;
    public static int Enum = 0x80;
    public static int DontDisplayClickableHighlight = 0x100;
    public static int RectDisplayOnClicked = 0x200;

    /// State
    public static int Clicked = 0x1;
    public static int Hovered = 0x2;
    public static int Extended = 0x4;
    public ArrayList<ComponentItem> DropdownItems;
    protected int Flags;
    protected int State;
    protected ComponentItemListener Listener;
    protected float CurrentWidth;
    private final String DisplayText;
    private final String Description;
    private float X;
    private float Y;
    private float Width;
    private float Height;

    public ComponentItem(String displayText, String description1, int flags, int state, ComponentItemListener listener, float width1, float height1) {
        DisplayText = displayText;
        Description = description1;
        Flags = flags;
        State = state;
        Listener = listener;

        DropdownItems = new ArrayList<ComponentItem>();

        X = 0;
        Y = 0;
        Width = width1;
        Height = height1;
        CurrentWidth = width1;
    }

    public String GetDisplayText() {
        return DisplayText;
    }

    public String GetDescription() {
        return Description;
    }

    public boolean HasFlag(int flag) {
        return (Flags & flag) != 0;
    }

    public boolean HasState(int state) {
        return (State & state) != 0;
    }

    public void AddState(int state) {
        State |= state;
    }

    public void RemoveState(int state) {
        State &= ~state;
    }

    public float GetX() {
        return X;
    }

    public void SetX(float x) {
        X = x;
    }

    public float GetY() {
        return Y;
    }

    public void SetY(float y) {
        Y = y;
    }

    public float GetWidth() {
        return Width;
    }

    public void SetWidth(float width) {
        Width = width;
    }

    public float GetHeight() {
        return Height;
    }

    public void SetHeight(float height) {
        Height = height;
    }

    public float GetCurrentWidth() {
        return CurrentWidth;
    }

    public void OnMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (Listener != null)
                Listener.OnToggled();

            if (HasState(Clicked))
                RemoveState(Clicked);
            else
                AddState(Clicked);
        } else if (mouseButton == 1) {
            if (HasState(Extended))
                RemoveState(Extended);
            else
                AddState(Extended);
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
    }

    public void OnMouseMove(float mouseX, float mouseY, float x1, float y1) {
    }

    public void Update() {
    }

    public void OnMouseRelease(int mouseX, int mouseY) {
        // TODO Auto-generated method stub

    }

    public void OnMouseClickMove(int mouseX, int mouseY, int clickedMouseButton) {
        // TODO Auto-generated method stub

    }
}
