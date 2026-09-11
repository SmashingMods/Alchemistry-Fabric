package com.smashingmods.alchemistry.test;

import com.google.gson.*;
import com.smashingmods.alchemistry.Alchemistry;
import com.smashingmods.alchemistry.Config;
import com.smashingmods.alchemistry.registry.*;
import com.smashingmods.alchemistry.api.blockentity.*;
import com.smashingmods.alchemistry.api.recipe.*;
import com.smashingmods.alchemistry.common.block.atomizer.AtomizerBlockEntity;
import com.smashingmods.alchemistry.common.block.liquifier.LiquifierBlockEntity;
import com.smashingmods.alchemistry.common.block.compactor.CompactorBlockEntity;
import com.smashingmods.alchemistry.common.block.combiner.CombinerBlockEntity;
import com.smashingmods.alchemistry.common.block.dissolver.DissolverBlockEntity;
import com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe;
import com.smashingmods.alchemistry.common.recipe.dissolver.DissolverRecipe;
import com.smashingmods.alchemistry.common.recipe.dissolver.ProbabilityGroup;
import com.smashingmods.alchemistry.common.recipe.dissolver.ProbabilitySet;
import com.smashingmods.alchemistry.common.recipe.atomizer.AtomizerRecipe;
import com.smashingmods.alchemistry.common.recipe.liquifier.LiquifierRecipe;
import com.smashingmods.alchemistry.common.recipe.compactor.CompactorRecipe;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.crafting.*;
import java.io.InputStreamReader;
import java.util.*;

public class AlchemistryGameTest {
    private Identifier id(String path) { return Identifier.parse("alchemistry:" + path); }

    @GameTest public void diamondRequiresBothGraphiteStacks(GameTestHelper test) {
        var pos = new BlockPos(1, 1, 1);
        test.setBlock(pos, BlockRegistry.COMBINER);
        var machine = test.getBlockEntity(pos, CombinerBlockEntity.class);
        var recipe = MachineRecipes.all(test.getLevel(), CombinerRecipe.Type.INSTANCE).stream()
                .filter(r -> r.getId().equals(id("combiner/diamond"))).findFirst().orElseThrow();
        machine.setRecipe(recipe);
        machine.setRecipeLocked(true);
        machine.insertEnergy(100000);
        machine.setStackInSlot(0, recipe.getInput().getFirst());
        machine.setStackInSlot(1, new ItemStack(Items.DIRT));
        for (int i = 0; i <= Config.Common.combinerTicksPerOperation.get(); i++) machine.tick();
        test.assertTrue(machine.getStackInSlot(4).isEmpty() && machine.getStackInSlot(0).getCount() == 64
                && machine.getEnergyStorage().amount == 100000, "Insufficient graphite must not produce diamonds or consume inputs/energy");
        machine.setStackInSlot(1, ItemStack.EMPTY);
        machine.setStackInSlot(3, recipe.getInput().getFirst().copyWithCount(63));
        test.assertTrue(!machine.canProcessRecipe(), "127 graphite must not satisfy the 128-graphite recipe");
        machine.setStackInSlot(3, recipe.getInput().getFirst());
        for (int i = 0; i <= Config.Common.combinerTicksPerOperation.get(); i++) machine.tick();
        test.assertTrue(machine.getStackInSlot(0).isEmpty() && machine.getStackInSlot(3).isEmpty()
                && ItemStack.matches(machine.getStackInSlot(4), recipe.getOutput()), "Two graphite stacks in shuffled slots must produce exactly one recipe output");
        test.succeed();
    }

