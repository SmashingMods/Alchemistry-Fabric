package com.smashingmods.alchemistry.registry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
public class ItemRegistry {
    public static final List<Item> ITEMS = new ArrayList<>();
    public static final Item GUIDE_BOOK = registerGuide();
    private static Item registerGuide() {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.parse("alchemistry:guide_book"));
        Item item = Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().setId(key).stacksTo(1)) {
            @Override public net.minecraft.world.InteractionResult use(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) { return net.minecraft.world.InteractionResult.SUCCESS; }
        });
        ITEMS.add(item); return item;
    }
    public static final BlockItem DISSOLVER = register(BlockRegistry.DISSOLVER);
    public static final BlockItem COMBINER = register(BlockRegistry.COMBINER);
    public static final BlockItem LIQUIFIER = register(BlockRegistry.LIQUIFIER);
    public static final BlockItem ATOMIZER = register(BlockRegistry.ATOMIZER);
    public static final BlockItem COMPACTOR = register(BlockRegistry.COMPACTOR);
    public static final BlockItem FUSION_CORE = register(BlockRegistry.FUSION_CORE);
    public static final BlockItem FISSION_CORE = register(BlockRegistry.FISSION_CORE);
    public static final BlockItem REACTOR_GLASS = register(BlockRegistry.REACTOR_GLASS);
    public static final BlockItem REACTOR_ENERGY = register(BlockRegistry.REACTOR_ENERGY);
    public static final BlockItem REACTOR_INPUT = register(BlockRegistry.REACTOR_INPUT);
    public static final BlockItem REACTOR_OUTPUT = register(BlockRegistry.REACTOR_OUTPUT);
    public static final BlockItem REACTOR_CASING = register(BlockRegistry.REACTOR_CASING);
    public static final BlockItem FISSION_CONTROLLER = register(BlockRegistry.FISSION_CONTROLLER);
    public static final BlockItem FUSION_CONTROLLER = register(BlockRegistry.FUSION_CONTROLLER);
    private static BlockItem register(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        BlockItem item = Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix()) {
            @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip, TooltipFlag flag) {
                super.appendHoverText(stack, context, display, tooltip, flag);
                if (block instanceof com.smashingmods.alchemistry.api.block.AbstractAlchemistryBlock machine) machine.appendHoverText(stack, context, display, tooltip, flag);
            }
        });
        ITEMS.add(item);
        return item;
    }
    public static void registerItems() { }
}
