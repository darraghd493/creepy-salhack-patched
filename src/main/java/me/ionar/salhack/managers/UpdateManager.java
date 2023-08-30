package me.ionar.salhack.managers;

import me.ionar.salhack.main.SalHack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateManager {

    private String version = "";
    private String msg = "Failed to get update message";

    public UpdateManager() {
        Load();
    }

    public static UpdateManager Get() {
        return SalHack.GetUpdateManager();
    }

    public void Load() {

        try {
            URL uRL;
            URLConnection connection;
            BufferedReader reader;

            System.out.println("Getting version");
            uRL = new URL("https://raw.githubusercontent.com/CreepyOrb924/creepy-salhack-assets/master/assets/version.txt");
            connection = uRL.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            version = reader.readLine();

            reader.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            URL uRL;
            URLConnection connection;
            BufferedReader reader;

            System.out.println("Getting update message");
            uRL = new URL("https://raw.githubusercontent.com/CreepyOrb924/creepy-salhack-assets/master/update.txt");
            connection = uRL.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            msg = reader.readLine();

            reader.close();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public String getVersion() {
        return this.version;
    }

    public String getMessage() {return this.msg;}

}
