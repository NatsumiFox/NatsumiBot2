package net.minecraft.server.v1_8_R3;

import com.google.common.base.Joiner;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.command.VanillaCommandWrapper;

public abstract class CommandBlockListenerAbstract implements ICommandListener {

    private static final SimpleDateFormat a = new SimpleDateFormat("HH:mm:ss");
    private int b;
    private boolean c = true;
    private IChatBaseComponent d = null;
    private String e = "";
    private String f = "@";
    private final CommandObjectiveExecutor g = new CommandObjectiveExecutor();
    protected CommandSender sender;

    public CommandBlockListenerAbstract() {}

    public int j() {
        return this.b;
    }

    public IChatBaseComponent k() {
        return this.d;
    }

    public void a(NBTTagCompound nbttagcompound) {
        nbttagcompound.setString("Command", this.e);
        nbttagcompound.setInt("SuccessCount", this.b);
        nbttagcompound.setString("CustomName", this.f);
        nbttagcompound.setBoolean("TrackOutput", this.c);
        if (this.d != null && this.c) {
            nbttagcompound.setString("LastOutput", IChatBaseComponent.ChatSerializer.a(this.d));
        }

        this.g.b(nbttagcompound);
    }

    public void b(NBTTagCompound nbttagcompound) {
        this.e = nbttagcompound.getString("Command");
        this.b = nbttagcompound.getInt("SuccessCount");
        if (nbttagcompound.hasKeyOfType("CustomName", 8)) {
            this.f = nbttagcompound.getString("CustomName");
        }

        if (nbttagcompound.hasKeyOfType("TrackOutput", 1)) {
            this.c = nbttagcompound.getBoolean("TrackOutput");
        }

        if (nbttagcompound.hasKeyOfType("LastOutput", 8) && this.c) {
            this.d = IChatBaseComponent.ChatSerializer.a(nbttagcompound.getString("LastOutput"));
        }

        this.g.a(nbttagcompound);
    }

    public boolean a(int i, String s) {
        return i <= 2;
    }

    public void setCommand(String s) {
        this.e = s;
        this.b = 0;
    }

    public String getCommand() {
        return this.e;
    }

