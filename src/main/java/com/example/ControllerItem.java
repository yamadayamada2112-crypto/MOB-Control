package com.example;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;

public class ControllerItem extends Item {
    public ControllerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!user.getWorld().isClient()) {
            user.startRiding(entity, true);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (!world.isClient()) {
            Entity vehicle = user.getVehicle();
            if (vehicle != null) {
                Vec3d look = user.getRotationVector();
                String entityType = vehicle.getType().toString();

                if (entityType.contains("ghast") || entityType.contains("blaze")) {
                    FireballEntity fireball = new FireballEntity(world, user, look.x, look.y, look.z, 1);
                    fireball.setPosition(user.getX() + look.x * 2, user.getY() + 1.5, user.getZ() + look.z * 2);
                    world.spawnEntity(fireball);
                } else if (entityType.contains("wither")) {
                    WitherSkullEntity skull = new WitherSkullEntity(world, user, look.x, look.y, look.z);
                    skull.setPosition(user.getX() + look.x * 2, user.getY() + 1.5, user.getZ() + look.z * 2);
                    world.spawnEntity(skull);
                } else if (entityType.contains("creeper")) {
                    world.createExplosion(vehicle, vehicle.getX(), vehicle.getY(), vehicle.getZ(), 3.0f, ExplosionSourceType.MOB);
                } else {
                    vehicle.setVelocity(look.x * 1.5, 0.8, look.z * 1.5);
                    vehicle.velocityModified = true;
                }

                return TypedActionResult.success(itemStack);
            }
        }
        return TypedActionResult.pass(itemStack);
    }
}
