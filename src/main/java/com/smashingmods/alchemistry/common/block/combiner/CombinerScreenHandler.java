package com.smashingmods.alchemistry.common.block.combiner;

import com.smashingmods.alchemistry.api.container.AbstractAlchemistryScreenHandler;
import com.smashingmods.alchemistry.api.container.slots.OutputSlot;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.alchemistry.network.AlchemistryNetwork;
import com.smashingmods.alchemistry.network.packets.CombinerIndexPacket;
import com.smashingmods.alchemistry.network.packets.CombinerRecipePacket;
import com.smashingmods.alchemistry.registry.ScreenRegistry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CombinerScreenHandler extends AbstractAlchemistryScreenHandler {

    protected final ContainerData propertyDelegate;
    private final Level level;
    private final Player viewer;
    private final CombinerBlockEntity blockEntity;
    private final List<CombinerRecipe> displayedRecipes = new ArrayList<>();

    public CombinerScreenHandler(int syncId, Inventory playerInventory, net.minecraft.core.BlockPos position) {
        this(syncId, playerInventory,Objects.requireNonNull(playerInventory.player.level().getBlockEntity(position)), new SimpleContainer(CombinerBlockEntity.INVENTORY_SIZE), new SimpleContainerData(5));
    }

    protected CombinerScreenHandler(int syncId, Inventory playerInventory, BlockEntity blockEntity, Container inventory, ContainerData delegate) {
        super(ScreenRegistry.COMBINER_SCREEN_HANDLER, syncId, playerInventory, blockEntity, inventory, delegate, 4, 1);
        this.level = playerInventory.player.level();
        this.viewer = playerInventory.player;
        this.blockEntity = (CombinerBlockEntity) blockEntity;

        setupRecipeList();
        // input 2x2 grid
        addSlots(Slot::new, inventory, 2, 2, 0, 4, 12, 63);
        // output
        addSlots(OutputSlot::new, inventory, 1, 1, 4, 1, 102, 81);

        this.propertyDelegate = delegate;
        addDataSlots(delegate);
    }

    @Override
    public void addPlayerInventorySlots(Container pInventory) {
        addSlots(Slot::new, pInventory, 3, 9, 9, 27,12, 106);
        addSlots(Slot::new, pInventory, 1, 9, 0,9, 12, 164);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if(player.level().isClientSide()) {
            if (!getBlockEntity().isRecipeLocked()) {
                if (this.isValidRecipeIndex(id)) {
                    int recipeIndex = blockEntity.getRecipes().indexOf(displayedRecipes.get(id));
                    CombinerRecipe recipe = blockEntity.getRecipes().get(recipeIndex);
                    this.setSelectedRecipeIndex(id);
                    this.blockEntity.setRecipe(recipe);
                    com.smashingmods.alchemistry.network.AlchemistryClientNetwork.sendToServer(new CombinerIndexPacket(getBlockEntity().getBlockPos(), recipeIndex));
                }
            }
        }
        return true;
    }

    protected int getSelectedRecipeIndex() {
        return this.getPropertyDelegate().get(4);
    }

    protected void setSelectedRecipeIndex(int pIndex) {
        this.getPropertyDelegate().set(4, pIndex);
    }

    private boolean isValidRecipeIndex(int pSlot) {
        return pSlot >= 0 && pSlot < this.displayedRecipes.size();
    }

    private void setupRecipeList() {
        if (!level.isClientSide()) {
            List<CombinerRecipe> recipes = com.smashingmods.alchemistry.api.recipe.MachineRecipes.all(level, CombinerRecipe.Type.INSTANCE).stream().sorted().toList();
            this.blockEntity.getRecipes().clear();
            boolean first = true;
            for (CombinerRecipe recipe : recipes) {
                this.blockEntity.addRecipe(recipe);
                AlchemistryNetwork.sendToClient(new CombinerRecipePacket(blockEntity.getBlockPos(), recipe, first), (ServerPlayer) viewer);
                first = false;
            }
            this.blockEntity.markRecipesSynced();
        }
    }

    public void resetDisplayedRecipes() {
        this.displayedRecipes.clear();
        this.displayedRecipes.addAll(this.blockEntity.getRecipes());
    }

    public List<CombinerRecipe> getDisplayedRecipes() {
        return displayedRecipes.stream().sorted().toList();
    }

    public void searchRecipeList(String pKeyword) {
        this.displayedRecipes.clear();
        this.displayedRecipes.addAll(this.blockEntity.getRecipes().stream().filter(recipe -> {
            Identifier id = BuiltInRegistries.ITEM.getKey(recipe.getOutput().getItem());
            return id.getPath().contains(pKeyword.toLowerCase().replace(" ", "_"));
        }).toList());
    }

}