    public void a(World world) {
        if (world.isClientSide) {
            this.b = 0;
        }

        MinecraftServer minecraftserver = MinecraftServer.getServer();

        if (minecraftserver != null && minecraftserver.O() && minecraftserver.getEnableCommandBlock()) {
            minecraftserver.getCommandHandler();

            try {
                this.d = null;
                this.b = executeCommand(this, this.sender, this.e);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Executing command block");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Command to be executed");

                crashreportsystemdetails.a("Command", new Callable() {
                    public String a() throws Exception {
                        return CommandBlockListenerAbstract.this.getCommand();
                    }

                    public Object call() throws Exception {
                        return this.a();
                    }
                });
                crashreportsystemdetails.a("Name", new Callable() {
                    public String a() throws Exception {
                        return CommandBlockListenerAbstract.this.getName();
                    }

                    public Object call() throws Exception {
                        return this.a();
                    }
                });
                throw new ReportedException(crashreport);
            }
        } else {
            this.b = 0;
        }

    }

    public static int executeCommand(ICommandListener icommandlistener, CommandSender commandsender, String s) {
        SimpleCommandMap simplecommandmap = icommandlistener.getWorld().getServer().getCommandMap();
        Joiner joiner = Joiner.on(" ");

        if (s.startsWith("/")) {
            s = s.substring(1);
        }

        String[] astring = s.split(" ");
        ArrayList arraylist = new ArrayList();
        String s1 = astring[0];

        if (s1.startsWith("minecraft:")) {
            s1 = s1.substring("minecraft:".length());
        }

        if (s1.startsWith("bukkit:")) {
            s1 = s1.substring("bukkit:".length());
        }

        if (!s1.equalsIgnoreCase("stop") && !s1.equalsIgnoreCase("kick") && !s1.equalsIgnoreCase("op") && !s1.equalsIgnoreCase("deop") && !s1.equalsIgnoreCase("ban") && !s1.equalsIgnoreCase("ban-ip") && !s1.equalsIgnoreCase("pardon") && !s1.equalsIgnoreCase("pardon-ip") && !s1.equalsIgnoreCase("reload")) {
            if (icommandlistener.getWorld().players.isEmpty()) {
                return 0;
            } else {
                Command command = simplecommandmap.getCommand(astring[0]);

                if (icommandlistener.getWorld().getServer().getCommandBlockOverride(astring[0])) {
                    command = simplecommandmap.getCommand("minecraft:" + astring[0]);
                }

                if (command instanceof VanillaCommandWrapper) {
                    s = s.trim();
                    if (s.startsWith("/")) {
                        s = s.substring(1);
                    }

                    String[] astring1 = s.split(" ");

                    astring1 = VanillaCommandWrapper.dropFirstArgument(astring1);
                    return !((VanillaCommandWrapper) command).testPermission(commandsender) ? 0 : ((VanillaCommandWrapper) command).dispatchVanillaCommand(commandsender, icommandlistener, astring1);
                } else if (simplecommandmap.getCommand(astring[0]) == null) {
                    return 0;
                } else {
                    arraylist.add(astring);
                    WorldServer[] aworldserver = MinecraftServer.getServer().worldServer;
                    MinecraftServer minecraftserver = MinecraftServer.getServer();

                    minecraftserver.worldServer = new WorldServer[minecraftserver.worlds.size()];
                    minecraftserver.worldServer[0] = (WorldServer) icommandlistener.getWorld();
                    int i = 0;

                    int j;

                    for (j = 1; j < minecraftserver.worldServer.length; ++j) {
                        WorldServer worldserver = (WorldServer) minecraftserver.worlds.get(i++);

                        if (minecraftserver.worldServer[0] == worldserver) {
                            --j;
                        } else {
                            minecraftserver.worldServer[j] = worldserver;
                        }
                    }

                    int k;

                    try {
                        ArrayList arraylist1 = new ArrayList();

                        for (k = 0; k < astring.length; ++k) {
                            if (PlayerSelector.isPattern(astring[k])) {
                                for (int l = 0; l < arraylist.size(); ++l) {
                                    arraylist1.addAll(buildCommands(icommandlistener, (String[]) arraylist.get(l), k));
                                }

                                ArrayList arraylist2 = arraylist;

                                arraylist = arraylist1;
                                arraylist1 = arraylist2;
                                arraylist2.clear();
                            }
                        }
                    } finally {
                        MinecraftServer.getServer().worldServer = aworldserver;
                    }

                    j = 0;

                    for (k = 0; k < arraylist.size(); ++k) {
                        try {
                            if (simplecommandmap.dispatch(commandsender, joiner.join(Arrays.asList((String[]) arraylist.get(k))))) {
                                ++j;
                            }
                        } catch (Throwable throwable) {
                            if (icommandlistener.f() instanceof EntityMinecartCommandBlock) {
                                MinecraftServer.getServer().server.getLogger().log(Level.WARNING, String.format("MinecartCommandBlock at (%d,%d,%d) failed to handle command", new Object[] { Integer.valueOf(icommandlistener.getChunkCoordinates().getX()), Integer.valueOf(icommandlistener.getChunkCoordinates().getY()), Integer.valueOf(icommandlistener.getChunkCoordinates().getZ())}), throwable);
                            } else if (icommandlistener instanceof CommandBlockListenerAbstract) {
                                CommandBlockListenerAbstract commandblocklistenerabstract = (CommandBlockListenerAbstract) icommandlistener;

                                MinecraftServer.getServer().server.getLogger().log(Level.WARNING, String.format("CommandBlock at (%d,%d,%d) failed to handle command", new Object[] { Integer.valueOf(commandblocklistenerabstract.getChunkCoordinates().getX()), Integer.valueOf(commandblocklistenerabstract.getChunkCoordinates().getY()), Integer.valueOf(commandblocklistenerabstract.getChunkCoordinates().getZ())}), throwable);
                            } else {
                                MinecraftServer.getServer().server.getLogger().log(Level.WARNING, String.format("Unknown CommandBlock failed to handle command", new Object[0]), throwable);
                            }
                        }
                    }

                    return j;
                }
            }
        } else {
            return 0;
        }
    }

    private static ArrayList<String[]> buildCommands(ICommandListener icommandlistener, String[] astring, int i) {
        ArrayList arraylist = new ArrayList();
        List list = PlayerSelector.getPlayers(icommandlistener, astring[i], EntityPlayer.class);

        if (list != null) {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                if (entityplayer.world == icommandlistener.getWorld()) {
                    String[] astring1 = (String[]) astring.clone();

                    astring1[i] = entityplayer.getName();
                    arraylist.add(astring1);
                }
            }
        }

        return arraylist;
    }

    public String getName() {
        return this.f;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatComponentText(this.getName());
    }

    public void setName(String s) {
        this.f = s;
    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        if (this.c && this.getWorld() != null && !this.getWorld().isClientSide) {
            this.d = (new ChatComponentText("[" + CommandBlockListenerAbstract.a.format(new Date()) + "] ")).addSibling(ichatbasecomponent);
            this.h();
        }

    }

    public boolean getSendCommandFeedback() {
        MinecraftServer minecraftserver = MinecraftServer.getServer();

        return minecraftserver == null || !minecraftserver.O() || minecraftserver.worldServer[0].getGameRules().getBoolean("commandBlockOutput");
    }

    public void a(CommandObjectiveExecutor.EnumCommandResult commandobjectiveexecutor_enumcommandresult, int i) {
        this.g.a(this, commandobjectiveexecutor_enumcommandresult, i);
    }

    public abstract void h();

    public void b(IChatBaseComponent ichatbasecomponent) {
        this.d = ichatbasecomponent;
    }

    public void a(boolean flag) {
        this.c = flag;
    }

    public boolean m() {
        return this.c;
    }

    public boolean a(EntityHuman entityhuman) {
        if (!entityhuman.abilities.canInstantlyBuild) {
            return false;
        } else {
            if (entityhuman.getWorld().isClientSide) {
                entityhuman.a(this);
            }

            return true;
        }
    }

    public CommandObjectiveExecutor n() {
        return this.g;
    }
}
