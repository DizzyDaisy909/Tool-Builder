package marioandweegee3.toolbuilder.common.effect;

import java.util.List;

import marioandweegee3.toolbuilder.common.config.ConfigHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MagneticEffect{
    public static void run(LivingEntity miner, World world, int level){
        //Based on code from Tinkers Construct
        double x = miner.getX();
        double y = miner.getY();
        double z = miner.getZ();

        double range = ConfigHandler.getInstance().getMagneticRange() * level;

        List<ItemEntity> items = world.getEntities(ItemEntity.class, new Box(x-range, y-range, z-range, x+range, y+range, z+range), null);

        int pulled = 0;

        for(ItemEntity item : items){
            if(item.getStack().isEmpty() || !item.isAlive()){
                continue;
            }

            if(pulled > 200){
                break;
            }

            float strength = 0.2f;

            Vec3d vec = new Vec3d(x, y, z);
            vec = vec.subtract(new Vec3d(item.getX(), item.getY(), item.getZ()));

            vec = vec.normalize();
            vec = vec.multiply(strength);

            item.addVelocity(vec.x, vec.y, vec.z);

            pulled++;
        }
    }

    public static void writeTicks(int ticks, CompoundTag tag){
        tag.putInt(Effects.magneticTickNBTtag, ticks);
    }

    public static int readTicks(CompoundTag tag){
        if(tag.contains(Effects.magneticTickNBTtag)){
            return tag.getInt(Effects.magneticTickNBTtag);
        }
        return 0;
    }
}