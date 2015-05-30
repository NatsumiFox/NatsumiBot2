package net.minecraft.server.v1_8_R3;

import com.google.common.base.Predicate;

public class PathfinderGoalRandomTargetNonTamed<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget {

    private EntityTameableAnimal g;

    public PathfinderGoalRandomTargetNonTamed(EntityTameableAnimal entitytameableanimal, Class<T> oclass, boolean flag, Predicate<? super T> predicate) {
        super(entitytameableanimal, oclass, 10, flag, false, predicate);
        this.g = entitytameableanimal;
    }

    public boolean a() {
        return !this.g.isTamed() && super.a();
    }
}
