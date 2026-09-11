package com.smashingmods.alchemistry.api.blockentity;

import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.Objects;

public abstract class AbstractProcessingBlockEntity extends BlockEntity implements ProcessingBlockEntity, ExtendedMenuProvider<BlockPos>, Nameable {

    private final Component name;
    private int progress = 0;
    private boolean recipeLocked = false;
    private boolean paused = false;
    private final SimpleEnergyStorage energyStorage;

    public AbstractProcessingBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState state, long energyCapacity) {
        super(type, worldPosition, state);
        String blockEntityName = Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(getType())).getPath();
        this.name = Component.translatable(String.format("%s.container.%s", Alchemistry.MOD_ID, blockEntityName));
        energyStorage = new SimpleEnergyStorage(energyCapacity, energyCapacity, energyCapacity) {
            @Override
            protected void onFinalCommit() {
                setChanged();
            }
        };
    }

    @Override
    public Component getName() {
        return name != null ? name : this.getTypeName();
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    protected Component getTypeName() {
        return name;
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        dropContents();
        if (this instanceof AbstractReactorBlockEntity reactor) reactor.onRemove();
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public void tick() {
        if (level != null && !level.isClientSide()) {
            refreshRecipe();
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

    /** Saved and selected recipes are snapshots; only the current server recipe may be processed. */
    protected void refreshRecipe() {
        if (!(level instanceof ServerLevel server) || !(getRecipe() instanceof AbstractAlchemistryRecipe selected)) return;
        var current = server.getServer().getRecipeManager()
                .byKey(ResourceKey.create(Registries.RECIPE, selected.getId()))
                .map(holder -> holder.value())
                .filter(recipe -> recipe.getType() == selected.getType())
                .orElse(null);
        if (current == selected) return;

        // Rebind identical recipes after loading/reloading without losing work already paid for.
        var ops = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var savedData = Recipe.CODEC.encodeStart(ops, selected).result();
        boolean unchanged = current != null && savedData.isPresent()
                && savedData.equals(Recipe.CODEC.encodeStart(ops, current).result());
        @SuppressWarnings("unchecked")
        var recipe = (Recipe<RecipeInput>) current;
        setRecipe(recipe);
        if (!unchanged) setProgress(0);
        setChanged();
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
        setChanged();
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
        setChanged();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput nbt) {
        nbt.putInt("progress", progress);
        nbt.putBoolean("locked", isRecipeLocked());
        nbt.putBoolean("paused", isProcessingPaused());
        nbt.putLong("energy", energyStorage.amount);
        if (getRecipe() != null) {
            nbt.store("recipe", net.minecraft.world.item.crafting.Recipe.CODEC, getRecipe());
            if (getRecipe() instanceof com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe recipe) nbt.store("recipeId", net.minecraft.resources.Identifier.CODEC, recipe.getId());
        }
        super.saveAdditional(nbt);
    }

    @Override
    public void loadAdditional(net.minecraft.world.level.storage.ValueInput nbt) {
        super.loadAdditional(nbt);
        setRecipe(null);
        nbt.read("recipe", net.minecraft.world.item.crafting.Recipe.CODEC).ifPresent(recipe -> {
            if (recipe instanceof com.smashingmods.alchemistry.api.recipe.AbstractAlchemistryRecipe machineRecipe) {
                nbt.read("recipeId", net.minecraft.resources.Identifier.CODEC).ifPresent(machineRecipe::setId);
                setRecipe(machineRecipe);
            }
        });
        setProgress(nbt.getIntOr("progress", 0));
        setRecipeLocked(nbt.getBooleanOr("locked", false));
        setPaused(nbt.getBooleanOr("paused", false));
        energyStorage.amount = Math.clamp(nbt.getLongOr("energy", 0L), 0L, energyStorage.capacity);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) { return getBlockPos(); }

    public SimpleEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public void insertEnergy(long value) {
        try (Transaction transaction = Transaction.openOuter()) {
            long amountExtracted = getEnergyStorage().insert(value, transaction);
            if (amountExtracted == value) {
                transaction.commit();
            }
        }
    }

    public void extractEnergy(long value) {
        try (Transaction transaction = Transaction.openOuter()) {
            long amountExtracted = getEnergyStorage().extract(value, transaction);
            if (amountExtracted == value) {
                transaction.commit();
            }
        }
    }

    public void forceSync() {
        this.setChanged();
        level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }
}
