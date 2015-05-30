package net.minecraft.server.v1_8_R3;

public class PathfinderGoalSit extends PathfinderGoal {

    private EntityTameableAnimal entity;
    private boolean willSit;

    public PathfinderGoalSit(EntityTameableAnimal entitytameableanimal) {
        this.entity = entitytameableanimal;
        this.a(5);
    }

    public boolean a() {
        if (!this.entity.isTamed()) {
            return this.willSit && this.entity.getGoalTarget() == null;
        } else if (this.entity.V()) {
            return false;
        } else if (!this.entity.onGround) {
            return false;
        } else {
            EntityLiving entityliving = this.entity.getOwner();

            return entityliving == null ? true : (this.entity.h(entityliving) < 144.0D && entityliving.getLastDamager() != null ? false : this.willSit);
        }
    }

    public void c() {
        this.entity.getNavigation().n();
        this.entity.setSitting(true);
    }

    public void d() {
        this.entity.setSitting(false);
    }

    public void setSitting(boolean flag) {
        this.willSit = flag;
    }
}
