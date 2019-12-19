package marioandweegee3.toolbuilder.api.material;

import java.util.HashSet;
import java.util.Set;

import marioandweegee3.toolbuilder.api.effect.EffectInstance;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public interface HeadMaterial extends ToolMaterial{
    public static HeadMaterial copy(ToolMaterial base, String name) {
        return new HeadMaterial() {

            @Override
            public int getDurability() {
                return base.getDurability();
            }

            @Override
            public float getMiningSpeed() {
                return base.getMiningSpeed();
            }

            @Override
            public float getAttackDamage() {
                return base.getAttackDamage();
            }

            @Override
            public int getMiningLevel() {
                return base.getMiningLevel();
            }

            @Override
            public int getEnchantability() {
                return base.getEnchantability();
            }

            @Override
            public Ingredient getRepairIngredient() {
                return base.getRepairIngredient();
			}

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Set<EffectInstance> getEffects() {
                return new HashSet<>();
            }

            @Override
            public String getRepairString() {
                return "";
            }

            @Override
            public String getBlockString() {
                return "";
            }

            @Override
            public boolean isCotton() {
                return false;
            }

            @Override
            public String getMod() {
                return "";
            }

        };
    }

    public Set<EffectInstance> getEffects();
    public String getName();
    public String getRepairString();
    public String getBlockString();
    public boolean isCotton();
    public String getMod();
}