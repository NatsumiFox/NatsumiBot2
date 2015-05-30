package net.minecraft.server.v1_8_R3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.libs.joptsimple.OptionSet;

public class PropertyManager {

    private static final Logger a = LogManager.getLogger();
    public final Properties properties;
    private final File file;
    private OptionSet options;

    public PropertyManager(File file) {
        this.properties = new Properties();
        this.options = null;
        this.file = file;
        if (file.exists()) {
            FileInputStream fileinputstream = null;

            try {
                fileinputstream = new FileInputStream(file);
                this.properties.load(fileinputstream);
            } catch (Exception exception) {
                PropertyManager.a.warn("Failed to load " + file, exception);
                this.a();
            } finally {
                if (fileinputstream != null) {
                    try {
                        fileinputstream.close();
                    } catch (IOException ioexception) {
                        ;
                    }
                }

            }
        } else {
            PropertyManager.a.warn(file + " does not exist");
            this.a();
        }

    }

    public PropertyManager(OptionSet optionset) {
        this((File) optionset.valueOf("config"));
        this.options = optionset;
    }

    private <T> T getOverride(String s, T t0) {
        return this.options != null && this.options.has(s) && !s.equals("online-mode") ? this.options.valueOf(s) : t0;
    }

    public void a() {
        PropertyManager.a.info("Generating new properties file");
        this.savePropertiesFile();
    }

    public void savePropertiesFile() {
        FileOutputStream fileoutputstream = null;

        try {
            if (!this.file.exists() || this.file.canWrite()) {
                fileoutputstream = new FileOutputStream(this.file);
                this.properties.store(fileoutputstream, "Minecraft server properties");
                return;
            }
        } catch (Exception exception) {
            PropertyManager.a.warn("Failed to save " + this.file, exception);
            this.a();
            return;
        } finally {
            if (fileoutputstream != null) {
                try {
                    fileoutputstream.close();
                } catch (IOException ioexception) {
                    ;
                }
            }

        }

    }

    public File c() {
        return this.file;
    }

    public String getString(String s, String s1) {
        if (!this.properties.containsKey(s)) {
            this.properties.setProperty(s, s1);
            this.savePropertiesFile();
            this.savePropertiesFile();
        }

        return (String) this.getOverride(s, this.properties.getProperty(s, s1));
    }

    public int getInt(String s, int i) {
        try {
            return ((Integer) this.getOverride(s, Integer.valueOf(Integer.parseInt(this.getString(s, "" + i))))).intValue();
        } catch (Exception exception) {
            this.properties.setProperty(s, "" + i);
            this.savePropertiesFile();
            return ((Integer) this.getOverride(s, Integer.valueOf(i))).intValue();
        }
    }

    public long getLong(String s, long i) {
        try {
            return ((Long) this.getOverride(s, Long.valueOf(Long.parseLong(this.getString(s, "" + i))))).longValue();
        } catch (Exception exception) {
            this.properties.setProperty(s, "" + i);
            this.savePropertiesFile();
            return ((Long) this.getOverride(s, Long.valueOf(i))).longValue();
        }
    }

    public boolean getBoolean(String s, boolean flag) {
        try {
            return ((Boolean) this.getOverride(s, Boolean.valueOf(Boolean.parseBoolean(this.getString(s, "" + flag))))).booleanValue();
        } catch (Exception exception) {
            this.properties.setProperty(s, "" + flag);
            this.savePropertiesFile();
            return ((Boolean) this.getOverride(s, Boolean.valueOf(flag))).booleanValue();
        }
    }

    public void setProperty(String s, Object object) {
        this.properties.setProperty(s, "" + object);
    }
}
