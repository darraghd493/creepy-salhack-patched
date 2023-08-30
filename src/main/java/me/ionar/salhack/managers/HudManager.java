package me.ionar.salhack.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ionar.salhack.gui.hud.GuiHudEditor;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.gui.hud.components.*;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.ValueListeners;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HudManager {
    public ArrayList<HudComponentItem> Items = new ArrayList<HudComponentItem>();
    private boolean CanSave = false;

    public HudManager() {
    }

    public static HudManager Get() {
        return SalHack.GetHudManager();
    }

    public void Init() {
        Add(new WatermarkComponent());
        Add(new WelcomeComponent());
        Add(new ArrayListComponent());
        Add(new InventoryComponent());
        Add(new TabGUIComponent());
        Add(new NotificationComponent());
        Add(new CoordsComponent());
        Add(new SpeedComponent());
        Add(new BiomeComponent());
        Add(new TimeComponent());
        Add(new TPSComponent());
        Add(new FPSComponent());
        Add(new DirectionComponent());
        Add(new TooltipComponent());
        Add(new ArmorComponent());
        Add(new KeyStrokesComponent());
        Add(new HoleInfoComponent());
        Add(new PlayerCountComponent());
        Add(new PlayerFrameComponent());
        Add(new NearestEntityFrameComponent());
        Add(new YawComponent());
        Add(new TotemCountComponent());
        Add(new PingComponent());
        Add(new ChestCountComponent());
        Add(new TrueDurabilityComponent());
        Add(new StopwatchComponent());
        Add(new PvPInfoComponent());
        Add(new SchematicaMaterialInfoComponent());

        /// MUST be last in list
        Add(new SelectorMenuComponent());

        CanSave = false;

        Items.forEach(item ->
        {
            item.LoadSettings();
        });

        CanSave = true;
    }

    public void Add(HudComponentItem item) {
        try {
            for (Field field : item.getClass().getDeclaredFields()) {
                if (Value.class.isAssignableFrom(field.getType())) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }

                    final Value val = (Value) field.get(item);

                    ValueListeners listener = new ValueListeners() {
                        @Override
                        public void OnValueChange(Value val) {
                            ScheduleSave(item);
                        }
                    };

                    val.Listener = listener;
                    item.ValueList.add(val);
                }
            }
            Items.add(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OnRender(float partialTicks) {
        GuiScreen currScreen = Wrapper.GetMC().currentScreen;

        if (currScreen != null) {
            if (currScreen instanceof GuiHudEditor) {
                return;
            }
        }

        GL11.glPushMatrix();

        Items.forEach(item ->
        {
            if (!item.IsHidden() && !item.HasFlag(HudComponentItem.OnlyVisibleInHudEditor)) {
                try {
                    item.render(0, 0, partialTicks);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });

        GL11.glPopMatrix();
    }

    public void ScheduleSave(HudComponentItem item) {
        if (!CanSave)
            return;

        try {
            GsonBuilder builder = new GsonBuilder();

            Gson gson = builder.setPrettyPrinting().create();

            Writer writer = Files.newBufferedWriter(Paths.get("SalHack/HUD/" + item.GetDisplayName() + ".json"));
            Map<String, String> map = new HashMap<>();

            map.put("displayname", item.GetDisplayName());
            map.put("visible", !item.IsHidden() ? "true" : "false");
            map.put("PositionX", String.valueOf(item.GetX()));
            map.put("PositionY", String.valueOf(item.GetY()));
            map.put("ClampLevel", String.valueOf(item.GetClampLevel()));
            map.put("ClampPositionX", String.valueOf(item.GetX()));
            map.put("ClampPositionY", String.valueOf(item.GetY()));
            map.put("Side", String.valueOf(item.GetSide()));

            for (Value val : item.ValueList) {
                map.put(val.getName(), val.getValue().toString());
            }

            gson.toJson(map, writer);
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
