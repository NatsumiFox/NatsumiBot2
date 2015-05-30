package net.minecraft.server.v1_8_R3;

import org.bukkit.World.Environment;
import org.bukkit.generator.ChunkGenerator;

public class SecondaryWorldServer extends WorldServer {

    private WorldServer a;

    public SecondaryWorldServer(MinecraftServer minecraftserver, IDataManager idatamanager, int i, WorldServer worldserver, MethodProfiler methodprofiler, WorldData worlddata, Environment environment, ChunkGenerator chunkgenerator) {
        super(minecraftserver, idatamanager, worlddata, i, methodprofiler, environment, chunkgenerator);
        this.a = worldserver;
    }

    public World b() {
        this.worldMaps = this.a.T();
        String s = PersistentVillage.a(this.worldProvider);
        PersistentVillage persistentvillage = (PersistentVillage) this.worldMaps.get(PersistentVillage.class, s);

        if (persistentvillage == null) {
            this.villages = new PersistentVillage(this);
            this.worldMaps.a(s, this.villages);
        } else {
            this.villages = persistentvillage;
            this.villages.a((World) this);
        }

        return super.b();
    }
}
