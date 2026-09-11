package com.smashingmods.alchemistry.test;

import com.smashingmods.alchemistry.api.blockentity.AbstractReactorBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.PowerState;
import com.smashingmods.alchemistry.api.blockentity.ReactorShape;
import com.smashingmods.alchemistry.api.blockentity.ReactorType;
import com.smashingmods.alchemistry.api.recipe.MachineRecipes;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipe;
import com.smashingmods.alchemistry.common.recipe.dissolver.ProbabilityGroup;
import com.smashingmods.alchemistry.common.recipe.dissolver.ProbabilitySet;
import com.smashingmods.alchemistry.registry.BlockRegistry;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;

public class ReactorAndDissolverGameTest {
    @GameTest
    public void portRemovalPreservesInventoryAcrossChunkSaves(GameTestHelper test) {
        var level = test.getLevel();
        var origin = test.absolutePos(new BlockPos(0, 2, 0));
        // Stay in the test area's active chunks, above the other test structures, straddling an X boundary.
        var pos = new BlockPos(origin.getX() & ~15, origin.getY() + 16, origin.getZ() + 3);
        for (ReactorType type : ReactorType.values()) {
            for (Block portBlock : List.of(BlockRegistry.REACTOR_INPUT, BlockRegistry.REACTOR_OUTPUT)) {
                var machine = buildReactor(test, pos, type);
                var portPos = pos.below().west(2);
                level.setBlockAndUpdate(portPos, portBlock.defaultBlockState());
                // Settle in DISABLED before saving: changing power state later would mask the original save bug.
                level.destroyBlock(pos.above(3), false);
                machine.tick();
                machine.tick();
                machine.setStackInSlot(0, new ItemStack(Items.DIAMOND, 32));
                var controllerChunk = level.getChunkAt(pos);
                test.assertTrue(controllerChunk != level.getChunkAt(portPos), "Port must be across a chunk boundary");
                level.getChunkSource().save(true);
                test.assertTrue(!controllerChunk.isUnsaved(), "Controller must start saved");
                test.assertTrue(readSavedController(test, machine).getStackInSlot(0).getCount() == 32,
                        "Baseline saved controller must contain the diamonds");

                level.destroyBlock(portPos, false);
                machine.tick();
                test.assertTrue(machine.getPowerState() == PowerState.DISABLED, "Controller must remain disabled");
                test.assertTrue(machine.getStackInSlot(0).getCount() == 32, "Breaking either port must preserve controller inventory");
                test.assertTrue(droppedDiamonds(test, pos) == 0, "Ports must not drop the controller's diamonds");
                level.getChunkSource().save(true);
                test.assertTrue(readSavedController(test, machine).getStackInSlot(0).getCount() == 32,
                        "Reload must preserve exactly the inventory that remained in the controller");

                level.destroyBlock(pos, false);
                test.assertTrue(droppedDiamonds(test, pos) == 32, "Breaking the controller must drop its inventory exactly once");
                level.getChunkSource().save(true);
                var savedChunk = level.getChunkSource().chunkMap.read(controllerChunk.getPos()).join().orElseThrow();
                test.assertTrue(savedChunk.getListOrEmpty("block_entities").stream().noneMatch(t -> isAt(t, pos)),
                        "Removed controller must not remain in the saved chunk");
                level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(5)).forEach(ItemEntity::discard);
            }
        }
        test.succeed();
    }

    @GameTest
    public void cachedPortStorageSurvivesFormationRemovalAndReconnection(GameTestHelper test) {
        var level = test.getLevel();
        var pos = test.absolutePos(new BlockPos(3, 2, 1));
        for (ReactorType type : ReactorType.values()) {
            var machine = buildReactor(test, pos, type);
            var inputPos = pos.below().west();
            var outputPos = pos.below().east();
            var inputPort = level.getBlockEntity(inputPos);
            var outputPort = level.getBlockEntity(outputPos);
            // These ports have not been attached by a controller tick yet.
            var input = ItemStorage.SIDED.find(level, inputPos, Direction.UP);
            var output = ItemStorage.SIDED.find(level, outputPos, Direction.DOWN);
            test.assertTrue(input != null && output != null, "Ports must expose storage before formation");
            for (var storage : List.of(input, output)) {
                int slots = 0;
                for (var view : storage) {
                    test.assertTrue(view.isResourceBlank() && view.getAmount() == 0, "Detached slots must be empty");
                    slots++;
                }
                test.assertTrue(slots == 3, "Cached storage must expose all three slots before formation");
            }

            machine.tick();
            test.assertTrue(machine.isValidMultiblock(), "Reactor must form");
            var diamond = ItemVariant.of(Items.DIAMOND);
            var gold = ItemVariant.of(Items.GOLD_INGOT);
            try (var transaction = Transaction.openOuter()) {
                test.assertTrue(input.insert(diamond, 8, transaction) == 8, "Cached input must work after formation");
                transaction.commit();
            }
            int outputSlot = type == ReactorType.FISSION ? 1 : 2;
            machine.setStackInSlot(outputSlot, new ItemStack(Items.GOLD_INGOT, 8));
            try (var transaction = Transaction.openOuter()) {
                test.assertTrue(input.extract(diamond, 1, transaction) == 0, "Input port must reject extraction");
                test.assertTrue(output.insert(gold, 1, transaction) == 0, "Output port must reject insertion");
                test.assertTrue(output.extract(gold, 3, transaction) == 3, "Cached output must support extraction");
                // Abort to ensure cached views still participate in transfer rollback.
            }
            test.assertTrue(machine.getStackInSlot(outputSlot).getCount() == 8, "Aborted extraction must restore the inventory");

            level.destroyBlock(pos, false);
            for (var storage : List.of(input, output)) {
                int slots = 0;
                for (var view : storage) {
                    test.assertTrue(view.isResourceBlank() && view.getResource().isBlank() && view.getAmount() == 0,
                            "Every retained storage view must safely become empty after controller removal");
                    slots++;
                }
                test.assertTrue(slots == 3, "Detached storage must retain a stable slot count");
                try (var transaction = Transaction.openOuter()) {
                    test.assertTrue(storage.insert(diamond, 1, transaction) == 0 && storage.extract(gold, 1, transaction) == 0,
                            "Detached storage must reject transfers");
                    transaction.commit();
                }
            }

            var replacement = buildReactor(test, pos, type);
            test.assertTrue(level.getBlockEntity(inputPos) == inputPort && level.getBlockEntity(outputPos) == outputPort,
                    "Reconnection must reuse the original ports");
            replacement.tick();
            replacement.setStackInSlot(outputSlot, new ItemStack(Items.GOLD_INGOT, 2));
            try (var transaction = Transaction.openOuter()) {
                test.assertTrue(input.insert(diamond, 4, transaction) == 4 && output.extract(gold, 2, transaction) == 2,
                        "Retained connections must transfer through the replacement controller");
                transaction.commit();
            }
            test.assertTrue(replacement.getStackInSlot(0).getCount() == 4 && replacement.getStackInSlot(outputSlot).isEmpty(),
                    "Transfers must update the replacement inventory");
            level.destroyBlock(pos, false);
            // Start the next reactor type with new ports, while testing reconnection above with the same ports.
            level.destroyBlock(inputPos, false);
            level.destroyBlock(outputPos, false);
        }
        test.succeed();
    }

    @GameTest
    public void dissolverChecksEachGroupOnceAndCompletesEveryRoll(GameTestHelper test) {
        var recipes = MachineRecipes.all(test.getLevel(), DissolverRecipe.Type.INSTANCE);
        var planks = recipes.stream().filter(r -> r.getId().equals(Identifier.parse("alchemistry:dissolver/planks"))).findFirst().orElseThrow();
        test.assertTrue(count(planks.getProbabilityOutput().calculateOutput(draws(0.2499))) == 1, "25% recipe must accept a draw below 25%");
        test.assertTrue(planks.getProbabilityOutput().calculateOutput(draws(0.25)).isEmpty(), "25% recipe must reject the boundary");
        var gravel = recipes.stream().filter(r -> r.getId().equals(Identifier.parse("alchemistry:dissolver/gravel"))).findFirst().orElseThrow();
        test.assertTrue(count(gravel.getProbabilityOutput().calculateOutput(draws(0.0099))) == 1, "1% recipe must accept a draw below 1%");
        test.assertTrue(gravel.getProbabilityOutput().calculateOutput(draws(0.01)).isEmpty(), "1% recipe must reject the boundary");
        var bones = recipes.stream().filter(r -> r.getId().equals(Identifier.parse("alchemistry:dissolver/bone_block"))).findFirst().orElseThrow();
        test.assertTrue(count(bones.getProbabilityOutput().calculateOutput(draws(0.9, 0.1, 0.9, 0.1, 0.9, 0.1, 0.9, 0.1, 0.9))) == 4,
                "All nine bone-block rolls must run even when the first and intermediate rolls miss");
        var groups = new ProbabilitySet(List.of(new ProbabilityGroup(List.of(new ItemStack(Items.DIAMOND)), 25),
                new ProbabilityGroup(List.of(new ItemStack(Items.GOLD_INGOT)), 50)), false, 2);
        var output = groups.calculateOutput(draws(0.99, 0.25, 0.1, 0.75));
        test.assertTrue(output.size() == 2 && count(output) == 2 && output.stream().anyMatch(s -> s.is(Items.DIAMOND))
                        && output.stream().anyMatch(s -> s.is(Items.GOLD_INGOT)), "Groups must roll independently within each operation");
        var boundaries = new ProbabilitySet(List.of(new ProbabilityGroup(List.of(new ItemStack(Items.DIAMOND)), 0),
                new ProbabilityGroup(List.of(new ItemStack(Items.GOLD_INGOT)), 100)), false, 1);
        var boundaryOutput = boundaries.calculateOutput(draws(0, 0.999999));
        test.assertTrue(boundaryOutput.size() == 1 && boundaryOutput.getFirst().is(Items.GOLD_INGOT), "0% never succeeds and 100% always succeeds");
        test.succeed();
    }

    @GameTest
    public void weightedDissolverSelectsExactlyOneGroupPerRoll(GameTestHelper test) {
        var weighted = new ProbabilitySet(List.of(new ProbabilityGroup(List.of(new ItemStack(Items.DIAMOND)), 0),
                new ProbabilityGroup(List.of(new ItemStack(Items.IRON_INGOT)), 1),
                new ProbabilityGroup(List.of(new ItemStack(Items.GOLD_INGOT)), 3)), true, 3);
        var output = weighted.calculateOutput(draws(0, 0.25, 0.999999));
        test.assertTrue(count(output) == 3 && output.stream().noneMatch(s -> s.is(Items.DIAMOND))
                        && output.stream().filter(s -> s.is(Items.IRON_INGOT)).mapToInt(ItemStack::getCount).sum() == 1
                        && output.stream().filter(s -> s.is(Items.GOLD_INGOT)).mapToInt(ItemStack::getCount).sum() == 2,
                "Weighted selection must preserve relative weights, one result per roll, and zero-weight exclusion");
        test.succeed();
    }

    private AbstractReactorBlockEntity buildReactor(GameTestHelper test, BlockPos pos, ReactorType type) {
        var level = test.getLevel();
        Block controller = type == ReactorType.FISSION ? BlockRegistry.FISSION_CONTROLLER : BlockRegistry.FUSION_CONTROLLER;
        level.setBlockAndUpdate(pos, controller.defaultBlockState());
        var machine = (AbstractReactorBlockEntity) level.getBlockEntity(pos);
        new ReactorShape(pos, type, level).createShapeMap().forEach((box, blocks) -> BlockPos.betweenClosedStream(box).forEach(p -> {
            if (!p.equals(pos) && !level.getBlockState(p).is(BlockRegistry.REACTOR_INPUT) && !level.getBlockState(p).is(BlockRegistry.REACTOR_OUTPUT)) {
                level.setBlock(p, blocks.getFirst().defaultBlockState(), 3);
            }
        }));
        level.setBlockAndUpdate(pos.below(), BlockRegistry.REACTOR_ENERGY.defaultBlockState());
        level.setBlockAndUpdate(pos.below().west(), BlockRegistry.REACTOR_INPUT.defaultBlockState());
        level.setBlockAndUpdate(pos.below().east(), BlockRegistry.REACTOR_OUTPUT.defaultBlockState());
        machine.insertEnergy(100000);
        machine.setPaused(true);
        return machine;
    }

    private AbstractReactorBlockEntity readSavedController(GameTestHelper test, AbstractReactorBlockEntity machine) {
        var level = test.getLevel();
        var pos = machine.getBlockPos();
        var savedChunk = level.getChunkSource().chunkMap.read(level.getChunkAt(pos).getPos()).join().orElseThrow();
        var tag = savedChunk.getListOrEmpty("block_entities").stream().filter(t -> isAt(t, pos))
                .map(t -> (CompoundTag) t).findFirst().orElseThrow();
        return (AbstractReactorBlockEntity) BlockEntity.loadStatic(pos, machine.getBlockState(), tag, level.registryAccess());
    }

    private boolean isAt(net.minecraft.nbt.Tag tag, BlockPos pos) {
        return tag instanceof CompoundTag data && data.getIntOr("x", 0) == pos.getX()
                && data.getIntOr("y", 0) == pos.getY() && data.getIntOr("z", 0) == pos.getZ();
    }

    private int droppedDiamonds(GameTestHelper test, BlockPos pos) {
        return test.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(5)).stream()
                .filter(e -> e.getItem().is(Items.DIAMOND)).mapToInt(e -> e.getItem().getCount()).sum();
    }

    private int count(List<ItemStack> stacks) {
        return stacks.stream().mapToInt(ItemStack::getCount).sum();
    }

    private Random draws(double... values) {
        return new Random(0) {
            private int index;

            @Override
            public double nextDouble() {
                if (index >= values.length) throw new AssertionError("Unexpected extra probability check");
                return values[index++];
            }
        };
    }
}