    @GameTest public void packedIceConsumesEveryWaterStack(GameTestHelper test) {
        var pos = new BlockPos(1, 1, 1);
        test.setBlock(pos, BlockRegistry.COMBINER);
        var machine = test.getBlockEntity(pos, CombinerBlockEntity.class);
        var recipe = MachineRecipes.all(test.getLevel(), CombinerRecipe.Type.INSTANCE).stream()
                .filter(r -> r.getId().equals(id("combiner/packed_ice"))).findFirst().orElseThrow();
        machine.setRecipe(recipe);
        machine.setRecipeLocked(true);
        machine.insertEnergy(100000);
        for (int slot = 0; slot < 4; slot++) machine.setStackInSlot(slot, recipe.getInput().getFirst().copyWithCount(64));
        for (int i = 0; i <= Config.Common.combinerTicksPerOperation.get(); i++) machine.tick();
        for (int slot = 0; slot < 4; slot++) {
            test.assertTrue(machine.getStackInSlot(slot).getCount() == 28, "Packed ice must consume 36 water from each of its four input stacks");
        }
        test.assertTrue(ItemStack.matches(machine.getStackInSlot(4), recipe.getOutput()), "Packed ice output must match the recipe");
        test.assertTrue(!machine.canProcessRecipe(), "Leftover water must not allow another operation");
        test.succeed();
    }

