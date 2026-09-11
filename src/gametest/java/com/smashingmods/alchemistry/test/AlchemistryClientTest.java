package com.smashingmods.alchemistry.test;

import com.smashingmods.alchemistry.api.blockentity.AbstractProcessingBlockEntity;
import com.smashingmods.alchemistry.api.blockentity.AbstractFluidBlockEntity;
import com.smashingmods.alchemistry.api.container.AbstractAlchemistryScreenHandler;
import com.smashingmods.alchemistry.client.guide.GuideBookScreen;
import com.smashingmods.alchemistry.common.block.combiner.CombinerBlockEntity;
import com.smashingmods.alchemistry.network.AlchemistryClientNetwork;
import com.smashingmods.alchemistry.network.packets.ProcessingButtonPacket;
import com.smashingmods.alchemistry.registry.BlockRegistry;
import com.smashingmods.alchemistry.registry.ItemRegistry;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.renderer.item.MissingItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;

public class AlchemistryClientTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        context.getInput().resizeWindow(1200, 800);
        try (var world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            context.runOnClient(client -> {
                client.options.guiScale().set(2); client.resizeGui();
                for (var item : ItemRegistry.ITEMS) {
                    var id = BuiltInRegistries.ITEM.getKey(item);
                    if (client.getModelManager().getItemModel(id) instanceof MissingItemModel) throw new AssertionError("Missing model " + id);
                    for (var mode : ItemDisplayContext.values()) {
                        var state = new ItemStackRenderState();
                        client.getItemModelResolver().updateForTopItem(state, new ItemStack(item), mode, null, null, 0);
                        if (state.isEmpty()) throw new AssertionError("Empty model " + id + " / " + mode);
                        state.getModelBoundingBox();
                    }
                }
            });
            BlockPos pos = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer().blockPosition().offset(2, 0, 0));
            for (Block block : new Block[]{BlockRegistry.ATOMIZER, BlockRegistry.LIQUIFIER, BlockRegistry.DISSOLVER, BlockRegistry.COMPACTOR, BlockRegistry.COMBINER, BlockRegistry.FISSION_CONTROLLER, BlockRegistry.FUSION_CONTROLLER}) {
                world.getServer().runOnServer(server -> {
                    var level = world.getConnection().getServerLevel();
                    level.setBlockAndUpdate(pos, block.defaultBlockState());
                    var machine = (AbstractProcessingBlockEntity) level.getBlockEntity(pos);
                    machine.setPaused(true);
                    machine.getEnergyStorage().amount = 100000;
                    if (machine instanceof AbstractFluidBlockEntity fluid) fluid.insertFluid(FluidVariant.of(Fluids.WATER), 12000 * 81L);
                    machine.forceSync();
                });
                world.getConnection().waitForClientboundPackets();
                context.waitTicks(3);
                world.getServer().runOnServer(server -> world.getConnection().getServerPlayer().openMenu((AbstractProcessingBlockEntity) world.getConnection().getServerLevel().getBlockEntity(pos)));
                world.getConnection().waitForClientboundPackets();
                context.waitFor(client -> client.player.containerMenu instanceof AbstractAlchemistryScreenHandler menu && menu.getBlockEntity().getBlockState().is(block));
                context.waitTicks(8);
                context.runOnClient(client -> {
                    var menu = (AbstractAlchemistryScreenHandler) client.player.containerMenu;
                    if (menu.getPropertyDelegate().get(2) != 100000) throw new AssertionError("Energy sync truncated for " + block + ": " + menu.getPropertyDelegate().get(2));
                    if (menu.getBlockEntity() instanceof AbstractFluidBlockEntity fluid && fluid.getFluidStorage().getAmount() != 12000 * 81L) throw new AssertionError("Fluid sync truncated");
                    if (menu.getBlockEntity() instanceof CombinerBlockEntity combiner && combiner.getRecipes().isEmpty()) throw new AssertionError("Combiner recipe list missing");
                    AlchemistryClientNetwork.sendToServer(new ProcessingButtonPacket(pos, true, true));
                });
                world.getConnection().waitForServerboundPackets();
                world.getServer().runOnServer(server -> {
                    var machine = (AbstractProcessingBlockEntity) world.getConnection().getServerLevel().getBlockEntity(pos);
                    if (!machine.isRecipeLocked() || !machine.isProcessingPaused()) throw new AssertionError("Control packet failed");
                });
                context.getInput().setCursorPos(0, 0);
                context.takeScreenshot(BuiltInRegistries.BLOCK.getKey(block).getPath());
                context.runOnClient(client -> client.player.closeContainer());
                world.getConnection().waitForServerboundPackets();
            }
            for (String category : new String[]{"atomizer", "liquifier", "dissolver", "compactor", "combiner", "fission_controller", "fusion_controller"}) {
                context.runOnClient(client -> {
                    var view = me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder().addCategory(me.shedaniel.rei.api.common.category.CategoryIdentifier.of("alchemistry", category));
                    if (view.streamDisplays().findAny().isEmpty() || !view.open()) throw new AssertionError("Missing REI recipes: " + category);
                });
                context.waitTicks(3);
                context.takeScreenshot("rei-" + category);
                context.setScreen(() -> null);
            }
            context.setScreen(GuideBookScreen::new);
            context.waitTicks(5);
            context.takeScreenshot("guide-book");
            context.clickScreenButton("Fission Multiblock");
            context.waitTicks(3);
            context.takeScreenshot("guide-reactor");
            context.clickScreenButton("Project in world");
            context.runOnClient(client -> {
                client.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ItemRegistry.GUIDE_BOOK));
                var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false);
                net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.invoker().interact(client.player, client.level, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
                if (!com.smashingmods.alchemistry.client.guide.ReactorProjection.isActive() || com.smashingmods.alchemistry.client.guide.ReactorProjection.missingBlocks() != 100) throw new AssertionError("Reactor projection shape must match controller requirements");
                client.player.setPos(pos.getX() + 8, pos.getY() + 2, pos.getZ() - 7);
                var look = net.minecraft.world.phys.Vec3.atCenterOf(pos.offset(0, 1, 2)).subtract(client.player.getEyePosition());
                client.player.setYRot((float) Math.toDegrees(Math.atan2(-look.x, look.z)));
                client.player.setXRot((float) -Math.toDegrees(Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z))));
            });
            context.waitTicks(10);
            context.runOnClient(client -> {
                if (client.getPerTickGizmos().isEmpty()) throw new AssertionError("Projection must submit world graphics");
            });
            context.takeScreenshot("reactor-world-projection");
            context.runOnClient(client -> com.smashingmods.alchemistry.client.guide.ReactorProjection.clear());
            context.setScreen(() -> null);
        }
    }
}
