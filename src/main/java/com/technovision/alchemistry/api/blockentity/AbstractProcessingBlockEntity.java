package com.technovision.alchemistry.api.blockentity;

import com.technovision.alchemistry.Alchemistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractProcessingBlockEntity extends BlockEntity implements ProcessingBlockEntity, ExtendedScreenHandlerFactory, Nameable {

    private final Text name;
    private int progress = 0;
    private boolean recipeLocked = false;
    private boolean paused = false;

    public AbstractProcessingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        String blockEntityName = Objects.requireNonNull(Registry.BLOCK_ENTITY_TYPE.getId(getType())).getPath();
        this.name = Text.translatable(String.format("%s.container.%s", Alchemistry.MOD_ID, blockEntityName));
    }

    @Override
    public Text getName() {
        return name != null ? name : this.getDefaultName();
    }

    @Override
    public Text getDisplayName() {
        return getName();
    }

    protected Text getDefaultName() {
        return name;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = super.toInitialChunkDataNbt();
        writeNbt(tag);
        return tag;
    }

    public boolean onBlockActivated(World world, BlockPos pos, PlayerEntity player, Hand hand) {
        return false;
    }

    @Override
    public void tick() {
        if (world != null && !world.isClient()) {
            if (!paused) {
                if (!recipeLocked) {
                    updateRecipe();
                }
                if (canProcessRecipe()) {
                    processRecipe();
                }
            }
        }
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public void incrementProgress() {
        this.progress++;
    }

    @Override
    public boolean isRecipeLocked() {
        return this.recipeLocked;
    }

    @Override
    public boolean isProcessingPaused() {
        return this.paused;
    }

    @Override
    public void setRecipeLocked(boolean recipeLocked) {
        this.recipeLocked = recipeLocked;
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putInt("progress", progress);
        nbt.putBoolean("locked", isRecipeLocked());
        nbt.putBoolean("paused", isProcessingPaused());
        // TODO: Add when implemented
        //nbt.put("energy", energyHandler.serializeNBT());
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        setProgress(nbt.getInt("progress"));
        setRecipeLocked(nbt.getBoolean("locked"));
        setPaused(nbt.getBoolean("paused"));
        // TODO: Add when implemented
        //energyHandler.deserializeNBT(nbt.get("energy"));
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }
}