    @GameTest public void duplicateIngredientsWithUnequalCounts(GameTestHelper test) {
        var pos = new BlockPos(1, 1, 1);
        test.setBlock(pos, BlockRegistry.COMBINER);
        var machine = test.getBlockEntity(pos, CombinerBlockEntity.class);
        var recipe = new CombinerRecipe(id("test/unequal_inputs"),
                List.of(RecipeStack.of(Items.COBBLESTONE, 1), RecipeStack.of(Items.COBBLESTONE, 64)), RecipeStack.of(Items.STONE, 1));
        try (var ignored = RecipeTestScope.withRecipes(test.getLevel(), recipe)) {
            machine.setRecipe(recipe);
            machine.setRecipeLocked(true);
            machine.insertEnergy(100000);
            machine.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 64));
            machine.setStackInSlot(2, new ItemStack(Items.COBBLESTONE, 1));
            for (int i = 0; i <= Config.Common.combinerTicksPerOperation.get(); i++) machine.tick();
            test.assertTrue(machine.getStackInSlot(0).isEmpty() && machine.getStackInSlot(2).isEmpty()
                    && ItemStack.matches(machine.getStackInSlot(4), recipe.getOutput()), "Duplicate ingredients with unequal counts must match and consume distinct slots");
        }
        test.succeed();
    }

    @GameTest public void dissolverOutputRespectsStackLimits(GameTestHelper test) {
        // One weighted group makes every roll deterministic while exercising repeated output merging.
        for (Item item : List.of(Items.COPPER_INGOT, Items.SNOWBALL, Items.IRON_SWORD)) {
            var output = new ProbabilitySet(List.of(new ProbabilityGroup(List.of(new ItemStack(item, 9)))), true, 16);
            var stacks = output.calculateOutput();
            test.assertTrue(stacks.stream().mapToInt(ItemStack::getCount).sum() == 144, "Splitting repeated outputs must preserve all 144 items");
            for (ItemStack stack : stacks) {
                test.assertTrue(stack.is(item) && stack.getCount() <= stack.getMaxStackSize(), "Outputs must respect each item's own stack limit");
            }
        }
        var output = new ProbabilitySet(List.of(new ProbabilityGroup(List.of(new ItemStack(Items.COPPER_INGOT, 144)))), true, 1);
        var stacks = output.calculateOutput();
        test.assertTrue(stacks.size() == 3 && stacks.get(0).getCount() == 64 && stacks.get(1).getCount() == 64
                && stacks.get(2).getCount() == 16, "A single oversized result must split into 64, 64, and 16");

        var pos = new BlockPos(1, 1, 1);
        test.setBlock(pos, BlockRegistry.DISSOLVER);
        var machine = test.getBlockEntity(pos, DissolverBlockEntity.class);
        var recipe = new DissolverRecipe(id("test/repeated_output"),
                new ProbabilitySet(List.of(new ProbabilityGroup(List.of(new ItemStack(Items.COPPER_INGOT, 9)))), true, 16), Ingredient.of(Items.STONE));
        try (var ignored = RecipeTestScope.withRecipes(test.getLevel(), recipe)) {
            machine.setRecipe(recipe);
            machine.setRecipeLocked(true);
            machine.insertEnergy(100000);
            machine.setStackInSlot(0, new ItemStack(Items.STONE));
            for (int i = 0; i <= Config.Common.dissolverTicksPerOperation.get() + 5; i++) machine.tick();
            test.assertTrue(machine.getStackInSlot(0).isEmpty() && machine.getItems().stream().mapToInt(ItemStack::getCount).sum() == 144,
                    "Dissolver must deliver all 144 items from the internal buffer");
            for (ItemStack stack : machine.getItems()) test.assertTrue(stack.getCount() <= stack.getMaxStackSize(), "Machine output slots must not exceed stack limits");
            var saved = machine.saveWithFullMetadata(test.getLevel().registryAccess());
            var restored = (DissolverBlockEntity) BlockEntity.loadStatic(machine.getBlockPos(), machine.getBlockState(), saved, test.getLevel().registryAccess());
            test.assertTrue(restored != null && restored.getItems().stream().mapToInt(ItemStack::getCount).sum() == 144,
                    "All split outputs must survive save/reload");
        }
        test.succeed();
    }

    @GameTest public void bucketsAndGhostSlots(GameTestHelper test) {
        BlockPos pos = test.absolutePos(new BlockPos(1, 2, 1));
        var level = test.getLevel();
        level.setBlockAndUpdate(pos, BlockRegistry.ATOMIZER.defaultBlockState());
        var tank = (AtomizerBlockEntity) level.getBlockEntity(pos);
        var player = test.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setPos(net.minecraft.world.phys.Vec3.atCenterOf(pos));
        var hand = net.minecraft.world.InteractionHand.OFF_HAND;
        player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false);
        level.getBlockState(pos).useItemOn(player.getItemInHand(hand), level, player, hand, hit);
        test.assertTrue(tank.getFluidStorage().getAmount() == 81000 && player.getItemInHand(hand).is(Items.BUCKET), "Offhand bucket fills tank");
        player.setItemInHand(hand, new ItemStack(Items.BUCKET, 5));
        level.getBlockState(pos).useItemOn(player.getItemInHand(hand), level, player, hand, hit);
        test.assertTrue(tank.getFluidStorage().getAmount() == 0 && player.getItemInHand(hand).getCount() == 4 && player.getInventory().contains(new ItemStack(Items.WATER_BUCKET)), "Stacked buckets are preserved when filling one");
        level.setBlockAndUpdate(pos, BlockRegistry.COMPACTOR.defaultBlockState());
        var compactor = (CompactorBlockEntity) level.getBlockEntity(pos);
        var recipe = MachineRecipes.all(level, CompactorRecipe.Type.INSTANCE).getFirst();
        var menu = compactor.createMenu(1, player.getInventory(), player);
        var carried = recipe.getOutput().copy();
        menu.setCarried(carried);
        menu.clicked(37, 0, net.minecraft.world.inventory.ContainerInput.PICKUP, player);
        test.assertTrue(ItemStack.isSameItemSameComponents(compactor.getTarget(), carried) && menu.getCarried().getCount() == carried.getCount() && compactor.getItem(1).isEmpty(), "Target click copies recipe without consuming or duplicating items");
        compactor.setItem(2, recipe.getOutput().copy());
        test.assertTrue(!menu.quickMoveStack(player, 38).isEmpty() && compactor.getItem(2).isEmpty(), "Shift-click extracts compactor output");
        test.succeed();
    }

    @GameTest public void contentAndRecipeCodecs(GameTestHelper test) {
        var manifest = JsonParser.parseReader(new InputStreamReader(getClass().getResourceAsStream("/alchemistry-content-manifest.json"))).getAsJsonObject();
        var manager = test.getLevel().getServer().getRecipeManager();
        for (var name : manifest.getAsJsonArray("recipes")) {
            var key = ResourceKey.create(Registries.RECIPE, id(name.getAsString()));
            test.assertTrue(manager.byKey(key).isPresent(), "Missing recipe: " + key);
        }
        long count = manager.getRecipes().stream().filter(h -> h.id().identifier().getNamespace().equals("alchemistry")).count();
        test.assertTrue(count >= 4880, "Original recipe count must be preserved");
        for (var holder : manager.getRecipes()) if (holder.value() instanceof AbstractAlchemistryRecipe recipe) {
            var buf = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), test.getLevel().registryAccess());
            try {
                Recipe.STREAM_CODEC.encode(buf, recipe);
                Recipe<?> decoded = Recipe.STREAM_CODEC.decode(buf);
                var ops = test.getLevel().registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
                var before = Recipe.CODEC.encodeStart(ops, recipe).getOrThrow();
                var after = Recipe.CODEC.encodeStart(ops, decoded).getOrThrow();
                test.assertTrue(before.equals(after), "Recipe synchronization changed data: " + holder.id());
            } finally { buf.release(); }
        }
        var params = new CreativeModeTab.ItemDisplayParameters(test.getLevel().enabledFeatures(), true, test.getLevel().registryAccess());
        Alchemistry.MACHINE_TAB.buildContents(params);
        test.assertTrue(Alchemistry.MACHINE_TAB.getDisplayItems().size() == 15, "14 machine blocks plus guide must be in creative tab");
        for (Item item : ItemRegistry.ITEMS) if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            var drops = Block.getDrops(block.defaultBlockState(), test.getLevel(), test.absolutePos(new BlockPos(1,1,1)), null, null, new ItemStack(Items.DIAMOND_PICKAXE));
            test.assertTrue(drops.size() == 1 && drops.getFirst().is(item), "Missing block drop: " + block);
        }
        test.succeed();
    }
    @GameTest public void fluidBoundariesAndPersistence(GameTestHelper test) {
        var pos = new BlockPos(1,1,1); test.setBlock(pos, BlockRegistry.ATOMIZER);
        var machine = test.getBlockEntity(pos, AtomizerBlockEntity.class);
        var recipe = MachineRecipes.all(test.getLevel(), AtomizerRecipe.Type.INSTANCE).stream().filter(r -> r.getFluidInput().isOf(Fluids.WATER)).findFirst().orElseThrow();
        machine.setRecipe(recipe); machine.setRecipeLocked(true); machine.insertEnergy(100000);
        machine.insertFluid(FluidVariant.of(Fluids.WATER), 500 * 81 - 1);
        test.assertTrue(!machine.canProcessRecipe(), "Atomizer must require the entire fluid amount");
        machine.insertFluid(FluidVariant.of(Fluids.WATER), 1);
        test.assertTrue(machine.canProcessRecipe(), "Atomizer should process 500 mB");
        for (int i=0;i<=Config.Common.atomizerTicksPerOperation.get();i++) machine.tick();
        test.assertTrue(machine.getStackInSlot(0).getCount()==8 && machine.getFluidStorage().amount==0, "Atomizer conversion must be lossless");
        machine.setPaused(true); machine.setProgress(17);
        var saved=machine.saveWithFullMetadata(test.getLevel().registryAccess());
        var restored=(AtomizerBlockEntity)BlockEntity.loadStatic(machine.getBlockPos(), machine.getBlockState(), saved, test.getLevel().registryAccess());
        test.assertTrue(restored != null && restored.isProcessingPaused() && restored.isRecipeLocked() && restored.getProgress()==17 && restored.getRecipe()!=null, "Locked recipe and processing state must survive reload");
        test.assertTrue(restored.getEnergyStorage().amount==machine.getEnergyStorage().amount && restored.getStackInSlot(0).getCount()==8, "Energy and contents must survive reload");
        var liquidPos = new BlockPos(2,1,1); test.setBlock(liquidPos, BlockRegistry.LIQUIFIER);
        var liquid=test.getBlockEntity(liquidPos, LiquifierBlockEntity.class);
        var liquify=MachineRecipes.all(test.getLevel(),LiquifierRecipe.Type.INSTANCE).stream().filter(r -> r.getFluidOutput().isOf(Fluids.WATER)).findFirst().orElseThrow();
        liquid.setRecipe(liquify); liquid.setRecipeLocked(true); liquid.insertEnergy(100000);
        liquid.setStackInSlot(0,new ItemStack(liquify.getInput().items().findFirst().orElseThrow(),8));
        liquid.insertFluid(FluidVariant.of(Fluids.WATER),liquid.getFluidStorage().getCapacity()-500*81+1);
        test.assertTrue(!liquid.canProcessRecipe(),"Full liquifier must not consume ingredients");
        liquid.extractFluid(FluidVariant.of(Fluids.WATER),1);
        test.assertTrue(liquid.canProcessRecipe(),"Exact remaining capacity should be accepted");
        for(int i=0;i<=Config.Common.liquifierTicksPerOperation.get();i++)liquid.tick();
        test.assertTrue(liquid.getStackInSlot(0).isEmpty() && liquid.getFluidStorage().amount==liquid.getFluidStorage().getCapacity(),"Liquifier output must fill tank exactly");
        test.succeed();
    }

    @GameTest public void solidMachinesAndControls(GameTestHelper test) {
        BlockPos pos = new BlockPos(1,1,1);
        test.setBlock(pos, BlockRegistry.COMPACTOR);
        var compactor = test.getBlockEntity(pos, CompactorBlockEntity.class);
        var compact = MachineRecipes.all(test.getLevel(), CompactorRecipe.Type.INSTANCE).stream().filter(r -> r.getId().equals(id("compactor/chromium_dust"))).findFirst().orElseThrow();
        compactor.setStackInSlot(0, compact.getInput().copy()); compactor.setTarget(compact.getOutput());
        compactor.setRecipe(compact); compactor.setRecipeLocked(true); compactor.insertEnergy(100000); compactor.setPaused(true);
        compactor.tick(); test.assertTrue(compactor.getProgress()==0,"Paused machine must not advance");
        compactor.setPaused(false);
        for(int i=0;i<=Config.Common.compactorTicksPerOperation.get();i++)compactor.tick();
        test.assertTrue(compactor.getStackInSlot(0).isEmpty() && ItemStack.isSameItemSameComponents(compactor.getStackInSlot(2),compact.getOutput()),"Compactor must consume exact input and produce the target");
        test.assertTrue(compactor.canPlaceItemThroughFace(0, compact.getInput(), net.minecraft.core.Direction.UP) && !compactor.canTakeItemThroughFace(0,compact.getInput(),net.minecraft.core.Direction.DOWN) && compactor.canTakeItemThroughFace(2,compact.getOutput(),net.minecraft.core.Direction.DOWN),"Compactor automation sides changed");
        BlockPos comboPos=new BlockPos(2,1,1); test.setBlock(comboPos,BlockRegistry.COMBINER);
        var combo=test.getBlockEntity(comboPos,com.smashingmods.alchemistry.common.block.combiner.CombinerBlockEntity.class);
        var recipe=MachineRecipes.all(test.getLevel(),com.smashingmods.alchemistry.common.recipe.combiner.CombinerRecipe.Type.INSTANCE).stream().filter(r -> r.getId().equals(id("combiner/water"))).findFirst().orElseThrow();
        combo.setRecipe(recipe); combo.setRecipeLocked(true); combo.insertEnergy(100000);
        combo.setStackInSlot(2,recipe.getInput().get(0).copy()); combo.setStackInSlot(0,recipe.getInput().get(1).copy());
        for(int i=0;i<=Config.Common.combinerTicksPerOperation.get();i++)combo.tick();
        test.assertTrue(combo.getStackInSlot(0).isEmpty() && combo.getStackInSlot(2).isEmpty() && ItemStack.matches(combo.getStackInSlot(4),recipe.getOutput()),"Combiner must accept shuffled inputs with exact quantities");
        BlockPos dissolvePos=new BlockPos(3,1,1);test.setBlock(dissolvePos,BlockRegistry.DISSOLVER);
        var dissolver=test.getBlockEntity(dissolvePos,com.smashingmods.alchemistry.common.block.dissolver.DissolverBlockEntity.class);
        dissolver.setStackInSlot(0,new ItemStack(Items.IRON_INGOT));dissolver.insertEnergy(100000);
        for(int i=0;i<=Config.Common.dissolverTicksPerOperation.get();i++)dissolver.tick();
        test.assertTrue(dissolver.getStackInSlot(0).isEmpty() && dissolver.getItems().stream().mapToInt(ItemStack::getCount).sum()==16,"Dissolver must produce 16 iron elements");
        test.succeed();
    }
    @GameTest public void fissionAndFusionMultiblocks(GameTestHelper test) {
        for (ReactorType type : ReactorType.values()) {
            BlockPos pos = new BlockPos(3,2,1);
            Block controllerBlock = type==ReactorType.FISSION ? BlockRegistry.FISSION_CONTROLLER : BlockRegistry.FUSION_CONTROLLER;
            test.setBlock(pos,controllerBlock);
            var controller=test.getBlockEntity(pos,AbstractReactorBlockEntity.class);
            controller.insertEnergy(100000);
            controller.tick();
            test.assertTrue(controller.getPowerState()==PowerState.DISABLED,"Incomplete reactor must be disabled");
            ReactorShape shape=new ReactorShape(controller.getBlockPos(),type,test.getLevel());
            shape.createShapeMap().forEach((box,blocks) -> BlockPos.betweenClosedStream(box).forEach(p -> {
                if(!p.equals(controller.getBlockPos())) test.getLevel().setBlock(p,blocks.getFirst().defaultBlockState(),3);
            }));
            var bottom=controller.getBlockPos().below();
            test.getLevel().setBlock(bottom,BlockRegistry.REACTOR_ENERGY.defaultBlockState(),3);
            test.getLevel().setBlock(bottom.west(),BlockRegistry.REACTOR_INPUT.defaultBlockState(),3);
            test.getLevel().setBlock(bottom.east(),BlockRegistry.REACTOR_OUTPUT.defaultBlockState(),3);
            test.getLevel().setBlock(bottom.west(2),BlockRegistry.REACTOR_INPUT.defaultBlockState(),3);
            controller.tick();
            test.assertTrue(controller.isValidMultiblock(),"Valid 5x5x5 reactor must form: "+type);
            if(type==ReactorType.FISSION) {
                var recipe=MachineRecipes.all(test.getLevel(),com.smashingmods.alchemistry.common.recipe.fission.FissionRecipe.Type.INSTANCE).stream().filter(r -> r.getId().equals(id("fission/helium"))).findFirst().orElseThrow();
                controller.setRecipe(recipe); controller.setRecipeLocked(true);controller.setStackInSlot(0,recipe.getInput());
                for(int i=0;i<=Config.Common.fissionTicksPerOperation.get();i++)controller.tick();
                test.assertTrue(ItemStack.matches(controller.getStackInSlot(1),recipe.getOutput1()) && ItemStack.matches(controller.getStackInSlot(2),recipe.getOutput2()),"Fission must split helium into hydrogen");
            } else {
                var recipe=MachineRecipes.all(test.getLevel(),com.smashingmods.alchemistry.common.recipe.fusion.FusionRecipe.Type.INSTANCE).getFirst();
                controller.setRecipe(recipe);controller.setRecipeLocked(true);controller.setStackInSlot(0,recipe.getInput1());controller.setStackInSlot(1,recipe.getInput2());
                for(int i=0;i<=Config.Common.fusionTicksPerOperation.get();i++)controller.tick();
                test.assertTrue(ItemStack.matches(controller.getStackInSlot(2),recipe.getOutput()),"Fusion must combine both inputs");
            }
            var energy=(com.smashingmods.alchemistry.common.block.reactor.ReactorEnergyBlockEntity)test.getLevel().getBlockEntity(bottom);
            test.assertTrue(energy.getEnergyStorage()==controller.getEnergyStorage(),"Energy port must share controller storage");
            test.getLevel().setBlock(bottom,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
            controller.tick();
            test.assertTrue(controller.getPowerState()==PowerState.DISABLED,"Breaking a required port must disable the reactor");
            test.getLevel().setBlock(controller.getBlockPos(),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
            for (var portPos : List.of(bottom.west(), bottom.west(2))) {
                var inputPort = (com.smashingmods.alchemistry.common.block.reactor.ReactorInputBlockEntity) test.getLevel().getBlockEntity(portPos);
                test.assertTrue(inputPort.getController() == null, "All ports must detach when the controller is removed");
            }
        }
        test.succeed();
    }
}
