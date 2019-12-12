package marioandweegee3.toolbuilder.common.data.recipes;

import com.swordglowsblue.artifice.api.ArtificeResourcePack.ServerResourcePackBuilder;

import marioandweegee3.toolbuilder.ToolBuilder;
import marioandweegee3.toolbuilder.api.ToolType;
import marioandweegee3.toolbuilder.api.item.BigTool;
import marioandweegee3.toolbuilder.api.material.BuiltToolMaterial;
import marioandweegee3.toolbuilder.api.material.HandleMaterial;
import marioandweegee3.toolbuilder.api.material.HeadMaterial;
import marioandweegee3.toolbuilder.common.config.ConfigHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ToolRecipe {
    private ToolBuilder.Builder builder;
    public ToolRecipe(ToolBuilder.Builder builder){
        this.builder = builder;
    }

    public void add(ServerResourcePackBuilder pack){
        BuiltToolMaterial material = builder.getMaterial();
        ToolType type = builder.getType();
        HeadMaterial head = material.head;
        HandleMaterial handle = material.handle;

        pack.addShapedRecipe(ToolBuilder.makeID(builder.name), recipe -> {
            recipe.pattern(type.getRecipePattern());

            if(head.getRepairString().startsWith("#")){
                Identifier id = new Identifier(head.getRepairString().substring(1));
                recipe.ingredientTag('x', id);
            } else {
                Identifier id = new Identifier(head.getRepairString());
                recipe.ingredientItem('x', id);
            }

            if(handle.getRepairItems(material.isGripped).contains(Items.STICK)){
                if(ConfigHandler.INSTANCE.canCraftWithSticks()){
                    recipe.multiIngredient('y', ing -> {
                        ing.item(new Identifier("stick"));
                        for(Item item : handle.getRepairItems(material.isGripped)){
                            if(item != Items.STICK){
                                ing.item(Registry.ITEM.getId(item));
                            }
                        }
                    });
                } else {
                    recipe.multiIngredient('y', ing -> {
                        for(Item item : handle.getRepairItems(material.isGripped)){
                            if(item != Items.STICK){
                                ing.item(Registry.ITEM.getId(item));
                            }
                        }
                    });
                }
            } else {
                recipe.multiIngredient('y', ing -> {
                    for(Item item : handle.getRepairItems(material.isGripped)){
                        ing.item(Registry.ITEM.getId(item));
                    }
                });
            }

            Item tool = builder.build();
            if(tool instanceof BigTool){
                if(head.getBlockString().startsWith("#")){
                    Identifier id = new Identifier(head.getBlockString().substring(1));
                    recipe.ingredientTag('z', id);
                } else {
                    Identifier id = new Identifier(head.getBlockString());
                    recipe.ingredientItem('z', id);
                }
            }

            recipe.result(ToolBuilder.makeID(builder.name), 1);
        });
    }
}