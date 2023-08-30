package me.ionar.salhack.command.util;

public interface ModuleCommandListener {
    void OnHide();

    void OnToggle();

    void OnRename(String newName);
}
