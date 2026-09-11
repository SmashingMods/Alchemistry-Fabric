package com.smashingmods.alchemistry.test;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.ReactorShape;
import com.smashingmods.alchemistry.api.blockentity.ReactorType;
import com.smashingmods.alchemistry.api.recipe.RecipeStack;
import com.smashingmods.alchemistry.common.block.combiner.CombinerBlockEntity;
import com.smashingmods.alchemistry.common.block.compactor.CompactorBlockEntity;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipe;
import com.smashingmods.alchemistry.network.packets.CombinerIndexPacket;
import com.smashingmods.alchemistry.registry.BlockRegistry;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.util.List;

/** Regression coverage for recipe controls and transactional reactor automation. */
public class ReleaseAuditGameTest {
    private static final BlockPos POS = new BlockPos(3, 2, 1);

    private CombinerRecipe combinerRecipe(String name, net.minecraft.world.item.Item input, net.minecraft.world.item.Item output) {
        return new CombinerRecipe(Identifier.parse("alchemistry:test/audit_" + name),
                List.of(RecipeStack.of(input, 1)), RecipeStack.of(output, 1));
    }

    @GameTest
    public void combinerLockRejectsSelectionAndWrongIngredients(GameTestHelper test) {
        var first = combinerRecipe("first", Items.COBBLESTONE, Items.STONE);
        var second = combinerRecipe("second", Items.DIRT, Items.GRAVEL);
        try (var ignored = RecipeTestScope.withRecipes(test.getLevel(), first, second)) {
            test.setBlock(POS, BlockRegistry.COMBINER);
            var machine = test.getBlockEntity(POS, CombinerBlockEntity.class);
            var player = (ServerPlayer) test.makeMockServerPlayer(GameType.SURVIVAL);
            player.setPos(net.minecraft.world.phys.Vec3.atCenterOf(machine.getBlockPos()));
            player.containerMenu = new com.smashingmods.alchemistry.api.container.AbstractAlchemistryScreenHandler(
                    com.smashingmods.alchemistry.registry.ScreenRegistry.COMBINER_SCREEN_HANDLER, 1,
                    player.getInventory(), machine, machine, new net.minecraft.world.inventory.SimpleContainerData(5), 4, 1) {};
            machine.addRecipe(first);
            machine.addRecipe(second);
            machine.setRecipe(first);
            machine.setRecipeLocked(true);
            machine.insertEnergy(100000);
            machine.setStackInSlot(0, new ItemStack(Items.DIRT));
            CombinerIndexPacket.handle(player, new CombinerIndexPacket(machine.getBlockPos(), machine.getRecipes().indexOf(second)));
            machine.tick();
            test.assertTrue(machine.getRecipe() == first && machine.getProgress() == 0
                            && machine.getEnergyStorage().amount == 100000 && machine.getStackInSlot(4).isEmpty(),
                    "Lock must reject recipe selection and processing of mismatched ingredients");
            machine.setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
            for (int tick = 0; tick <= Config.Common.combinerTicksPerOperation.get(); tick++) machine.tick();
            test.assertTrue(machine.getStackInSlot(4).is(Items.STONE), "Locked recipe must resume with the correct input");
        }
        test.succeed();
    }

    @GameTest
    public void selectingAnotherCombinerRecipeResetsProgress(GameTestHelper test) {
        var first = combinerRecipe("first", Items.COBBLESTONE, Items.STONE);
        var second = combinerRecipe("second", Items.DIRT, Items.GRAVEL);
        try (var ignored = RecipeTestScope.withRecipes(test.getLevel(), first, second)) {
            test.setBlock(POS, BlockRegistry.COMBINER);
            var machine = test.getBlockEntity(POS, CombinerBlockEntity.class);
            var player = (ServerPlayer) test.makeMockServerPlayer(GameType.SURVIVAL);
            player.setPos(net.minecraft.world.phys.Vec3.atCenterOf(machine.getBlockPos()));
            player.containerMenu = new com.smashingmods.alchemistry.api.container.AbstractAlchemistryScreenHandler(
                    com.smashingmods.alchemistry.registry.ScreenRegistry.COMBINER_SCREEN_HANDLER, 1,
                    player.getInventory(), machine, machine, new net.minecraft.world.inventory.SimpleContainerData(5), 4, 1) {};
            machine.addRecipe(first);
            machine.addRecipe(second);
            machine.setRecipe(first);
            machine.setProgress(Config.Common.combinerTicksPerOperation.get());
            CombinerIndexPacket.handle(player, new CombinerIndexPacket(machine.getBlockPos(), machine.getRecipes().indexOf(first)));
            test.assertTrue(machine.getProgress() == Config.Common.combinerTicksPerOperation.get(),
                    "Selecting the same recipe must preserve paid progress");
            machine.insertEnergy(100000);
            machine.setStackInSlot(0, new ItemStack(Items.DIRT));
            CombinerIndexPacket.handle(player, new CombinerIndexPacket(machine.getBlockPos(), machine.getRecipes().indexOf(second)));
            machine.tick();
            test.assertTrue(machine.getStackInSlot(4).isEmpty() && machine.getProgress() == 1,
                    "Selecting a different recipe must not complete immediately using the previous recipe's progress");
        }
        test.succeed();
    }

