package com.smashingmods.alchemistry.test;

import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.api.blockentity.*;
import com.smashingmods.alchemistry.api.recipe.*;
import com.smashingmods.alchemistry.common.recipe.atomizer.AtomizerRecipe;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipe;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipe;
import com.smashingmods.alchemistry.common.recipe.fission.FissionRecipe;
import com.smashingmods.alchemistry.common.recipe.fusion.FusionRecipe;
import com.smashingmods.alchemistry.common.recipe.liquifier.LiquifierRecipe;
import com.smashingmods.alchemistry.registry.BlockRegistry;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class RecipeValidationGameTest {
    private static final List<Block> MACHINES = List.of(BlockRegistry.COMBINER, BlockRegistry.COMPACTOR,
            BlockRegistry.DISSOLVER, BlockRegistry.ATOMIZER, BlockRegistry.LIQUIFIER,
            BlockRegistry.FISSION_CONTROLLER, BlockRegistry.FUSION_CONTROLLER);

    @GameTest
    public void removedRecipesCannotProcessLiveOrSavedMachines(GameTestHelper test) {
        var level = test.getLevel();
        for (Block block : MACHINES) {
            for (boolean locked : List.of(false, true)) {
                for (boolean reload : List.of(false, true)) {
                    var machine = readyMachine(test, block);
                    machine.setRecipeLocked(locked);
                    test.assertTrue(machine.canProcessRecipe(), "Fixture must be ready to complete an operation: " + block);
                    var type = machine.getRecipe().getType();
                    var contents = machine.getItems().stream().map(ItemStack::copy).toList();
                    long energy = machine.getEnergyStorage().amount;
                    long fluid = machine instanceof AbstractFluidBlockEntity tank ? tank.getFluidStorage().amount : 0;
                    var saved = machine.saveWithFullMetadata(level.registryAccess());
                    // Remove this type so unlocked machines cannot legitimately select a different matching recipe.
                    var remaining = level.getServer().getRecipeManager().getRecipes().stream()
                            .filter(holder -> holder.value().getType() != type).toList();
                    try (var ignored = RecipeTestScope.replace(level, remaining)) {
                        if (reload) {
                            machine = (AbstractInventoryBlockEntity) BlockEntity.loadStatic(machine.getBlockPos(), machine.getBlockState(), saved, level.registryAccess());
                            level.setBlockEntity(machine);
                        }
                        machine.tick();
                        test.assertTrue(machine.getRecipe() == null && machine.getProgress() == 0,
                                "Removed recipe must clear before processing: " + block + ", locked=" + locked + ", reload=" + reload);
                        test.assertTrue(machine.getEnergyStorage().amount == energy, "Removed recipe must not consume energy");
                        for (int slot = 0; slot < contents.size(); slot++) {
                            test.assertTrue(ItemStack.matches(contents.get(slot), machine.getStackInSlot(slot)), "Removed recipe must not consume or create items");
                        }
                        if (machine instanceof AbstractFluidBlockEntity tank) {
                            test.assertTrue(tank.getFluidStorage().amount == fluid, "Removed recipe must not consume or create fluid");
                        }
                    }
                }
            }
        }
        test.succeed();
    }

    @GameTest
    public void unchangedSavedRecipesKeepTheirProgress(GameTestHelper test) {
        var level = test.getLevel();
        for (Block block : MACHINES) {
            var machine = readyMachine(test, block);
            var canonical = machine.getRecipe();
            machine.setProgress(17);
            machine.setPaused(true);
            machine.setRecipeLocked(true);
            var saved = machine.saveWithFullMetadata(level.registryAccess());
            var restored = (AbstractInventoryBlockEntity) BlockEntity.loadStatic(machine.getBlockPos(), machine.getBlockState(), saved, level.registryAccess());
            level.setBlockEntity(restored);
            restored.tick();
            test.assertTrue(restored.getRecipe() == canonical && restored.getProgress() == 17 && restored.isRecipeLocked() && restored.isProcessingPaused(),
                    "Identical saved recipe must resolve to current registry without losing progress or controls: " + block);
        }
        test.succeed();
    }

    @GameTest
    public void changedRecipeUsesNewCostAndRejectsWrongType(GameTestHelper test) {
        var level = test.getLevel();
        var id = Identifier.parse("alchemistry:test/reload_cost");
        var oldRecipe = new CombinerRecipe(id, List.of(RecipeStack.of(Items.COBBLESTONE, 1)), RecipeStack.of(Items.STONE, 1));
        var replacement = new CombinerRecipe(id, List.of(RecipeStack.of(Items.COBBLESTONE, 2)), RecipeStack.of(Items.STONE, 2));
        for (boolean reload : List.of(false, true)) {
            for (boolean locked : List.of(false, true)) {
                try (var original = RecipeTestScope.withRecipes(level, oldRecipe)) {
                    var machine = placeMachine(test, BlockRegistry.COMBINER);
                    machine.setRecipe(oldRecipe);
                    machine.setRecipeLocked(locked);
                    machine.setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
                    machine.setProgress(Config.Common.combinerTicksPerOperation.get());
                    var saved = machine.saveWithFullMetadata(level.registryAccess());
                    try (var changed = RecipeTestScope.withRecipes(level, replacement)) {
                        if (reload) {
                            machine = (AbstractInventoryBlockEntity) BlockEntity.loadStatic(machine.getBlockPos(), machine.getBlockState(), saved, level.registryAccess());
                            level.setBlockEntity(machine);
                        }
                        machine.tick();
                        test.assertTrue(machine.getRecipe() == replacement && machine.getProgress() == 0 && machine.getStackInSlot(4).isEmpty()
                                        && machine.getStackInSlot(0).getCount() == 1 && machine.getEnergyStorage().amount == 100000,
                                "A changed recipe must use its new cost and discard progress from the old operation");
                        machine.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 2));
                        for (int i = 0; i <= Config.Common.combinerTicksPerOperation.get(); i++) machine.tick();
                        test.assertTrue(machine.getStackInSlot(0).isEmpty() && ItemStack.matches(machine.getStackInSlot(4), replacement.getOutput()),
                                "Replacement recipe must process successfully with its new input and output counts");
                    }
                    // Reusing an ID for another recipe type must never reach a machine-specific cast.
                    machine.setRecipe(oldRecipe);
                    machine.setRecipeLocked(true);
                    var otherType = new CompactorRecipe(id, RecipeStack.of(Items.COBBLESTONE, 1), RecipeStack.of(Items.STONE, 1));
                    try (var changed = RecipeTestScope.withRecipes(level, otherType)) {
                        machine.tick();
                        test.assertTrue(machine.getRecipe() == null && machine.getProgress() == 0, "Wrong-type replacement must clear selection safely");
                    }
                }
            }
        }
        test.succeed();
    }

    @GameTest
    public void fusionConsumesMatchedSlotsWithUnequalCounts(GameTestHelper test) {
        for (boolean sameItem : List.of(false, true)) {
            var recipe = new FusionRecipe(Identifier.parse("alchemistry:test/fusion_counts"),
                    RecipeStack.of(Items.COBBLESTONE, 2), RecipeStack.of(sameItem ? Items.COBBLESTONE : Items.DIRT, 1), RecipeStack.of(Items.STONE, 1));
            try (var ignored = RecipeTestScope.withRecipes(test.getLevel(), recipe)) {
                for (boolean reverse : List.of(false, true)) {
                    var machine = placeMachine(test, BlockRegistry.FUSION_CONTROLLER);
                    machine.setRecipe(recipe);
                    machine.setRecipeLocked(true);
                    machine.setStackInSlot(0, reverse ? recipe.getInput2() : recipe.getInput1());
                    machine.setStackInSlot(1, reverse ? recipe.getInput1() : recipe.getInput2());
                    for (int i = 0; i <= Config.Common.fusionTicksPerOperation.get(); i++) machine.tick();
                    test.assertTrue(machine.getStackInSlot(0).isEmpty() && machine.getStackInSlot(1).isEmpty()
                                    && ItemStack.matches(machine.getStackInSlot(2), recipe.getOutput()),
                            "Fusion must consume exact ingredients in either order, including repeated item types");
                    long energy = machine.getEnergyStorage().amount;
                    for (int i = 0; i <= Config.Common.fusionTicksPerOperation.get(); i++) machine.tick();
                    test.assertTrue(machine.getStackInSlot(2).getCount() == 1 && machine.getEnergyStorage().amount == energy,
                            "Exhausted fusion inputs must not create another output or consume energy");

                    machine.clearContent();
                    machine.setStackInSlot(0, recipe.getInput1().copyWithCount(1));
                    machine.setStackInSlot(1, recipe.getInput2());
                    machine.tick();
                    test.assertTrue(machine.getStackInSlot(2).isEmpty() && machine.getStackInSlot(0).getCount() == 1
                                    && machine.getStackInSlot(1).getCount() == 1 && machine.getEnergyStorage().amount == energy,
                            "Insufficient ingredients must remain untouched");
                }
                // Rearranging stacks during an operation must use the order present at completion.
                var machine = placeMachine(test, BlockRegistry.FUSION_CONTROLLER);
                machine.setRecipe(recipe);
                machine.setRecipeLocked(true);
                machine.setStackInSlot(0, recipe.getInput1());
                machine.setStackInSlot(1, recipe.getInput2());
                machine.tick();
                machine.setStackInSlot(0, recipe.getInput2());
                machine.setStackInSlot(1, recipe.getInput1());
                for (int i = 0; i < Config.Common.fusionTicksPerOperation.get(); i++) machine.tick();
                test.assertTrue(machine.getStackInSlot(0).isEmpty() && machine.getStackInSlot(1).isEmpty() && machine.getStackInSlot(2).getCount() == 1,
                        "Swapping inputs during progress must consume the matched slot counts at completion");
            }
        }
        test.succeed();
    }

    private AbstractInventoryBlockEntity placeMachine(GameTestHelper test, Block block) {
        var level = test.getLevel();
        var pos = test.absolutePos(new BlockPos(3, 2, 1));
        level.destroyBlock(pos, false);
        level.setBlockAndUpdate(pos, block.defaultBlockState());
        var machine = (AbstractInventoryBlockEntity) level.getBlockEntity(pos);
        machine.insertEnergy(100000);
        if (machine instanceof AbstractReactorBlockEntity reactor) {
            new ReactorShape(pos, reactor.getReactorType(), level).createShapeMap().forEach((box, blocks) -> BlockPos.betweenClosedStream(box).forEach(p -> {
                if (!p.equals(pos)) level.setBlock(p, blocks.getFirst().defaultBlockState(), 3);
            }));
            level.setBlockAndUpdate(pos.below(), BlockRegistry.REACTOR_ENERGY.defaultBlockState());
            level.setBlockAndUpdate(pos.below().west(), BlockRegistry.REACTOR_INPUT.defaultBlockState());
            level.setBlockAndUpdate(pos.below().east(), BlockRegistry.REACTOR_OUTPUT.defaultBlockState());
            reactor.tick();
            test.assertTrue(reactor.isValidMultiblock(), "Reactor fixture must form");
        }
        return machine;
    }

    private AbstractInventoryBlockEntity readyMachine(GameTestHelper test, Block block) {
        var machine = placeMachine(test, block);
        var level = test.getLevel();
        AbstractAlchemistryRecipe recipe;
        int ticks;
        if (block == BlockRegistry.COMBINER) {
            var selected = MachineRecipes.all(level, CombinerRecipe.Type.INSTANCE).stream().filter(r -> r.getId().equals(Identifier.parse("alchemistry:combiner/diamond"))).findFirst().orElseThrow();
            for (int i = 0; i < selected.getInput().size(); i++) machine.setStackInSlot(i, selected.getInput().get(i));
            recipe = selected; ticks = Config.Common.combinerTicksPerOperation.get();
        } else if (block == BlockRegistry.COMPACTOR) {
            var selected = MachineRecipes.all(level, CompactorRecipe.Type.INSTANCE).getFirst();
            machine.setStackInSlot(0, selected.getInput());
            recipe = selected; ticks = Config.Common.compactorTicksPerOperation.get();
        } else if (block == BlockRegistry.DISSOLVER) {
            var selected = MachineRecipes.all(level, DissolverRecipe.Type.INSTANCE).stream().filter(r -> r.getInput().test(new ItemStack(Items.IRON_INGOT))).findFirst().orElseThrow();
            machine.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
            recipe = selected; ticks = Config.Common.dissolverTicksPerOperation.get();
        } else if (block == BlockRegistry.ATOMIZER) {
            var selected = MachineRecipes.all(level, AtomizerRecipe.Type.INSTANCE).getFirst();
            ((AbstractFluidBlockEntity) machine).insertFluid(selected.getFluidInput(), selected.getFluidAmount() * 81);
            recipe = selected; ticks = Config.Common.atomizerTicksPerOperation.get();
        } else if (block == BlockRegistry.LIQUIFIER) {
            var selected = MachineRecipes.all(level, LiquifierRecipe.Type.INSTANCE).getFirst();
            machine.setStackInSlot(0, new ItemStack(selected.getInput().items().findFirst().orElseThrow(), selected.getInputAmount()));
            recipe = selected; ticks = Config.Common.liquifierTicksPerOperation.get();
        } else if (block == BlockRegistry.FISSION_CONTROLLER) {
            var selected = MachineRecipes.all(level, FissionRecipe.Type.INSTANCE).getFirst();
            machine.setStackInSlot(0, selected.getInput());
            recipe = selected; ticks = Config.Common.fissionTicksPerOperation.get();
        } else {
            var selected = MachineRecipes.all(level, FusionRecipe.Type.INSTANCE).getFirst();
            machine.setStackInSlot(0, selected.getInput1()); machine.setStackInSlot(1, selected.getInput2());
            recipe = selected; ticks = Config.Common.fusionTicksPerOperation.get();
        }
        machine.setRecipe(recipe);
        machine.setProgress(ticks);
        return machine;
    }
}
