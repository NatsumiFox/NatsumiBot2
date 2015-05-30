package net.minecraft.server.v1_8_R3;

import java.util.Iterator;
import java.util.List;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;

public class EntityLightning extends EntityWeather {

    private int lifeTicks;
    public long a;
    private int c;
    public boolean isEffect;
    public boolean isSilent;

    public EntityLightning(World world, double d0, double d1, double d2) {
        this(world, d0, d1, d2, false);
    }

    public EntityLightning(World world, double d0, double d1, double d2, boolean flag) {
        super(world);
        this.isEffect = false;
        this.isSilent = false;
        this.isEffect = flag;
        this.setPositionRotation(d0, d1, d2, 0.0F, 0.0F);
        this.lifeTicks = 2;
        this.a = this.random.nextLong();
        this.c = this.random.nextInt(3) + 1;
        BlockPosition blockposition = new BlockPosition(this);

        if (!flag && !world.isClientSide && world.getGameRules().getBoolean("doFireTick") && (world.getDifficulty() == EnumDifficulty.NORMAL || world.getDifficulty() == EnumDifficulty.HARD) && world.areChunksLoaded(blockposition, 10)) {
            if (world.getType(blockposition).getBlock().getMaterial() == Material.AIR && Blocks.FIRE.canPlace(world, blockposition) && !CraftEventFactory.callBlockIgniteEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this).isCancelled()) {
                world.setTypeUpdate(blockposition, Blocks.FIRE.getBlockData());
            }

            for (int i = 0; i < 4; ++i) {
                BlockPosition blockposition1 = blockposition.a(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);

                if (world.getType(blockposition1).getBlock().getMaterial() == Material.AIR && Blocks.FIRE.canPlace(world, blockposition1) && !CraftEventFactory.callBlockIgniteEvent(world, blockposition1.getX(), blockposition1.getY(), blockposition1.getZ(), this).isCancelled()) {
                    world.setTypeUpdate(blockposition1, Blocks.FIRE.getBlockData());
                }
            }
        }

    }

    public EntityLightning(World world, double d0, double d1, double d2, boolean flag, boolean flag1) {
        this(world, d0, d1, d2, flag);
        this.isSilent = flag1;
    }

    public void t_() {
        super.t_();
        if (!this.isSilent && this.lifeTicks == 2) {
            float f = 0.8F + this.random.nextFloat() * 0.2F;
            int i = ((WorldServer) this.world).getServer().getViewDistance() * 16;
            Iterator iterator = this.world.players.iterator();

            while (iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator.next();
                double d0 = this.locX - entityplayer.locX;
                double d1 = this.locZ - entityplayer.locZ;
                double d2 = d0 * d0 + d1 * d1;

                if (d2 > (double) (i * i)) {
                    double d3 = Math.sqrt(d2);
                    double d4 = entityplayer.locX + d0 / d3 * (double) i;
                    double d5 = entityplayer.locZ + d1 / d3 * (double) i;

                    entityplayer.playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect("ambient.weather.thunder", d4, this.locY, d5, 10000.0F, f));
                } else {
                    entityplayer.playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect("ambient.weather.thunder", this.locX, this.locY, this.locZ, 10000.0F, f));
                }
            }

            this.world.makeSound(this.locX, this.locY, this.locZ, "random.explode", 2.0F, 0.5F + this.random.nextFloat() * 0.2F);
        }

        --this.lifeTicks;
        if (this.lifeTicks < 0) {
            if (this.c == 0) {
                this.die();
            } else if (this.lifeTicks < -this.random.nextInt(10)) {
                --this.c;
                this.lifeTicks = 1;
                this.a = this.random.nextLong();
                BlockPosition blockposition = new BlockPosition(this);

                if (!this.world.isClientSide && this.world.getGameRules().getBoolean("doFireTick") && this.world.areChunksLoaded(blockposition, 10) && this.world.getType(blockposition).getBlock().getMaterial() == Material.AIR && Blocks.FIRE.canPlace(this.world, blockposition) && !this.isEffect && !CraftEventFactory.callBlockIgniteEvent(this.world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this).isCancelled()) {
                    this.world.setTypeUpdate(blockposition, Blocks.FIRE.getBlockData());
                }
            }
        }

        if (this.lifeTicks >= 0 && !this.isEffect) {
            if (this.world.isClientSide) {
                this.world.d(2);
            } else {
                double d6 = 3.0D;
                List list = this.world.getEntities(this, new AxisAlignedBB(this.locX - d6, this.locY - d6, this.locZ - d6, this.locX + d6, this.locY + 6.0D + d6, this.locZ + d6));

                for (int j = 0; j < list.size(); ++j) {
                    Entity entity = (Entity) list.get(j);

                    entity.onLightningStrike(this);
                }
            }
        }

    }

    protected void h() {}

    protected void a(NBTTagCompound nbttagcompound) {}

    protected void b(NBTTagCompound nbttagcompound) {}
}
