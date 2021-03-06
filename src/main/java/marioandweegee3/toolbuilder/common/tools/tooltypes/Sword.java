package marioandweegee3.toolbuilder.common.tools.tooltypes;

import java.util.List;

import marioandweegee3.toolbuilder.ToolBuilder;
import marioandweegee3.toolbuilder.api.BuiltTool;
import marioandweegee3.toolbuilder.api.material.*;
import marioandweegee3.toolbuilder.common.itemgroups.Groups;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class Sword extends SwordItem implements BuiltTool{
    private static float speed = ToolValues.SWORD.getSpeed();
    private static float damage = ToolValues.SWORD.getDamage();

    private BuiltToolMaterial material;

    private Sword(BuiltToolMaterial material, Item.Settings settings){
        super(material, (int) damage, BuiltTool.getAttackSpeed(material, speed), settings);
        this.material = material;
    }

    public static Item create(BuiltToolMaterial material){
        return new Sword(material, new Item.Settings());
    }

    @Override
    public String getType() {
        return "sword";
    }

    @Override
    public BuiltToolMaterial getMaterial() {
        return material;
    }

    @Override
    public String getTranslationKey() {
        return getTranslationName(material);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        addTooltips(tooltip, stack, material, ToolBuilder.toolStyle, ToolBuilder.effectStyle, ToolBuilder.modifierStyle);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        onHit(stack, target, attacker);
        return super.postHit(stack, target, attacker);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        onMine(material, stack, state, world, pos, miner, this::shouldDropXp);

        return super.postMine(stack, world, state, pos, miner);
    }

    public boolean shouldDropXp(BlockState state, ItemStack stack){
        return getMiningSpeed(stack, state) == 15;
    }
}