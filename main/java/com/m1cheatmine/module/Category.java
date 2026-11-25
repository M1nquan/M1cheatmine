package com.m1cheatmine.module;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    PLAYER("Player"),
    MISC("Misc"),
    HUD("HUD");

    public final String name;

    Category(String n) {
        this.name = n;
    }
}