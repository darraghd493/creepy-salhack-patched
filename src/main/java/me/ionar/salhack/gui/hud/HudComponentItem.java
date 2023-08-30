package me.ionar.salhack.gui.hud;

import com.google.gson.Gson;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.managers.CommandManager;
import me.ionar.salhack.managers.HudManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class HudComponentItem {
    public static int OnlyVisibleInHudEditor = 0x1;
    public ArrayList<Value> ValueList = new ArrayList<Value>();
    protected float DeltaX;
    protected float DeltaY;
    protected float ClampX;
    protected float ClampY;
    protected int ClampLevel = 0;
    protected int Side = 0;
    protected Minecraft mc = Wrapper.GetMC();
    private String DisplayName;
    private float X;
    private float Y;
    private final float DefaultX;
    private final float DefaultY;
    private float Width;
    private float Height;
    private int Flags;
    private boolean Hidden = true;
    private boolean Dragging = false;
    private boolean Selected = false;
    private boolean MultiSelectedDragging = false;

    public HudComponentItem(String displayName, float x1, float y1) {
        DisplayName = displayName;
        X = x1;
        Y = y1;
        DefaultX = x1;
        DefaultY = y1;
    }

    public String GetDisplayName() {
        return DisplayName;
    }

    public void SetWidth(float width1) {
        Width = width1;
    }

    public void SetHeight(float height1) {
        Height = height1;
    }

    public float GetWidth() {
        return Width;
    }

    public float GetHeight() {
        return Height;
    }

    public boolean IsHidden() {
        return Hidden;
    }

    public void SetHidden(boolean hide) {
        Hidden = hide;

        HudManager.Get().ScheduleSave(this);
    }

    public float GetX() {
        return X;
    }

    public float GetY() {
        return Y;
    }

    public void SetX(float x1) {
        if (X == x1)
            return;

        X = x1;

        if (ClampLevel == 0)
            HudManager.Get().ScheduleSave(this);
    }

    public void SetY(float y1) {
        if (Y == y1)
            return;

        Y = y1;

        if (ClampLevel == 0)
            HudManager.Get().ScheduleSave(this);
    }

    public boolean IsDragging() {
        return Dragging;
    }

    public void SetDragging(boolean dragging1) {
        Dragging = dragging1;
    }

    protected void SetClampPosition(float x1, float y1) {
        ClampX = x1;
        ClampY = y1;
    }

    protected void SetClampLevel(int clampLevel1) {
        ClampLevel = clampLevel1;
    }

    /// don't override unless you return this
    public boolean Render(int mouseX, int mouseY, float partialTicks) {
        boolean inside = mouseX >= GetX() && mouseX < GetX() + GetWidth() && mouseY >= GetY() && mouseY < GetY() + GetHeight();

        if (inside) {
            RenderUtil.drawRect(GetX(), GetY(), GetX() + GetWidth(), GetY() + GetHeight(), 0x50384244);
        }

        if (IsDragging()) {
            ScaledResolution res = new ScaledResolution(mc);

            float x = mouseX - DeltaX;
            float y = mouseY - DeltaY;

            SetX(Math.min(Math.max(0, x), res.getScaledWidth() - GetWidth()));
            SetY(Math.min(Math.max(0, y), res.getScaledHeight() - GetHeight()));
        }
        /*else if (Clamped)
        {
            SetX(ClampX);
            SetY(ClampY);
        }*/

        render(mouseX, mouseY, partialTicks);

        if (IsSelected()) {
            RenderUtil.drawRect(GetX(), GetY(),
                    GetX() + GetWidth(), GetY() + GetHeight(),
                    0x35DDDDDD);
        }

        return inside;
    }

    /// override for childs
    public void render(int mouseX, int mouseY, float partialTicks) {

    }

    public boolean OnMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseX >= GetX() && mouseX < GetX() + GetWidth() && mouseY >= GetY() && mouseY < GetY() + GetHeight()) {
            if (mouseButton == 0) {
                SetDragging(true);
                DeltaX = mouseX - GetX();
                DeltaY = mouseY - GetY();

                HudManager.Get().Items.forEach(item ->
                {
                    if (item.IsMultiSelectedDragging()) {
                        item.SetDragging(true);
                        item.SetDeltaX(mouseX - item.GetX());
                        item.SetDeltaY(mouseY - item.GetY());
                    }
                });
            } else if (mouseButton == 1) {
                ++Side;

                if (Side > 3)
                    Side = 0;

                HudManager.Get().ScheduleSave(this);
            } else if (mouseButton == 2) {
                ++ClampLevel;

                if (ClampLevel > 2)
                    ClampLevel = 0;
                SetClampPosition(GetX(), GetY());
                HudManager.Get().ScheduleSave(this);
            }

            return true;
        }

        return false;
    }

    public void SetDeltaX(float x1) {
        DeltaX = x1;
    }

    public void SetDeltaY(float y1) {
        DeltaY = y1;
    }

    public void OnMouseRelease(int mouseX, int mouseY, int state) {
        SetDragging(false);
    }

    public void LoadSettings() {
        File exists = new File("SalHack/HUD/" + GetDisplayName() + ".json");
        if (!exists.exists())
            return;

        try {
            // create Gson instance
            Gson gson = new Gson();

            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get("SalHack/HUD/" + GetDisplayName() + ".json"));

            // convert JSON file to map
            Map<?, ?> map = gson.fromJson(reader, Map.class);

            // print map entries
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                if (key.equalsIgnoreCase("displayname")) {
                    SetDisplayName(value, false);
                    continue;
                }

                if (key.equalsIgnoreCase("visible")) {
                    SetHidden(value.equalsIgnoreCase("false"));
                    continue;
                }

                if (key.equalsIgnoreCase("PositionX")) {
                    SetX(Float.parseFloat(value));
                    continue;
                }

                if (key.equalsIgnoreCase("PositionY")) {
                    SetY(Float.parseFloat(value));
                    continue;
                }

                if (key.equalsIgnoreCase("ClampLevel")) {
                    SetClampLevel(Integer.parseInt(value));
                    continue;
                }

                if (key.equalsIgnoreCase("ClampPositionX")) {
                    ClampX = (Float.parseFloat(value));
                    continue;
                }

                if (key.equalsIgnoreCase("ClampPositionY")) {
                    ClampY = (Float.parseFloat(value));
                    continue;
                }

                if (key.equalsIgnoreCase("Side")) {
                    Side = Integer.parseInt(value);
                    continue;
                }

                for (Value val : ValueList) {
                    if (val.getName().equalsIgnoreCase((String) entry.getKey())) {
                        if (val.getValue() instanceof Number && !(val.getValue() instanceof Enum)) {
                            if (val.getValue() instanceof Integer)
                                val.SetForcedValue(Integer.parseInt(value));
                            else if (val.getValue() instanceof Float)
                                val.SetForcedValue(Float.parseFloat(value));
                            else if (val.getValue() instanceof Double)
                                val.SetForcedValue(Double.parseDouble(value));
                        } else if (val.getValue() instanceof Boolean) {
                            val.SetForcedValue(value.equalsIgnoreCase("true"));
                        } else if (val.getValue() instanceof Enum) {
                            val.SetForcedValue(val.GetEnumReal(value));
                        } else if (val.getValue() instanceof String)
                            val.SetForcedValue(value);

                        break;
                    }
                }
            }

            // close reader
            reader.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int GetSide() {
        return Side;
    }

    public int GetClampLevel() {
        return ClampLevel;
    }

    public boolean HasFlag(int flag) {
        return (Flags & flag) != 0;
    }

    public void AddFlag(int flags) {
        Flags |= flags;
    }

    public void ResetToDefaultPos() {
        SetX(DefaultX);
        SetY(DefaultY);
    }

    public void SetSelected(boolean selected) {
        Selected = selected;
    }

    public boolean IsInArea(float mouseX1, float mouseX2, float mouseY1, float mouseY2) {
        return GetX() >= mouseX1 && GetX() + GetWidth() <= mouseX2 && GetY() >= mouseY1 && GetY() + GetHeight() <= mouseY2;
    }

    public boolean IsSelected() {
        return Selected;
    }

    public void SetMultiSelectedDragging(boolean b) {
        MultiSelectedDragging = b;
    }

    public boolean IsMultiSelectedDragging() {
        return MultiSelectedDragging;
    }

    public void SetDisplayName(String newName, boolean save) {
        DisplayName = newName;

        if (save) {
            HudManager.Get().ScheduleSave(this);
            CommandManager.Get().Reload();
        }
    }
}
