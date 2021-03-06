package marioandweegee3.toolbuilder;

import java.util.List;
import java.util.Map;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.swordglowsblue.artifice.api.Artifice;

import marioandweegee3.ml3api.registry.RegistryHelper;
import marioandweegee3.toolbuilder.api.ToolType;
import marioandweegee3.toolbuilder.api.effect.Effect;
import marioandweegee3.toolbuilder.api.effect.EffectInstance;
import marioandweegee3.toolbuilder.api.entry.TBInitializer;
import marioandweegee3.toolbuilder.api.item.BuiltArmorItem;
import marioandweegee3.toolbuilder.api.item.ModifierItem;
import marioandweegee3.toolbuilder.api.loot.BuiltToolLootCondition;
import marioandweegee3.toolbuilder.api.loot.serial.BuiltToolLootConditionFactory;
import marioandweegee3.toolbuilder.api.material.BowMaterial;
import marioandweegee3.toolbuilder.api.material.BuiltArmorMaterial;
import marioandweegee3.toolbuilder.api.material.BuiltToolMaterial;
import marioandweegee3.toolbuilder.api.material.HandleMaterial;
import marioandweegee3.toolbuilder.api.material.HeadMaterial;
import marioandweegee3.toolbuilder.api.material.StringMaterial;
import marioandweegee3.toolbuilder.api.registry.TBRegistries;
import marioandweegee3.toolbuilder.client.ToolBuilderClient;
import marioandweegee3.toolbuilder.common.blocks.BlockTorches;
import marioandweegee3.toolbuilder.common.blocks.Torch;
import marioandweegee3.toolbuilder.common.blocks.WallTorch;
import marioandweegee3.toolbuilder.common.command.EquipmentSlotArgumentType;
import marioandweegee3.toolbuilder.common.command.TBEffectCommand;
import marioandweegee3.toolbuilder.common.command.TBItemCommand;
import marioandweegee3.toolbuilder.common.config.ConfigHandler;
import marioandweegee3.toolbuilder.common.data.TBData;
import marioandweegee3.toolbuilder.common.data.loot_tables.BasicBlockLootTable;
import marioandweegee3.toolbuilder.common.data.recipes.ArmorRecipe;
import marioandweegee3.toolbuilder.common.data.recipes.BowRecipe;
import marioandweegee3.toolbuilder.common.data.recipes.HandleRecipe;
import marioandweegee3.toolbuilder.common.data.recipes.ToolRecipe;
import marioandweegee3.toolbuilder.common.itemgroups.Groups;
import marioandweegee3.toolbuilder.common.items.Handles;
import marioandweegee3.toolbuilder.common.items.HolyWaterItem;
import marioandweegee3.toolbuilder.common.items.StringItems;
import marioandweegee3.toolbuilder.common.effect.Effects;
import marioandweegee3.toolbuilder.common.tools.HandleMaterials;
import marioandweegee3.toolbuilder.common.tools.HeadMaterials;
import marioandweegee3.toolbuilder.common.tools.tooltypes.Bow;
import marioandweegee3.toolbuilder.common.tools.tooltypes.ToolTypes;
import marioandweegee3.toolbuilder.util.MiscUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.condition.LootConditions;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ToolBuilder implements ModInitializer {
    public static final String modID = "toolbuilder";

    public static final Item obsidian_plate = new Item(new Item.Settings().group(ItemGroup.MATERIALS));

    public static final Style toolStyle = new Style().setColor(Formatting.GRAY);
    public static final Style effectStyle = new Style().setColor(Formatting.AQUA);
    public static final Style modifierStyle = new Style().setColor(Formatting.DARK_GREEN);

    public static final RegistryHelper HELPER = new RegistryHelper(modID);

    public static void debugDummy() {
        HELPER.log("DEBUG DUMMY METHOD CALLED - CHANGE CODE FOR RELEASE");
    }

    @Override
    public void onInitialize() {
        ConfigHandler.init();

        LootConditions.register(new BuiltToolLootConditionFactory());

        boolean cottonResourcesLoaded = FabricLoader.getInstance().isModLoaded("cotton-resources") || FabricLoader.getInstance().isModLoaded("techreborn");

        Groups.makeGroupSets();

        registerTorch(BlockTorches.stone_torch, BlockTorches.wall_stone_torch, "stone_torch", ItemGroup.DECORATIONS);

        HELPER.registerBlock("grip_station", new Block(FabricBlockSettings.copy(Blocks.SMITHING_TABLE).build()),
                ItemGroup.DECORATIONS);
        HELPER.registerBlock("mod_station", new Block(FabricBlockSettings.copy(Blocks.SMITHING_TABLE).build()),
                ItemGroup.DECORATIONS);

        TBData.getBlockLootTables().add(new BasicBlockLootTable(makeID("stone_torch")));

        TBData.getBlockLootTables().add(new BasicBlockLootTable(makeID("grip_station")));
        TBData.getBlockLootTables().add(new BasicBlockLootTable(makeID("mod_station")));

        HELPER.registerItem("blaze_string", StringItems.blazeString);
        HELPER.registerItem("ender_string", StringItems.enderString);

        HELPER.registerItem("obsidian_plate", obsidian_plate);

        HELPER.registerItem("slime_crystal", new Item(new Item.Settings().group(ItemGroup.MATERIALS)));
        HELPER.registerItem("ender_dust", new Item(new Item.Settings().group(ItemGroup.MATERIALS)));
        HELPER.registerItem("raw_heavy_plate", new Item(new Item.Settings().group(ItemGroup.MATERIALS)));

        HELPER.registerBlock("dense_obsidian",
                new Block(FabricBlockSettings.copy(Blocks.OBSIDIAN).strength(100, 2400).build()),
                ItemGroup.BUILDING_BLOCKS);
        HELPER.registerBlock("slime_crystal_block", new Block(FabricBlockSettings.of(Material.GLASS).build()),
                ItemGroup.BUILDING_BLOCKS);

        TBData.getBlockLootTables().add(new BasicBlockLootTable(makeID("slime_crystal_block")));
        TBData.getBlockLootTables().add(new BasicBlockLootTable(makeID("dense_obsidian")));

        HELPER.registerAllItems(MiscUtils.toMap(new Object[][] { { "poison_tip", new ModifierItem(Effects.POISONOUS) },
                { "holy_water", new HolyWaterItem() }, { "moss", new ModifierItem(Effects.GROWING) },
                { "blazing_stone", new ModifierItem(Effects.FLAMING) },
                { "heavy_plate", new ModifierItem(Effects.DURABLE) },
                { "magnet", new ModifierItem(Effects.MAGNETIC) } }));

        HELPER.registerAllItems(MiscUtils.toMap(new Object[][] { { "wood_handle", Handles.wood_handle },
                { "wood_gripped_handle", Handles.wood_gripped_handle }, { "stone_handle", Handles.stone_handle },
                { "stone_gripped_handle", Handles.stone_gripped_handle }, { "gold_handle", Handles.gold_handle },
                { "gold_gripped_handle", Handles.gold_gripped_handle }, { "bone_handle", Handles.bone_handle },
                { "bone_gripped_handle", Handles.bone_gripped_handle }, { "diamond_handle", Handles.diamond_handle },
                { "diamond_gripped_handle", Handles.diamond_gripped_handle } }));

        if (ConfigHandler.getInstance().shouldAddNetherCobaltLootTable()) {
            TBData.getBlockLootTables().add(new BasicBlockLootTable(new Identifier("c:cobalt_nether_ore")));
        }

        FabricLoader.getInstance().getEntrypoints("toolbuilder", TBInitializer.class).forEach(mod -> {
            List<String> enabledHeads = ConfigHandler.getInstance().enabledHeadMaterials();
            List<String> enabledHandles = ConfigHandler.getInstance().enabledHandleMaterials();
            for (HeadMaterial material : mod.headMaterials()) {
                if (enabledHeads.contains(material.getID().toString())) {
                    TBRegistries.HEAD_MATERIALS.put(material.getID(), material);
                }
            }
            for (HandleMaterial material : mod.handleMaterials()) {
                if (enabledHandles.contains(material.getID().toString())) {
                    TBRegistries.HANDLE_MATERIALS.put(material.getID(), material);
                }
            }
            for (StringMaterial material : mod.stringMaterials()) {
                TBRegistries.STRING_MATERIALS.put(material.getID(), material);
            }
            for (BuiltArmorMaterial material : mod.armorMaterials()) {
                if (enabledHeads.contains(material.getID().toString())) {
                    TBRegistries.ARMOR_MATERIALS.put(material.getID(), material);
                }
            }
            for (Effect effect : mod.effects()) {
                TBRegistries.EFFECTS.put(effect.getID(), effect);
            }
        });

        for (ToolType toolType : ToolTypes.values()) {
            for (HandleMaterial handle : TBRegistries.HANDLE_MATERIALS.values()) {
                for (HeadMaterial head : TBRegistries.HEAD_MATERIALS.values()) {
                    makeToolItem(handle, head, toolType, true);
                    makeToolItem(handle, head, toolType, false);
                }
            }
        }

        for (BuiltArmorMaterial material : TBRegistries.ARMOR_MATERIALS.values()) {
            makeArmorItems(material);
        }

        for (HandleMaterial material : TBRegistries.HANDLE_MATERIALS.values()) {
            for (StringMaterial string : TBRegistries.STRING_MATERIALS.values()) {
                makeBowItem(material, false, string);
                makeBowItem(material, true, string);
            }
            TBData.getHandleRecipes().add(new HandleRecipe(material));
        }

        LootTableLoadingCallback.EVENT.register((resourceManager, lootManager, id, supplier, setter) -> {
            if (id.equals(new Identifier("entities/wither_skeleton"))) {
                FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.builder()
                        .withRolls(ConstantLootTableRange.create(1))
                        .withEntry(ItemEntry.builder(Items.WITHER_SKELETON_SKULL))
                        .withCondition(new BuiltToolLootCondition(ToolTypes.GREATSWORD.getName(), 20));

                supplier.withPool(poolBuilder);
            }
        });

        if (ConfigHandler.getInstance().shouldAddSteelRecipe()) {
            TBData.getBlastingRecipes().put(makeID("steel_ingot"), recipe -> {
                recipe.ingredientTag(new Identifier("c:iron_plate"));
                recipe.experience(2);
                recipe.type(new Identifier("minecraft:blasting"));
                recipe.result(new Identifier("c:steel_ingot"));
            });
        }

        TBData.getBlastingRecipes().put(makeID("slime_crystal_blasting"), recipe -> {
            recipe.ingredientItem(new Identifier("slime_block"));
            recipe.experience(1);
            recipe.type(new Identifier("minecraft:blasting"));
            recipe.result(makeID("slime_crystal"));
        });

        TBData.getShapelessRecipes().put(makeID("slime_crystal_block"), recipe -> {
            for (int i = 0; i < 9; i++) {
                recipe.ingredientItem(makeID("slime_crystal"));
            }
            recipe.result(makeID("slime_crystal_block"), 1);
        });

        TBData.getShapelessRecipes().put(makeID("slime_crystal_from_block"), recipe -> {
            recipe.ingredientItem(makeID("slime_crystal_block"));
            recipe.result(makeID("slime_crystal"), 9);
        });

        TBData.getShapelessRecipes().put(makeID("holy_water"), recipe -> {
            recipe.ingredientItem(new Identifier("water_bucket"));
            recipe.ingredientItem(new Identifier("glass_bottle"));
            for (int i = 0; i < 3; i++) {
                if (cottonResourcesLoaded) {
                    recipe.ingredientTag(new Identifier("c:silver_ingot"));
                } else {
                    recipe.ingredientItem(new Identifier("iron_ingot"));
                }
            }
            recipe.result(makeID("holy_water"), 1);
        });

        TBData.getShapelessRecipes().put(makeID("poison_tip"), recipe -> {
            recipe.ingredientTag(new Identifier("logs"));
            recipe.ingredientItem(new Identifier("spider_eye"));
            recipe.result(makeID("poison_tip"), 1);
        });

        TBData.getShapelessRecipes().put(makeID("moss"), recipe -> {
            for (int i = 0; i < 9; i++) {
                recipe.ingredientItem(new Identifier("vine"));
            }
            recipe.result(makeID("moss"), 1);
        });

        TBData.getShapelessRecipes().put(makeID("blazing_stone"), recipe -> {
            recipe.ingredientItem(new Identifier("smooth_stone"));
            for (int i = 0; i < 6; i++) {
                recipe.ingredientItem(new Identifier("blaze_powder"));
            }
            recipe.result(makeID("blazing_stone"), 1);
        });

        TBData.getShapedRecipes().put(makeID("magnet"), recipe -> {
            recipe.pattern(
                "r l",
                "i i",
                "iii"
            );
            recipe.ingredientItem('r', new Identifier("redstone"));
            recipe.ingredientItem('l', new Identifier("lapis_lazuli"));
            recipe.ingredientItem('i', new Identifier("iron_ingot"));
            recipe.result(makeID("magnet"), 1);
        });

        TBData.getShapelessRecipes().put(makeID("raw_heavy_plate"), recipe -> {
            if (cottonResourcesLoaded) {
                recipe.ingredientTag(new Identifier("c:lead_plate"));
                recipe.ingredientTag(new Identifier("c:tungsten_plate"));
            } else {
                recipe.ingredientItem(makeID("obsidian_plate"));
                recipe.ingredientItem(makeID("obsidian_plate"));
            }
            recipe.ingredientItem(new Identifier("slime_ball"));
            recipe.ingredientItem(makeID("obsidian_plate"));
            recipe.result(makeID("raw_heavy_plate"), 1);
        });

        TBData.getBlastingRecipes().put(makeID("heavy_plate"), recipe -> {
            recipe.cookingTime(200);
            recipe.ingredientItem(makeID("raw_heavy_plate"));
            recipe.experience(1);
            recipe.type(new Identifier("minecraft:blasting"));
            recipe.result(makeID("heavy_plate"));
        });

        TBData.getShapedRecipes().put(makeID("obsidian_plate"), recipe -> {
            recipe.pattern("xx", "xx");
            recipe.ingredientItem('x', new Identifier("obsidian"));
            recipe.result(makeID("obsidian_plate"), 1);
        });

        TBData.getShapedRecipes().put(makeID("blaze_string"), recipe -> {
            recipe.pattern("bb", "ss");
            recipe.ingredientItem('b', new Identifier("blaze_powder"));
            recipe.ingredientItem('s', new Identifier("string"));
            recipe.result(makeID("blaze_string"), 2);
        });

        TBData.getBlastingRecipes().put(makeID("ender_dust"), recipe -> {
            recipe.ingredientItem(new Identifier("ender_pearl"));
            recipe.experience(3);
            recipe.type(new Identifier("minecraft:blasting"));
            recipe.result(makeID("ender_dust"));
        });

        TBData.getShapedRecipes().put(makeID("ender_string"), recipe -> {
            recipe.pattern("ee", "ss");
            recipe.ingredientItem('e', makeID("ender_dust"));
            recipe.ingredientItem('s', new Identifier("string"));
            recipe.result(makeID("ender_string"), 2);
        });

        TBData.getShapedRecipes().put(makeID("stone_torch"), recipe -> {
            recipe.pattern("c", "s");
            recipe.ingredientTag('c', new Identifier("coals"));
            recipe.ingredientItem('s', makeID("stone_handle"));
            recipe.result(makeID("stone_torch"), 4);
        });

        TBData.getShapedRecipes().put(makeID("dense_obsidian"), recipe -> {
            recipe.pattern("ppp", "ppp", "ppp");

            recipe.ingredientItem('p', makeID("obsidian_plate"));
            recipe.result(makeID("dense_obsidian"), 1);
        });

        TBData.getShapelessRecipes().put(makeID("stick_from_wood_handle"), recipe -> {
            recipe.ingredientItem(makeID("wood_handle"));
            recipe.result(new Identifier("stick"), 1);
        });

        TBData.getShapelessRecipes().put(makeID("wood_handle_from_stick"), recipe -> {
            recipe.ingredientItem(new Identifier("stick"));
            recipe.result(makeID("wood_handle"), 1);
        });

        for(HandleMaterial handle : TBRegistries.HANDLE_MATERIALS.values()) {
            TBData.getShapelessRecipes().put(makeID(handle.getName()+"_gripped"), recipe -> {
                recipe.group(makeID("gripped_handle"));

                recipe.multiIngredient(ing -> {
                    for(Item item : handle.getRepairItems(false)) {
                        ing.item(Registry.ITEM.getId(item));
                    }
                });
                recipe.ingredientItem(new Identifier("leather"));

                recipe.result(Registry.ITEM.getId(handle.getRepairItems(true).get(0)), 1);
            });
        }

        Artifice.registerData(makeID("recipes"), TBData::addRecipes);

        CommandRegistry.INSTANCE.register(false, dispatcher -> {
            LiteralCommandNode<ServerCommandSource> toolbuilderNode = CommandManager
            .literal("toolbuilder")
            .build();

            LiteralCommandNode<ServerCommandSource> tbNode = CommandManager
            .literal("tb")
            .build();

            LiteralCommandNode<ServerCommandSource> effectsNode = CommandManager
            .literal("effects")
            .build();

            LiteralCommandNode<ServerCommandSource> effectGetNode = CommandManager
            .literal("get")
            .executes(TBEffectCommand::get)
            .build();

            LiteralCommandNode<ServerCommandSource> effectAddNameNode = CommandManager
            .literal("add")
            .requires(source -> source.hasPermissionLevel(3))
            .build();

            LiteralCommandNode<ServerCommandSource> effectClearNode = CommandManager
            .literal("clear")
            .executes(TBEffectCommand::clear)
            .requires(source -> source.hasPermissionLevel(3))
            .build();

            ArgumentCommandNode<ServerCommandSource, Identifier> effectAddNode = CommandManager
            .argument("effect", IdentifierArgumentType.identifier())
            .suggests(Effects.effectSuggestions())
            .executes(TBEffectCommand::add)
            .requires(source -> source.hasPermissionLevel(3))
            .build();

            dispatcher.getRoot().addChild(toolbuilderNode);
            dispatcher.getRoot().addChild(tbNode);

            tbNode.addChild(effectsNode);
            toolbuilderNode.addChild(effectsNode);
            effectsNode.addChild(effectGetNode);
            effectsNode.addChild(effectAddNameNode);
            effectsNode.addChild(effectClearNode);
            effectAddNameNode.addChild(effectAddNode);

            LiteralCommandNode<ServerCommandSource> giveNode = CommandManager
            .literal("give")
            .requires(source -> source.hasPermissionLevel(3))
            .build();

            LiteralCommandNode<ServerCommandSource> giveArmorNode = CommandManager
            .literal("armor")
            .build();

            ArgumentCommandNode<ServerCommandSource, Identifier> armorMaterialNode = CommandManager
            .argument("armor", IdentifierArgumentType.identifier())
            .suggests(TBItemCommand.armorMaterialSuggestions())
            .build();

            ArgumentCommandNode<ServerCommandSource, EquipmentSlot> armorSlotNode = CommandManager
            .argument("slot", new EquipmentSlotArgumentType())
            .suggests(TBItemCommand.armorSlotSuggestions())
            .executes(TBItemCommand::giveArmor)
            .build();

            LiteralCommandNode<ServerCommandSource> giveToolNode = CommandManager
            .literal("tool")
            .build();

            ArgumentCommandNode<ServerCommandSource, Identifier> toolTypeNode = CommandManager
            .argument("tool", IdentifierArgumentType.identifier())
            .suggests(TBItemCommand.toolTypeSuggestions())
            .build();

            ArgumentCommandNode<ServerCommandSource, Identifier> toolHeadNode = CommandManager
            .argument("head", IdentifierArgumentType.identifier())
            .suggests(TBItemCommand.headMaterialSuggestions())
            .build();

            ArgumentCommandNode<ServerCommandSource, Identifier> toolHandleNode = CommandManager
            .argument("handle", IdentifierArgumentType.identifier())
            .suggests(TBItemCommand.handleMaterialSuggestions())
            .executes(TBItemCommand::giveTool)
            .build();

            ArgumentCommandNode<ServerCommandSource, Boolean> toolGripNode = CommandManager
            .argument("gripped", BoolArgumentType.bool())
            .executes(TBItemCommand::giveTool)
            .build();

            tbNode.addChild(giveNode);
            toolbuilderNode.addChild(giveNode);

            giveNode.addChild(giveToolNode);
            giveToolNode.addChild(toolTypeNode);
            toolTypeNode.addChild(toolHeadNode);
            toolHeadNode.addChild(toolHandleNode);
            toolHandleNode.addChild(toolGripNode);

            giveNode.addChild(giveArmorNode);
            giveArmorNode.addChild(armorMaterialNode);
            armorMaterialNode.addChild(armorSlotNode);
        });

        Groups.init();

        int effectCount = 0;
        HELPER.log("Registered these effects: ");

        for(Map.Entry<Identifier, Effect> entry : TBRegistries.EFFECTS.entrySet()){
            effectCount++;
            HELPER.log(entry.getKey().toString());
        }

        HELPER.log("Registered "+effectCount+" effects.");
    }

    public static void registerTorch(Torch block, WallTorch block2, String name, ItemGroup group){
        WallStandingBlockItem item = new WallStandingBlockItem(block, block2, new Item.Settings().group(group));
        Registry.register(Registry.ITEM, makeID(name), item);
        Registry.register(Registry.BLOCK, makeID(name), block);
        Registry.register(Registry.BLOCK, makeID("wall_"+name), block2);
    }

    public static void makeToolItem(HandleMaterial handle, HeadMaterial head, ToolType toolType, Boolean grip) {
        ToolItemBuilder builder = new ToolItemBuilder(handle, head, toolType, grip);
        Item tool = builder.build();
        TBData.getToolRecipes().add(new ToolRecipe(builder));
        HELPER.registerItem(builder.name, tool);
        Groups.addTo(tool, "tools");
        if(head == HeadMaterials.WOOD && handle == HandleMaterials.WOOD && !grip){
            FuelRegistry.INSTANCE.add(tool, 200);
        }
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ToolBuilderClient.addToolModel(handle, head, toolType, grip);
        }
    }

    public static void makeBowItem(HandleMaterial material, Boolean grip, StringMaterial string) {
        BowBuilder builder = new BowBuilder(material, grip, string);
        Item bow = builder.build();
        TBData.getBowRecipes().add(new BowRecipe(builder));
        HELPER.registerItem(builder.name, bow);
        Groups.addTo(bow, "tools");
        if(material == HandleMaterials.WOOD){
            int burnTime = 200;
            if(EffectInstance.toEffectSet(builder.material.getEffects()).contains(Effects.FLAMING)) burnTime += 100;
            FuelRegistry.INSTANCE.add(bow, burnTime);
        }
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ToolBuilderClient.addBowModel(material, grip, string);
        }
    }

    public static void makeArmorItems(BuiltArmorMaterial material){
        ArmorBuilder builder = new ArmorBuilder(material);
        builder.register();
    }

    public static Identifier makeID(String name){
        return HELPER.makeId(name);
    }

    public static class BowBuilder{
        private BowMaterial material;

        public String name;

        public BowBuilder(HandleMaterial material2, Boolean grip, StringMaterial string) {
            this.material = new BowMaterial(material2, grip, string);

            this.name = makeName(material);
        }

        public BowMaterial getMaterial(){
            return material;
        }

        public Item build(){
            return Bow.create(material);
        }

        private String makeName(BowMaterial material2) {
            String name = material2.getName() + "_bow";
            if(material.grip) name += "_gripped";
            return name;
        }
    }

    public static class ArmorBuilder{
        public BuiltArmorMaterial armorMaterial;

        public ArmorBuilder(BuiltArmorMaterial material){
            armorMaterial = material;
        }

        public String makeName(EquipmentSlot slot){
            return armorMaterial.getMaterialName()+"_"+getTypeString(slot);
        }

        public static String getTypeString(EquipmentSlot slot){
            String typeString = "";
            switch(slot){
                case FEET: typeString = "boots"; break;
                case LEGS: typeString = "leggings"; break;
                case CHEST: typeString = "chestplate"; break;
                case HEAD: typeString = "helmet"; break;
                case MAINHAND:
                case OFFHAND:
                    break;
            }
            return typeString;
        }

        public void register(){
            register(EquipmentSlot.FEET);
            register(EquipmentSlot.LEGS);
            register(EquipmentSlot.CHEST);
            register(EquipmentSlot.HEAD);
        }

        private void register(EquipmentSlot slot){
            TBData.getArmorRecipes().add(new ArmorRecipe(this, slot));
            Item item = build(slot);
            HELPER.registerItem(makeName(slot), item);
            Groups.addTo(item, "armor");
            if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ToolBuilderClient.addArmorModel(this, slot);
            }
        }

        public Item build(EquipmentSlot slot){
            return new BuiltArmorItem(armorMaterial, slot, new Item.Settings());
        }
    }
    
    public static class ToolItemBuilder{
        private BuiltToolMaterial material;
        private ToolType toolType;

        public String name;

        public ToolItemBuilder(HandleMaterial handle, HeadMaterial head, ToolType toolType2, Boolean grip) {
            this.toolType = toolType2;
            this.name = makeName(head, handle, grip);
            this.material = BuiltToolMaterial.of(handle, head, name, grip);
        }

        public Item build(){
            return toolType.getBuilder().build(material);
        }

        public BuiltToolMaterial getMaterial(){
            return material;
        }

        public ToolType getType(){
            return toolType;
        }

        private String makeName(HeadMaterial head, HandleMaterial handle) {
            String nameString = toolType.getName();
            nameString += "_" + head.getName();
            nameString += "_" + handle.getName();
            return nameString;
        }

        private String makeName(HeadMaterial head, HandleMaterial handle, Boolean grip) {
            if (grip)
                return makeName(head, handle) + "_gripped";
            else
                return makeName(head, handle);
        }
    }
}