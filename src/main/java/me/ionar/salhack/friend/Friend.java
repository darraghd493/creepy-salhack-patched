package me.ionar.salhack.friend;

public class Friend {
    private final String Name;
    private String Alias;
    private String Cape;
    public Friend(String name1, String alias1, String cape1) {
        Name = name1;
        Alias = alias1;
        Cape = cape1;
    }

    public void SetAlias(String alias1) {
        Alias = alias1;
    }

    public void SetCape(String cape1) {
        Cape = cape1;
    }

    public String GetName() {
        return Name;
    }

    public String GetAlias() {
        return Alias;
    }

    public String GetCape() {
        return Cape;
    }
}
