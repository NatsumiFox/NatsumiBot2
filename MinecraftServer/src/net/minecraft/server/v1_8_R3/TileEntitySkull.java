package net.minecraft.server.v1_8_R3;

import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TileEntitySkull extends TileEntity {

    private int a;
    private int rotation;
    private GameProfile g = null;
    public static final Executor executor = Executors.newFixedThreadPool(3, (new ThreadFactoryBuilder()).setNameFormat("Head Conversion Thread - %1$d").build());
    public static final LoadingCache<String, GameProfile> skinCache = CacheBuilder.newBuilder().maximumSize(5000L).expireAfterAccess(60L, TimeUnit.MINUTES).build(new CacheLoader() {
        public GameProfile load(String s) throws Exception {
            final GameProfile[] agameprofile = new GameProfile[1];
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
                public void onProfileLookupSucceeded(GameProfile gameprofile) {
                    agameprofile[0] = gameprofile;
                }

                public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                    agameprofile[0] = gameprofile;
                }
            };

            MinecraftServer.getServer().getGameProfileRepository().findProfilesByNames(new String[] { s}, Agent.MINECRAFT, profilelookupcallback);
            GameProfile gameprofile = agameprofile[0];

            if (gameprofile == null) {
                UUID uuid = EntityHuman.a(new GameProfile((UUID) null, s));

                gameprofile = new GameProfile(uuid, s);
                profilelookupcallback.onProfileLookupSucceeded(gameprofile);
            } else {
                Property property = (Property) Iterables.getFirst(gameprofile.getProperties().get("textures"), (Object) null);

                if (property == null) {
                    gameprofile = MinecraftServer.getServer().aD().fillProfileProperties(gameprofile, true);
                }
            }

            return gameprofile;
        }

        public Object load(Object object) throws Exception {
            return this.load((String) object);
        }
    });

    public TileEntitySkull() {}

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setByte("SkullType", (byte) (this.a & 255));
        nbttagcompound.setByte("Rot", (byte) (this.rotation & 255));
        if (this.g != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            GameProfileSerializer.serialize(nbttagcompound1, this.g);
            nbttagcompound.set("Owner", nbttagcompound1);
        }

    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.a = nbttagcompound.getByte("SkullType");
        this.rotation = nbttagcompound.getByte("Rot");
        if (this.a == 3) {
            if (nbttagcompound.hasKeyOfType("Owner", 10)) {
                this.g = GameProfileSerializer.deserialize(nbttagcompound.getCompound("Owner"));
            } else if (nbttagcompound.hasKeyOfType("ExtraType", 8)) {
                String s = nbttagcompound.getString("ExtraType");

                if (!UtilColor.b(s)) {
                    this.g = new GameProfile((UUID) null, s);
                    this.e();
                }
            }
        }

    }

    public GameProfile getGameProfile() {
        return this.g;
    }

    public Packet getUpdatePacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        this.b(nbttagcompound);
        return new PacketPlayOutTileEntityData(this.position, 4, nbttagcompound);
    }

    public void setSkullType(int i) {
        this.a = i;
        this.g = null;
    }

    public void setGameProfile(GameProfile gameprofile) {
        this.a = 3;
        this.g = gameprofile;
        this.e();
    }

    private void e() {
        GameProfile gameprofile = this.g;

        this.setSkullType(0);
        b(gameprofile, new Predicate() {
            public boolean apply(GameProfile gameprofile) {
                TileEntitySkull.this.setSkullType(3);
                TileEntitySkull.this.g = gameprofile;
                TileEntitySkull.this.update();
                if (TileEntitySkull.this.world != null) {
                    TileEntitySkull.this.world.notify(TileEntitySkull.this.position);
                }

                return false;
            }

            public boolean apply(Object object) {
                return this.apply((GameProfile) object);
            }
        });
    }

    public static void b(final GameProfile gameprofile, final Predicate<GameProfile> predicate) {
        if (gameprofile != null && !UtilColor.b(gameprofile.getName())) {
            if (gameprofile.isComplete() && gameprofile.getProperties().containsKey("textures")) {
                predicate.apply(gameprofile);
            } else if (MinecraftServer.getServer() == null) {
                predicate.apply(gameprofile);
            } else {
                GameProfile gameprofile1 = (GameProfile) TileEntitySkull.skinCache.getIfPresent(gameprofile.getName());

                if (gameprofile1 != null && Iterables.getFirst(gameprofile1.getProperties().get("textures"), (Object) null) != null) {
                    predicate.apply(gameprofile1);
                } else {
                    TileEntitySkull.executor.execute(new Runnable() {
                        public void run() {
                            final GameProfile gameprofile = (GameProfile) TileEntitySkull.skinCache.getUnchecked(gameprofile1.getName().toLowerCase());

                            MinecraftServer.getServer().processQueue.add(new Runnable() {
                                public void run() {
                                    if (gameprofile == null) {
                                        predicate.apply(gameprofile1);
                                    } else {
                                        predicate.apply(gameprofile);
                                    }

                                }
                            });
                        }
                    });
                }
            }
        } else {
            predicate.apply(gameprofile);
        }

    }

    public int getSkullType() {
        return this.a;
    }

    public void setRotation(int i) {
        this.rotation = i;
    }

    public int getRotation() {
        return this.rotation;
    }
}
