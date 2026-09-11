package com.smashingmods.alchemistry.api.blockentity;
import net.minecraft.world.level.block.state.properties.EnumProperty;
public final class PowerStateProperty {
    public static final EnumProperty<PowerState> POWER_STATE = EnumProperty.create("power_state", PowerState.class);
}
