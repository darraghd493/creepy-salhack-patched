package me.ionar.salhack.managers;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.player.EventPlayerGetLocationCape;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.main.Wrapper;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listenable;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class CapeManager implements Listenable {
    private final HashMap<String, ResourceLocation> CapeUsers = new HashMap<String, ResourceLocation>();
    private final HashMap<String, ResourceLocation> Capes = new HashMap<String, ResourceLocation>(); /// < Only used at startup
    List<String> capesList = new ArrayList<String>();

    @EventHandler
    private final Listener<EventPlayerGetLocationCape> OnGetLocationSkin = new Listener<>(event -> {
        if (Wrapper.GetMC().renderEngine == null) return;

        if (CapeUsers.containsKey(event.Player.getUniqueID().toString())) {
            event.cancel();
            event.SetResourceLocation(CapeUsers.get(event.Player.getUniqueID().toString()));
        }
    });

    public CapeManager() {
        LoadCapes();

        SalHackMod.EVENT_BUS.subscribe(this);
    }

    public static CapeManager Get() {
        return SalHack.GetCapeManager();
    }

    public void LoadCapes() {
        try {
            URL uRL;
            URLConnection connection;
            BufferedReader reader;
            String line;

            System.out.println("Downloading & Loading cape imgs");
            uRL = new URL("https://raw.githubusercontent.com/CreepyOrb924/creepy-salhack-assets/master/assets/capes/cape.txt");
            connection = uRL.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            try (Stream<Path> paths = Files.walk(Paths.get("SalHack/Capes/"))) {
                paths.filter(Files::isRegularFile).forEach(path ->
                        capesList.add(path.getFileName().toString().replace(".png", ""))
                );
            }

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(" ");

                if (!capesList.contains(split[1]) && split.length == 2)
                    DownloadCapeFromLocationWithName(split[0], split[1]);

                addCape(split[1]);
            }
            reader.close();

            uRL = new URL("https://raw.githubusercontent.com/CreepyOrb924/creepy-salhack-assets/master/assets/capes/capes.txt");
            connection = uRL.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((line = reader.readLine()) != null)
                ProcessCapeString(line);
            reader.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println("Done loading capes");
        capesList.clear();
    }

    private void ProcessCapeString(String string) {
        // FIX [me.ionar.salhack.managers.CapeManager:ProcessCapeString:104]: Invalid cape name amazonemp for user https://i.imgur.com/D6PS7w0.png
        String[] split = string.split(" ");

        if (split.length == 2) {
            /// User, CapeName
            ResourceLocation cape = GetCapeFromName(split[1]);

            if (cape != null) CapeUsers.put(split[0], cape);
            else System.out.println("Invalid cape name " + split[1] + " for user " + split[0]);
        }
    }

    private ResourceLocation GetCapeFromName(String name1) {
        if (!Capes.containsKey(name1)) return null;
        return Capes.get(name1);
    }

    private void addCape(String name) throws IOException {
        File file = new File("SalHack/Capes/" + name + ".png");
        BufferedImage bufferedImage = ImageIO.read(file);
        DynamicTexture texture = new DynamicTexture(bufferedImage);

        Capes.put(name, Wrapper.GetMC().getTextureManager().getDynamicTextureLocation("SalHack/Capes/", texture));
    }

    public void DownloadCapeFromLocationWithName(String link, String name1) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new URL(link));
        File file = new File("SalHack/Capes/" + name1 + ".png");

        ImageIO.write(bufferedImage, "png", file);
        System.out.println("Downloaded cape " + name1);
    }
}
