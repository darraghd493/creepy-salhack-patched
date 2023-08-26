package me.ionar.salhack.managers;

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

import javax.imageio.ImageIO;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.player.EventPlayerGetLocationCape;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.main.Wrapper;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listenable;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

public class CapeManager implements Listenable {
    private final HashMap<String, ResourceLocation> CapeUsers = new HashMap<String, ResourceLocation>();
    private final HashMap<String, ResourceLocation> Capes = new HashMap<String, ResourceLocation>(); /// < Only used at startup
    List<String> capesList = new ArrayList<String>();

    @EventHandler
    private Listener<EventPlayerGetLocationCape> OnGetLocationSkin = new Listener<>(p_Event -> {
        if (Wrapper.GetMC().renderEngine == null) return;

        if (CapeUsers.containsKey(p_Event.Player.getUniqueID().toString())) {
            p_Event.cancel();
            p_Event.SetResourceLocation(CapeUsers.get(p_Event.Player.getUniqueID().toString()));
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
            URL l_URL;
            URLConnection l_Connection;
            BufferedReader l_Reader;
            String l_Line;

            System.out.println("Downloading & Loading cape imgs");
            l_URL = new URL("https://raw.githubusercontent.com/CreepyOrb924/creepy-salhack-assets/master/assets/capes/cape.txt");
            l_Connection = l_URL.openConnection();
            l_Connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
            l_Reader = new BufferedReader(new InputStreamReader(l_Connection.getInputStream()));

            try (Stream<Path> paths = Files.walk(Paths.get("SalHack/Capes/"))) {
                paths.filter(Files::isRegularFile).forEach(path ->
                        capesList.add(path.getFileName().toString().replace(".png", ""))
                );
            }

            while ((l_Line = l_Reader.readLine()) != null) {
                String[] l_Split = l_Line.split(" ");

                if (!capesList.contains(l_Split[1]) && l_Split.length == 2)
                    DownloadCapeFromLocationWithName(l_Split[0], l_Split[1]);

                addCape(l_Split[1]);
            }
            l_Reader.close();

            l_URL = new URL("https://raw.githubusercontent.com/CreepyOrb924/creepy-salhack-assets/master/assets/capes/capes.txt");
            l_Connection = l_URL.openConnection();
            l_Connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
            l_Reader = new BufferedReader(new InputStreamReader(l_Connection.getInputStream()));

            while ((l_Line = l_Reader.readLine()) != null)
                ProcessCapeString(l_Line);
            l_Reader.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        System.out.println("Done loading capes");
        capesList.clear();
    }

    private void ProcessCapeString(String p_String) {
        // FIX [me.ionar.salhack.managers.CapeManager:ProcessCapeString:104]: Invalid cape name amazonemp for user https://i.imgur.com/D6PS7w0.png
        String[] l_Split = p_String.split(" ");

        if (l_Split.length == 2) {
            /// User, CapeName
            ResourceLocation l_Cape = GetCapeFromName(l_Split[1]);

            if (l_Cape != null) CapeUsers.put(l_Split[0], l_Cape);
            else System.out.println("Invalid cape name " + l_Split[1] + " for user " + l_Split[0]);
        }
    }

    private ResourceLocation GetCapeFromName(String p_Name) {
        if (!Capes.containsKey(p_Name)) return null;
        return Capes.get(p_Name);
    }

    private void addCape(String name) throws IOException {
        File file = new File("SalHack/Capes/" + name + ".png");
        BufferedImage bufferedImage = ImageIO.read(file);
        DynamicTexture l_Texture = new DynamicTexture(bufferedImage);

        Capes.put(name, Wrapper.GetMC().getTextureManager().getDynamicTextureLocation("SalHack/Capes/", l_Texture));
    }

    public void DownloadCapeFromLocationWithName(String p_Link, String p_Name) throws MalformedURLException, IOException {
        BufferedImage bufferedImage = ImageIO.read(new URL(p_Link));
        File file = new File("SalHack/Capes/" + p_Name + ".png");

        ImageIO.write(bufferedImage, "png", file);
        System.out.println("Downloaded cape " + p_Name);
    }
}
