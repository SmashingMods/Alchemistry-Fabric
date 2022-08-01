package com.smashingmods.alchemistry.api.blockentity;

import net.minecraft.util.StringIdentifiable;

public enum ReactorType implements StringIdentifiable {

    FUSION("fusion"),
    FISSION("fission");

    private final String name;

    ReactorType(String pName) {
        this.name = pName;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
