package com.example;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
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

    // ① モブを右クリック：乗っかる ＆ 視点切り替え
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!user.getWorld().isClient()) {
            user.startRiding(entity, true);
        } else {
            MinecraftClient.getInstance().options.setPerspective(Perspective.THIRD_PERSON_BACK);
        }
        return ActionResult.SUCCESS;
    }

    // ② 乗ったまま殴った（攻撃した）時の効果（ウィザースケルトン＆アイアンゴーレム）
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player && player.hasVehicle()) {
            Entity vehicle = player.getVehicle();
            
            // ウィザースケルトン：殴ると「衰弱」を付与
            if (vehicle.getType() == EntityType.WITHER_SKELETON) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 200, 1));
            }
            // アイアンゴーレム：敵を空高く吹き飛ばす
            else if (vehicle.getType() == EntityType.IRON_GOLEM) {
                target.setVelocity(target.getVelocity().x * 2.5, 1.2, target.getVelocity().z * 2.5);
                target.velocityModified = true;
            }
        }
        return super.postHit(stack, target, attacker);
    }

    // ③ 空中で右クリック：固有の射撃＆爆発能力を発動！
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient() && user.hasVehicle()) {
            Entity vehicle = user.getVehicle();
            Vec3d look = user.getRotationVector();

            // 1. クリーパー：爆発（プレイヤー＆クリーパーは無傷）
            if (vehicle.getType() == EntityType.CREEPER) {
                world.createExplosion(null, vehicle.getX(), vehicle.getY(), vehicle.getZ(), 3.5F, ExplosionSourceType.NONE);
            }
            // 2. スノーゴーレム：雪玉を連射
            else if (vehicle.getType() == EntityType.SNOW_GOLEM) {
                SnowballEntity snowball = new SnowballEntity(world, user);
                snowball.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 2.0F, 1.0F);
                world.spawnEntity(snowball);
            }
            // 3. スケルトン系：色違い・亜種に応じた矢を発射
            else if (vehicle.getType() == EntityType.SKELETON) {
                spawnArrow(world, user, null);
            } else if (vehicle.getType() == EntityType.STRAY) { // ストレイ：鈍化の矢
                spawnArrow(world, user, Potions.SLOWNESS);
            } else if (vehicle.getType() == EntityType.BOGGED) { // ボグド：毒の矢
                spawnArrow(world, user, Potions.POISON);
            }
            // 4. ブレイズ：小さな火の玉（火炎弾）
            else if (vehicle.getType() == EntityType.BLAZE) {
                SmallFireballEntity fireball = new SmallFireballEntity(world, user, look.x, look.y, look.z);
                fireball.setPosition(user.getX() + look.x, user.getY() + 1.2, user.getZ() + look.z);
                world.spawnEntity(fireball);
            }
            // 5. ガスト：大きな火の玉（爆発）
            else if (vehicle.getType() == EntityType.GHAST) {
                FireballEntity fireball = new FireballEntity(world, user, look.x, look.y, look.z, 1);
                fireball.setPosition(user.getX() + look.x * 2, user.getY() + 1.5, user.getZ() + look.z * 2);
                world.spawnEntity(fireball);
            }
            // 6. ブリーズ：ウィンドチャージ
            else if (vehicle.getType() == EntityType.BREEZE) {
                BreezeWindChargeEntity windCharge = new BreezeWindChargeEntity(world, user);
                windCharge.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 2.5F, 1.0F);
                world.spawnEntity(windCharge);
            }
            // 7. ウィザー：ウィザースカル（爆発する頭蓋骨）を発射！
            else if (vehicle.getType() == EntityType.WITHER) {
                WitherSkullEntity skull = new WitherSkullEntity(world, user, look.x, look.y, look.z);
                skull.setPosition(user.getX() + look.x * 2, user.getY() + 1.5, user.getZ() + look.z * 2);
                world.spawnEntity(skull);
            }

            return TypedActionResult.success(itemStack);
        }

        return TypedActionResult.pass(itemStack);
    }

    // 矢を生成するヘルパーメソッド
    private void spawnArrow(World world, PlayerEntity user, net.minecraft.potion.Potion potion) {
        ArrowEntity arrow = new ArrowEntity(world, user);
        if (potion != null) {
            arrow.initFromStack(PotionUtil.setPotion(new ItemStack(Items.TIPPED_ARROW), potion));
        }
        arrow.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 3.0F, 1.0F);
        world.spawnEntity(arrow);
    }

    // ④ 移動操作（プレイヤーのWASD操作に合わせてモブが進む）
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient() && entity instanceof PlayerEntity player && player.hasVehicle()) {
            Entity vehicle = player.getVehicle();
            if (player.forwardSpeed > 0) {
                Vec3d look = player.getRotationVector();
                vehicle.setVelocity(look.x * 0.6, vehicle.getVelocity().y, look.z * 0.6);
                vehicle.velocityModified = true;
            }
        }
    }
}
