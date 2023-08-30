package me.ionar.salhack.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class Preset {
    private String _displayName;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> _valueListMods = new ConcurrentHashMap<>();
    private boolean _active;

    public Preset(String displayName) {
        _displayName = displayName;
    }

    public void initNewPreset() {
        ModuleManager.Get().GetModuleList().forEach(mod ->
        {
            addModuleSettings(mod);
        });
    }

    public void addModuleSettings(final Module mod) {
        ConcurrentHashMap<String, String> valsMap = new ConcurrentHashMap<>();

        valsMap.put("enabled", mod.isEnabled() ? "true" : "false");
        valsMap.put("display", mod.getDisplayName());
        valsMap.put("keybind", mod.getKey());
        valsMap.put("hidden", mod.isHidden() ? "true" : "false");

        mod.getValueList().forEach(val ->
        {
            if (val.getValue() != null)
                valsMap.put(val.getName(), val.getValue().toString());
        });

        _valueListMods.put(mod.getDisplayName(), valsMap);

        save();
    }

    // this will load the settings for presets, and modules settings
    public void load(File directory) {
        File exists = new File("SalHack/Presets/" + directory.getName() + "/" + directory.getName() + ".json");
        if (!exists.exists())
            return;

        try {
            Gson gson = new Gson();

            Reader reader = Files.newBufferedReader(Paths.get("SalHack/Presets/" + directory.getName() + "/" + directory.getName() + ".json"));

            Map<?, ?> map = gson.fromJson(reader, Map.class);

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();

                if (key == "displayName") {
                    _displayName = val;
                    continue;
                }
            }

            reader.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try (Stream<Path> paths = Files.walk(Paths.get("SalHack/Presets/" + directory.getName() + "/Modules/"))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path ->
                    {
                        try {
                            Gson gson = new Gson();

                            Reader reader = Files.newBufferedReader(Paths.get("SalHack/Presets/" + directory.getName() + "/Modules/" + path.getFileName().toString()));

                            Map<?, ?> map = gson.fromJson(reader, Map.class);

                            ConcurrentHashMap<String, String> valsMap = new ConcurrentHashMap<>();

                            for (Map.Entry<?, ?> entry : map.entrySet()) {
                                String key = (String) entry.getKey();
                                String val = (String) entry.getValue();

                                valsMap.put(key, val);
                            }

                            _valueListMods.put(path.getFileName().toString().substring(0, path.getFileName().toString().indexOf(".json")), valsMap);

                            reader.close();

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            GsonBuilder builder = new GsonBuilder();

            Gson gson = builder.setPrettyPrinting().create();

            Writer writer = Files.newBufferedWriter(Paths.get("SalHack/Presets/" + _displayName + "/" + _displayName + ".json"));
            Map<String, String> map = new HashMap<>();

            map.put("displayName", _displayName);
            gson.toJson(map, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            for (Entry<String, ConcurrentHashMap<String, String>> entry : _valueListMods.entrySet()) {
                GsonBuilder builder = new GsonBuilder();

                Gson gson = builder.setPrettyPrinting().create();

                Writer writer = Files.newBufferedWriter(Paths.get("SalHack/Presets/" + _displayName + "/Modules/" + entry.getKey() + ".json"));
                Map<String, String> map = new HashMap<>();

                for (Entry<String, String> value : entry.getValue().entrySet()) {
                    String key = value.getKey();
                    String val = value.getValue();

                    map.put(key, val);
                }
                gson.toJson(map, writer);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return _displayName;
    }

    public boolean isActive() {
        return _active;
    }

    public void setActive(boolean b) {
        _active = b;
    }

    public void initValuesForMod(Module mod) {
        if (_valueListMods.containsKey(mod.getDisplayName())) {
            for (Entry<String, String> value : _valueListMods.get(mod.getDisplayName()).entrySet()) {
                String key = value.getKey();
                String value1 = (String) value.getValue();

                if (key.equalsIgnoreCase("enabled")) {
                    if (value1.equalsIgnoreCase("true")) {
                        if (!mod.isEnabled())
                            mod.toggleNoSave();
                    } else if (mod.isEnabled())
                        mod.toggle();
                    continue;
                }

                if (key.equalsIgnoreCase("display")) {
                    mod.displayName = value1;
                    continue;
                }

                if (key.equalsIgnoreCase("keybind")) {
                    mod.key = value1;
                    continue;
                }

                if (key.equalsIgnoreCase("hidden")) {
                    mod.hidden = value1.equalsIgnoreCase("true");
                    continue;
                }

                for (Value val : mod.valueList) {
                    if (val.getName().equalsIgnoreCase((String) value.getKey())) {
                        if (val.getValue() instanceof Number && !(val.getValue() instanceof Enum)) {
                            if (val.getValue() instanceof Integer)
                                val.SetForcedValue(Integer.parseInt(value1));
                            else if (val.getValue() instanceof Float)
                                val.SetForcedValue(Float.parseFloat(value1));
                            else if (val.getValue() instanceof Double)
                                val.SetForcedValue(Double.parseDouble(value1));
                        } else if (val.getValue() instanceof Boolean) {
                            val.SetForcedValue(value1.equalsIgnoreCase("true"));
                        } else if (val.getValue() instanceof Enum) {
                            Enum e = val.GetEnumReal(value1);

                            if (e != null)
                                val.SetForcedValue(e);
                        } else if (val.getValue() instanceof String)
                            val.SetForcedValue(value);

                        break;
                    }
                }
            }
        }
    }
}
