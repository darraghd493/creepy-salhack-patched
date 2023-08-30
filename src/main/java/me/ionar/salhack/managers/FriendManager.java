package me.ionar.salhack.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import me.ionar.salhack.friend.Friend;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.misc.FriendsModule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FriendManager {
    private FriendsModule m_FriendsModule;
    private LinkedTreeMap<String, Friend> FriendList = new LinkedTreeMap<>();

    public FriendManager() {
    }

    public static FriendManager Get() {
        return SalHack.GetFriendManager();
    }

    /// Loads the friends from the JSON
    public void LoadFriends() {
        File exists = new File("SalHack/FriendList.json");
        if (!exists.exists())
            return;

        try {
            // create Gson instance
            Gson gson = new Gson();

            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get("SalHack/" + "FriendList" + ".json"));

            // convert JSON file to map
            FriendList = gson.fromJson(reader, new TypeToken<LinkedTreeMap<String, Friend>>() {}.getType());

            // close reader
            reader.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void SaveFriends() {
        GsonBuilder builder = new GsonBuilder();

        Gson gson = builder.setPrettyPrinting().create();

        Writer writer;
        try {
            writer = Files.newBufferedWriter(Paths.get("SalHack/" + "FriendList" + ".json"));

            gson.toJson(FriendList, new TypeToken<LinkedTreeMap<String, Friend>>() {}.getType(), writer);
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String GetFriendName(Entity entity) {
        if (!FriendList.containsKey(entity.getName().toLowerCase()))
            return entity.getName();

        return FriendList.get(entity.getName().toLowerCase()).GetAlias();
    }

    public boolean IsFriend(Entity entity) {
        return entity instanceof EntityPlayer && FriendList.containsKey(entity.getName().toLowerCase());
    }

    public boolean AddFriend(String name1) {
        if (FriendList.containsKey(name1))
            return false;

        Friend friend = new Friend(name1, name1, null);

        FriendList.put(name1, friend);
        SaveFriends();
        return true;
    }

    public boolean RemoveFriend(String name1) {
        if (!FriendList.containsKey(name1))
            return false;

        FriendList.remove(name1);
        SaveFriends();
        return true;
    }

    public final LinkedTreeMap<String, Friend> GetFriends() {
        return FriendList;
    }

    public boolean IsFriend(String name1) {
        if (!m_FriendsModule.isEnabled())
            return false;

        return FriendList.containsKey(name1.toLowerCase());
    }

    public Friend GetFriend(Entity e) {
        if (!m_FriendsModule.isEnabled())
            return null;

        if (!FriendList.containsKey(e.getName().toLowerCase()))
            return null;

        return FriendList.get(e.getName().toLowerCase());
    }

    public void Load() {
        LoadFriends();

        m_FriendsModule = (FriendsModule) ModuleManager.Get().GetMod(FriendsModule.class);
    }
}