    @GameTest
    public void compactorLocksTheTargetSelectedWhilePaused(GameTestHelper test) {
        // Unique outputs avoid ambiguity with the built-in stone and gravel recipes.
        var first = new CompactorRecipe(Identifier.parse("alchemistry:test/audit_compact_a"),
                RecipeStack.of(Items.COBBLESTONE, 1), RecipeStack.of(Items.OAK_BUTTON, 1));
        var second = new CompactorRecipe(Identifier.parse("alchemistry:test/audit_compact_b"),
                RecipeStack.of(Items.COBBLESTONE, 1), RecipeStack.of(Items.BIRCH_BUTTON, 1));
        try (var ignored = RecipeTestScope.withRecipes(test.getLevel(), first, second)) {
            for (boolean previouslySelected : List.of(false, true)) {
                test.getLevel().destroyBlock(test.absolutePos(POS), false);
                test.setBlock(POS, BlockRegistry.COMPACTOR);
                var machine = test.getBlockEntity(POS, CompactorBlockEntity.class);
                machine.setPaused(true);
                if (previouslySelected) {
                    machine.setTarget(first.getOutput());
                    machine.setProgress(17);
                }
                var target = second.getOutput();
                machine.setTarget(target);
                target.shrink(1);
                test.assertTrue(machine.getRecipe() == second && machine.getProgress() == 0
                                && machine.getTarget().is(Items.BIRCH_BUTTON),
                        "Target selection must copy the stack and resolve the recipe before ingredients arrive");
                machine.setProgress(17);
                machine.setTarget(second.getOutput());
                test.assertTrue(machine.getProgress() == 17, "Reselecting the same target must retain progress");
                machine.setRecipeLocked(true);
                machine.setTarget(first.getOutput());
                test.assertTrue(machine.getRecipe() == second && machine.getTarget().is(Items.BIRCH_BUTTON),
                        "Locked compactor must reject target changes");
                machine.setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
                machine.insertEnergy(100000);
                machine.tick();
                test.assertTrue(machine.getProgress() == 17, "Paused target selection must not process ingredients");
                machine.setPaused(false);
                for (int tick = 17; tick <= Config.Common.compactorTicksPerOperation.get(); tick++) machine.tick();
                test.assertTrue(machine.getStackInSlot(2).is(Items.BIRCH_BUTTON),
                        "Compactor must process the target selected while paused");
                machine.setRecipeLocked(false);
                machine.setTarget(ItemStack.EMPTY);
                test.assertTrue(machine.getRecipe() == null && machine.getTarget().isEmpty() && machine.getProgress() == 0,
                        "Clearing the target must clear its selected recipe and progress");
            }
        }
        test.succeed();
    }

    @GameTest
    public void sharedReactorOutputRollbackConservesItems(GameTestHelper test) {
        test.setBlock(POS, BlockRegistry.FISSION_CONTROLLER);
        var machine = test.getBlockEntity(POS, AbstractReactorBlockEntity.class);
        var level = test.getLevel();
        var pos = machine.getBlockPos();
        new ReactorShape(pos, ReactorType.FISSION, level).createShapeMap().forEach((box, blocks) ->
                BlockPos.betweenClosedStream(box).forEach(p -> {
                    if (!p.equals(pos)) level.setBlockAndUpdate(p, blocks.getFirst().defaultBlockState());
                }));
        level.setBlockAndUpdate(pos.below(), BlockRegistry.REACTOR_ENERGY.defaultBlockState());
        level.setBlockAndUpdate(pos.below().west(), BlockRegistry.REACTOR_INPUT.defaultBlockState());
        level.setBlockAndUpdate(pos.below().east(), BlockRegistry.REACTOR_OUTPUT.defaultBlockState());
        level.setBlockAndUpdate(pos.below().east(2), BlockRegistry.REACTOR_OUTPUT.defaultBlockState());
        machine.tick();
        test.assertTrue(machine.isValidMultiblock(), "Fixture must form");
        var first = ItemStorage.SIDED.find(level, pos.below().east(), Direction.UP);
        var second = ItemStorage.SIDED.find(level, pos.below().east(2), Direction.UP);
        machine.setStackInSlot(1, new ItemStack(Items.DIAMOND, 64));
        try (var outer = Transaction.openOuter()) {
            try (var inner = outer.openNested()) {
                test.assertTrue(first.extract(ItemVariant.of(Items.DIAMOND), 10, inner) == 10, "First extraction");
                test.assertTrue(second.extract(ItemVariant.of(Items.DIAMOND), 10, inner) == 10, "Second extraction");
                inner.commit();
            }
            // Abort the outer transaction: both extractions must be rolled back.
        }
        test.assertTrue(machine.getStackInSlot(1).getCount() == 64,
                "Aborting transfers through two ports must restore all 64 diamonds; actual=" + machine.getStackInSlot(1).getCount());
        test.succeed();
    }
}
