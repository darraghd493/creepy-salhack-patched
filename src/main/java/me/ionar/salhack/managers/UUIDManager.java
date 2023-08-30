package me.ionar.salhack.managers;

import com.google.common.collect.Maps;
import com.google.gson.JsonParser;
import me.ionar.salhack.main.SalHack;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

// based from seppuku
public class UUIDManager {
    private final Map<String, String> uuidNameCache = Maps.newConcurrentMap();

    public UUIDManager() {

    }

    public static UUIDManager Get() {
        return SalHack.GetUUIDManager();
    }

    public String resolveName(String uuid) {
        uuid = uuid.replace("-", "");
        if (uuidNameCache.containsKey(uuid)) {
            return uuidNameCache.get(uuid);
        }

        final String url = "https://api.mojang.com/user/profiles/" + uuid + "/names";
        try {
            final String nameJson = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            if (nameJson != null && nameJson.length() > 0) {
                JsonParser parser = new JsonParser();

                return parser.parse(nameJson).getAsJsonArray().get(parser.parse(nameJson).getAsJsonArray().size() - 1)
                        .getAsJsonObject().get("name").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
