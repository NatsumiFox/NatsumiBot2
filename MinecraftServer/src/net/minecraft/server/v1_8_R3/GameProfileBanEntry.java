package net.minecraft.server.v1_8_R3;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.UUID;

public class GameProfileBanEntry extends ExpirableListEntry<GameProfile> {

    public GameProfileBanEntry(GameProfile gameprofile) {
        this(gameprofile, (Date) null, (String) null, (Date) null, (String) null);
    }

    public GameProfileBanEntry(GameProfile gameprofile, Date date, String s, Date date1, String s1) {
        super(gameprofile, date, s, date1, s1);
    }

    public GameProfileBanEntry(JsonObject jsonobject) {
        super(b(jsonobject), jsonobject);
    }

    protected void a(JsonObject jsonobject) {
        if (this.getKey() != null) {
            jsonobject.addProperty("uuid", ((GameProfile) this.getKey()).getId() == null ? "" : ((GameProfile) this.getKey()).getId().toString());
            jsonobject.addProperty("name", ((GameProfile) this.getKey()).getName());
            super.a(jsonobject);
        }

    }

    private static GameProfile b(JsonObject jsonobject) {
        UUID uuid = null;
        String s = null;

        if (jsonobject.has("uuid")) {
            String s1 = jsonobject.get("uuid").getAsString();

            try {
                uuid = UUID.fromString(s1);
            } catch (Throwable throwable) {
                ;
            }
        }

        if (jsonobject.has("name")) {
            s = jsonobject.get("name").getAsString();
        }

        return uuid == null && s == null ? null : new GameProfile(uuid, s);
    }
}
