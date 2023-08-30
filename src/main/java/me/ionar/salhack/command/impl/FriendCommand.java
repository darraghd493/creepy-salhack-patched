package me.ionar.salhack.command.impl;

import com.google.gson.internal.LinkedTreeMap;
import me.ionar.salhack.command.Command;
import me.ionar.salhack.friend.Friend;
import me.ionar.salhack.managers.FriendManager;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("Friend", "Allows you to communicate with the friend manager, allowing for adding/removing/updating friends");

        CommandChunks.add("add <username>");
        CommandChunks.add("remove <username>");
        CommandChunks.add("list");
    }

    @Override
    public void ProcessCommand(String args) {
        String[] split = args.split(" ");

        if (split == null || split.length <= 1) {
            SendToChat("Invalid Input");
            return;
        }

        if (split[1].toLowerCase().startsWith("a")) {
            if (split.length > 1) {
                if (FriendManager.Get().AddFriend(split[2].toLowerCase()))
                    SendToChat(String.format("Added %s as a friend.", split[2]));
                else
                    SendToChat(String.format("%s is already a friend.", split[2]));
            } else {
                SendToChat("Usage: friend add <name>");
            }
        } else if (split[1].toLowerCase().startsWith("r")) {
            if (split.length > 1) {
                if (FriendManager.Get().RemoveFriend(split[2].toLowerCase()))
                    SendToChat(String.format("Removed %s as a friend.", split[2]));
                else
                    SendToChat(String.format("%s is not a friend.", split[2]));
            } else {
                SendToChat("Usage: friend remove <name>");
            }
        } else if (split[1].toLowerCase().startsWith("l")) {
            final LinkedTreeMap<String, Friend> map = FriendManager.Get().GetFriends();

            map.forEach((k, v) ->
            {
                SendToChat(String.format("F: %s A: %s", v.GetName(), v.GetAlias()));
            });

            if (map.isEmpty()) {
                SendToChat("You don't have any friends...");
            }
        }
    }

    @Override
    public String GetHelp() {
        return "Allows you to add friends, or remove friends or list friends..\nfriend add <name>\nfriend remove<name>\nfriend list";
    }
}
