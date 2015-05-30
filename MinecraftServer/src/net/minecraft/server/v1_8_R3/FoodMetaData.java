package net.minecraft.server.v1_8_R3;

import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class FoodMetaData {

    public int foodLevel = 20;
    public float saturationLevel = 5.0F;
    public float exhaustionLevel;
    private int foodTickTimer;
    private EntityHuman entityhuman;
    private int e = 20;

    public FoodMetaData() {
        throw new AssertionError("Whoopsie, we missed the bukkit.");
    }

    public FoodMetaData(EntityHuman entityhuman) {
        Validate.notNull(entityhuman);
        this.entityhuman = entityhuman;
    }

    public void eat(int i, float f) {
        this.foodLevel = Math.min(i + this.foodLevel, 20);
        this.saturationLevel = Math.min(this.saturationLevel + (float) i * f * 2.0F, (float) this.foodLevel);
    }

    public void a(ItemFood itemfood, ItemStack itemstack) {
        int i = this.foodLevel;
        FoodLevelChangeEvent foodlevelchangeevent = CraftEventFactory.callFoodLevelChangeEvent(this.entityhuman, itemfood.getNutrition(itemstack) + i);

        if (!foodlevelchangeevent.isCancelled()) {
            this.eat(foodlevelchangeevent.getFoodLevel() - i, itemfood.getSaturationModifier(itemstack));
        }

        ((EntityPlayer) this.entityhuman).playerConnection.sendPacket(new PacketPlayOutUpdateHealth(((EntityPlayer) this.entityhuman).getBukkitEntity().getScaledHealth(), this.entityhuman.getFoodData().foodLevel, this.entityhuman.getFoodData().saturationLevel));
    }

    public void a(EntityHuman entityhuman) {
        EnumDifficulty enumdifficulty = entityhuman.world.getDifficulty();

        this.e = this.foodLevel;
        if (this.exhaustionLevel > 4.0F) {
            this.exhaustionLevel -= 4.0F;
            if (this.saturationLevel > 0.0F) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
            } else if (enumdifficulty != EnumDifficulty.PEACEFUL) {
                FoodLevelChangeEvent foodlevelchangeevent = CraftEventFactory.callFoodLevelChangeEvent(entityhuman, Math.max(this.foodLevel - 1, 0));

                if (!foodlevelchangeevent.isCancelled()) {
                    this.foodLevel = foodlevelchangeevent.getFoodLevel();
                }

                ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutUpdateHealth(((EntityPlayer) entityhuman).getBukkitEntity().getScaledHealth(), this.foodLevel, this.saturationLevel));
            }
        }

        if (entityhuman.world.getGameRules().getBoolean("naturalRegeneration") && this.foodLevel >= 18 && entityhuman.cm()) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
                entityhuman.heal(1.0F, RegainReason.SATIATED);
                this.a(entityhuman.world.spigotConfig.regenExhaustion);
                this.foodTickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
                if (entityhuman.getHealth() > 10.0F || enumdifficulty == EnumDifficulty.HARD || entityhuman.getHealth() > 1.0F && enumdifficulty == EnumDifficulty.NORMAL) {
                    entityhuman.damageEntity(DamageSource.STARVE, 1.0F);
                }

                this.foodTickTimer = 0;
            }
        } else {
            this.foodTickTimer = 0;
        }

    }

    public void a(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.hasKeyOfType("foodLevel", 99)) {
            this.foodLevel = nbttagcompound.getInt("foodLevel");
            this.foodTickTimer = nbttagcompound.getInt("foodTickTimer");
            this.saturationLevel = nbttagcompound.getFloat("foodSaturationLevel");
            this.exhaustionLevel = nbttagcompound.getFloat("foodExhaustionLevel");
        }

    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInt("foodLevel", this.foodLevel);
        nbttagcompound.setInt("foodTickTimer", this.foodTickTimer);
        nbttagcompound.setFloat("foodSaturationLevel", this.saturationLevel);
        nbttagcompound.setFloat("foodExhaustionLevel", this.exhaustionLevel);
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public boolean c() {
        return this.foodLevel < 20;
    }

    public void a(float f) {
        this.exhaustionLevel = Math.min(this.exhaustionLevel + f, 40.0F);
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    public void a(int i) {
        this.foodLevel = i;
    }
}
