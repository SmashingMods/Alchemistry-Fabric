package com.smashingmods.alchemistry.client.guide;

import com.smashingmods.alchemistry.api.blockentity.ReactorShape;
import com.smashingmods.alchemistry.api.blockentity.ReactorType;
import com.smashingmods.alchemistry.registry.BlockRegistry;
import com.smashingmods.alchemistry.registry.ItemRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Client-only construction aid, using the same shape rules as the reactor controller. */
public final class ReactorProjection {
    private static ClientLevel projectionLevel;
    private static BlockPos anchor;
    private static ReactorType type;
    private static Map<BlockPos, List<Block>> requirements = Map.of();

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (!level.isClientSide() || !player.getItemInHand(hand).is(ItemRegistry.GUIDE_BOOK)) return InteractionResult.PASS;
            Block block = level.getBlockState(hit.getBlockPos()).getBlock();
            if (block != BlockRegistry.FISSION_CONTROLLER && block != BlockRegistry.FUSION_CONTROLLER) return InteractionResult.PASS;
            if (player.isShiftKeyDown()) clear();
            else start((ClientLevel) level, hit.getBlockPos(), block == BlockRegistry.FISSION_CONTROLLER ? ReactorType.FISSION : ReactorType.FUSION);
            return InteractionResult.SUCCESS;
        });
        ClientTickEvents.END_CLIENT_TICK.register(ReactorProjection::tick);
    }

    public static void beginFromGuide() {
        var client = Minecraft.getInstance();
        if (client.player != null) client.player.sendSystemMessage(Component.literal("Use this guide on a reactor controller to project its structure. Sneak-use to hide it."));
        client.gui.setScreen(null);
    }

    public static void start(ClientLevel level, BlockPos controller, ReactorType reactorType) {
        projectionLevel = level;
        anchor = controller.immutable();
        type = reactorType;
        Map<BlockPos, List<Block>> blocks = new LinkedHashMap<>();
        new ReactorShape(anchor, type, level).createShapeMap().forEach((box, allowed) -> {
            for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) blocks.put(pos.immutable(), allowed);
        });
        requirements = Map.copyOf(blocks);
    }

    public static void clear() { anchor = null; projectionLevel = null; requirements = Map.of(); }
    public static boolean isActive() { return anchor != null; }
    public static int missingBlocks() {
        if (!isActive()) return 0;
        return (int) requirements.entrySet().stream().filter(entry -> !entry.getValue().contains(projectionLevel.getBlockState(entry.getKey()).getBlock())).count();
    }

    private static void tick(Minecraft client) {
        if (!isActive()) return;
        Block controller = type == ReactorType.FISSION ? BlockRegistry.FISSION_CONTROLLER : BlockRegistry.FUSION_CONTROLLER;
        if (client.level != projectionLevel || client.player == null || !client.level.getBlockState(anchor).is(controller)) { clear(); return; }
        if (client.player.distanceToSqr(Vec3.atCenterOf(anchor)) > 64 * 64) return;
        try (var ignored = client.collectPerTickGizmos()) {
            Vec3 eye = client.player.getEyePosition();
            Vec3 end = eye.add(client.player.getLookAngle().scale(12));
            BlockPos selected = null;
            List<Block> selectedBlocks = null;
            double nearest = Double.MAX_VALUE;
            for (var entry : requirements.entrySet()) {
                var state = client.level.getBlockState(entry.getKey());
                if (entry.getValue().contains(state.getBlock())) continue;
                int color = state.isAir() ? 0xFF62C9FF : 0xFFFF6961;
                Gizmos.cuboid(entry.getKey(), -0.025F, GizmoStyle.strokeAndFill(color, 1.5F, (color & 0xFFFFFF) | 0x18000000));
                var intersection = new AABB(entry.getKey()).clip(eye, end);
                if (intersection.isPresent() && intersection.get().distanceToSqr(eye) < nearest) {
                    nearest = intersection.get().distanceToSqr(eye); selected = entry.getKey(); selectedBlocks = entry.getValue();
                }
            }
            int missing = missingBlocks();
            Gizmos.billboardText(missing == 0 ? "Structure complete — check energy and item ports" : missing + " blocks remaining • Blue: missing / Red: replace",
                Vec3.atCenterOf(anchor.above(4)), TextGizmo.Style.whiteAndCentered().withScale(0.6F));
            if (selected != null) {
                String label = selectedBlocks.stream().map(block -> block.getName().getString()).collect(java.util.stream.Collectors.joining(" / "));
                Gizmos.billboardText(label, Vec3.atCenterOf(selected), TextGizmo.Style.whiteAndCentered().withScale(0.45F)).setAlwaysOnTop();
            }
        }
    }
}
