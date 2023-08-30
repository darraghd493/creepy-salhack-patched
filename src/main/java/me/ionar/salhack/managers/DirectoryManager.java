package me.ionar.salhack.managers;

import me.ionar.salhack.main.SalHack;

import java.io.File;
import java.io.IOException;

public class DirectoryManager {
    public DirectoryManager() {
    }

    public static DirectoryManager Get() {
        return SalHack.GetDirectoryManager();
    }

    public void Init() {
        /// Create directories as needed
        try {
            CreateDirectory("SalHack");
            CreateDirectory("SalHack/Modules");
            CreateDirectory("SalHack/GUI");
            CreateDirectory("SalHack/HUD");
            CreateDirectory("SalHack/Locater");
            CreateDirectory("SalHack/StashFinder");
            CreateDirectory("SalHack/Config");
            CreateDirectory("SalHack/Capes");
            CreateDirectory("SalHack/Music");
            CreateDirectory("SalHack/CoordExploit");
            CreateDirectory("SalHack/LogoutSpots");
            CreateDirectory("SalHack/DeathSpots");
            CreateDirectory("SalHack/Waypoints");
            CreateDirectory("SalHack/Fonts");
            CreateDirectory("SalHack/CustomMods");
            CreateDirectory("SalHack/Presets");
            CreateDirectory("SalHack/Presets/Default");
            CreateDirectory("SalHack/Presets/Default/Modules");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void CreateDirectory(String path) throws IOException {
        new File(path).mkdirs();

        //System.out.println("Created path at " + path.get().toString());
    }

    public String GetCurrentDirectory() throws IOException {
        return new java.io.File(".").getCanonicalPath();
    }
}
