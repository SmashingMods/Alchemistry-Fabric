package com.smashingmods.alchemistry.common.block.combiner;

import com.smashingmods.alchemistry.api.container.AbstractAlchemistryScreenHandler;
import com.smashingmods.alchemistry.api.container.slots.OutputSlot;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.alchemistry.network.AlchemistryNetwork;
import com.smashingmods.alchemistry.network.packets.CombinerRecipePacket;
import com.smashingmods.alchemistry.registry.ScreenRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CombinerScreenHandler extends AbstractAlchemistryScreenHandler {

    protected final PropertyDelegate propertyDelegate;
    private final World world;
    private final PlayerEntity viewer;
    private final CombinerBlockEntity blockEntity;
    private final List<CombinerRecipe> displayedRecipes = new ArrayList<>();

    public CombinerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buffer) {
        this(syncId, playerInventory,Objects.requireNonNull(playerInventory.player.getWorld().getBlockEntity(buffer.readBlockPos())), new SimpleInventory(CombinerBlockEntity.INVENTORY_SIZE), new ArrayPropertyDelegate(5));
    }

    protected CombinerScreenHandler(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity, Inventory inventory, PropertyDelegate delegate) {
        super(ScreenRegistry.COMBINER_SCREEN_HANDLER, syncId, playerInventory, blockEntity, inventory, delegate, 4, 1);
        this.world = playerInventory.player.getWorld();
        this.viewer = playerInventory.player;
        this.blockEntity = (CombinerBlockEntity) blockEntity;

        setupRecipeList();
        // input 2x2 grid
        addSlots(Slot::new, inventory, 2, 2, 0, 4, 12, 63);
        // output
        addSlots(OutputSlot::new, inventory, 1, 1, 4, 1, 102, 81);

        this.propertyDelegate = delegate;
        addProperties(delegate);
    }

    @Override
    public void addPlayerInventorySlots(Inventory pInventory) {
        addSlots(Slot::new, pInventory, 3, 9, 9, 27,12, 106);
        addSlots(Slot::new, pInventory, 1, 9, 0,9, 12, 164);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if(player.world.isClient()) {
            if (!getBlockEntity().isRecipeLocked()) {
                if (this.isValidRecipeIndex(id)) {
                    int recipeIndex = blockEntity.getRecipes().indexOf(displayedRecipes.get(id));
                    CombinerRecipe recipe = blockEntity.getRecipes().get(recipeIndex);
                    this.setSelectedRecipeIndex(id);
                    this.blockEntity.setRecipe(recipe);
                    //AlchemistryPacketHandler.INSTANCE.sendToServer(new CombinerRecipePacket(getBlockEntity().getBlockPos(), recipeIndex));
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
        if (!world.isClient() && !this.blockEntity.isRecipesSynced()) {
            List<CombinerRecipe> recipes = world.getRecipeManager().getAllMatches(CombinerRecipe.Type.INSTANCE, new SimpleInventory(1), world).stream().sorted().toList();
            for (CombinerRecipe recipe : recipes) {
                AlchemistryNetwork.sendToClient(new CombinerRecipePacket(blockEntity.getPos(), recipe), (ServerPlayerEntity) viewer);
            }
            this.blockEntity.markRecipesSynced();
        }
    }

    public void resetDisplayedRecipes() {
        this.displayedRecipes.clear();
        this.displayedRecipes.addAll(this.blockEntity.getRecipes());
    }

    public List<CombinerRecipe> getDisplayedRecipes() {
        return blockEntity.getRecipes().stream().sorted().toList();
    }

    public void searchRecipeList(String pKeyword) {
        this.displayedRecipes.clear();
        this.displayedRecipes.addAll(this.blockEntity.getRecipes().stream().filter(recipe -> {
            Objects.requireNonNull(recipe.getOutput().getItem().getName());
            return recipe.getOutput().getItem().getName().toString().contains(pKeyword.toLowerCase().replace(" ", "_"));
        }).toList());
    }
}
