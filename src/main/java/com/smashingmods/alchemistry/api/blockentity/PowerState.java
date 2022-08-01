package com.smashingmods.alchemistry.api.blockentity;

import net.minecraft.util.StringIdentifiable;

public enum PowerState implements StringIdentifiable {

    DISABLED("disabled"),
    OFF("off"),
    STANDBY("standby"),
    ON("on");

    private final String name;

    PowerState(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
