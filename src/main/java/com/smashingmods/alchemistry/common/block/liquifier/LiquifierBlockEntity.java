package com.smashingmods.alchemistry.common.block.liquifier;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractFluidBlockEntity;
import com.smashingmods.alchemistry.mixin.BucketItemMixin;
import com.smashingmods.alchemistry.registry.BlockEntityRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LiquifierBlockEntity extends AbstractFluidBlockEntity {

    public static final int INVENTORY_SIZE = 1;

    protected final PropertyDelegate propertyDelegate;
    private final int maxProgress;

    public LiquifierBlockEntity(BlockPos pos, BlockState state) {
        super(DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY), BlockEntityRegistry.LIQUIFIER_BLOCK_ENTITY, pos, state, Config.Common.liquifierEnergyCapacity.get(), Config.Common.liquifierFluidCapacity.get());
        this.maxProgress = Config.Common.liquifierTicksPerOperation.get();
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> getProgress();
                    case 1 -> maxProgress;
                    case 2 -> (int) getEnergyStorage().getAmount();
                    case 3 -> (int) getEnergyStorage().getCapacity();
                    case 4 -> (int) getFluidStorage().getAmount();
                    case 5 -> (int) getFluidStorage().getCapacity();
                    default -> 0;
                };
            }
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> setProgress(value);
                    case 2 -> insertEnergy(value);
                    case 3 -> getFluidStorage().amount = value;
                }
            }
            public int size() {
                return 6;
            }
        };
    }

    @Override
    public void updateRecipe() {
        // TODO: Implement
    }

    @Override
    public boolean canProcessRecipe() {
        // TODO: Implement
        return false;
    }

    @Override
    public void processRecipe() {
        // TODO: Implement
    }

    @Override
    public <T extends Recipe<SimpleInventory>> void setRecipe(@Nullable T pRecipe) {
        // TODO: Implement
    }

    @Override
    public Recipe<SimpleInventory> getRecipe() {
        // TODO: Implement
        return null;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new LiquifierScreenHandler(syncId, inv, this, this, this.propertyDelegate);
    }

    /**
     * Handles fluid insertion/extraction when using a bucket
     * @return true if fluid was transferred, false if GUI should be opened instead.
     */
    public boolean onBlockActivated(World world, BlockPos blockPos, PlayerEntity player, Hand hand) {
        Item item = player.getStackInHand(hand).getItem();
        if (item instanceof BucketItem bucket) {
            if (item == Items.BUCKET) {
                // Extract fluid if possible
                FluidVariant fluidVariant = getFluidStorage().variant;
                try { extractFluid(fluidVariant); }
                catch (IllegalArgumentException e) { return false; }

                // Fill bucket in hand
                if (!world.isClient() && !player.isCreative()) player.setStackInHand(hand, new ItemStack(fluidVariant.getFluid().getBucketItem()));
                else player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0f, 1.0f);
            } else {
                // Insert fluid if it matches
                Fluid fluid = ((BucketItemMixin) bucket).getFluid();
                FluidVariant fluidVariant = FluidVariant.of(fluid);
                if (getFluidStorage().amount == 0 || fluidVariant.equals(getFluidStorage().variant)) insertFluid(fluidVariant);
                else return false;

                // Empty bucket in hand
                if (!world.isClient() && !player.isCreative()) player.setStackInHand(hand, new ItemStack(Items.BUCKET));
                else player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1.0f, 1.0f);
            }
            return true;
        }
        return false;
    }
}
