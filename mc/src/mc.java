import bot.nat.sumi.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3._MinecraftLogin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@SuppressWarnings("deprecation")
public class mc extends Module implements Closed {
    private final Minecraft instance;

    // NetHandlerLoginServer.getOfflineProfile
	public mc(){
        instance = new Minecraft();
        Thread minecraft = new Thread(instance, "Minecraft");
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
		minecraft.start();

        Main.spec.add(0, this);
	}

	@Override
	public void command(Message m, Server srv) {
        if(!m.type.equals("PRIVMSG")){
            not(m, srv);

        } else if(m.text.startsWith(Main.cmd + "mc")){
            cmd(m, srv);

        } else if(Minecraft.Chan.equals(m.channel)) {
            instance.write("tellraw @a {text:\"<" + m.author + "> " + m.text + "\",color:\"gray\",hoverEvent:{action:\"show_text\",value:\"" + m.channel + "\"}}");
        }
	}

    private void not(final Message m, Server srv) {
        switch (m.type){
            case "JOIN":
                instance.write("tellraw @a {text:\"" + SpecialModules.getUser(m.author) + "\",color:\"yellow\",hoverEvent:{action:\"show_text\",value:\"" + m.channel.replace(":", "") + "\"},extra:[{text:\" joined the channel.\",color:\"yellow\"}]}");
                return;

            case "PART":
                instance.write("tellraw @a {text:\""+ SpecialModules.getUser(m.author) +"\",color:\"yellow\",hoverEvent:{action:\"show_text\",value:\""+ m.channel +"\"},extra:[{text:\" left the channel.\",color:\"yellow\",hoverEvent:{action:\"show_text\",value:\""+ m.text.replace(" :", "") +"\"}}]}");
                return;

            case "QUIT":
                instance.write("tellraw @a {text:\""+ SpecialModules.getUser(m.author) +"\",color:\"yellow\",extra:[{text:\" quit the channel.\",color:\"yellow\",hoverEvent:{action:\"show_text\",value:\""+ m.channel.replace(":", "") + m.text +"\"}}]}");
                return;

            case "NICK":
                User u = srv.getUser(SpecialModules.getUser(m.author));
                instance.write("tellraw @a {text:\""+ SpecialModules.getUser(m.author) +" is now know as "+ m.channel +"!\",color:\"yellow\",hoverEvent:{action:\"show_text\",value:\""+ instance.getString(u) +"\"}}");
                return;
        }
    }

    private void cmd(Message m, Server srv) {
        String s = m.text.replace(Main.cmd +"mc ", "");

        switch (s.split(" ")[0]){
            case "help":
                help(s.replace("help", "").replace(" ", ""), m, srv);
                return;

	        case "ip":
		        /* R() in MinecraftServer is getPort() */
		    //    srv.send(m.channel, m.author, Minecraft.mcIP + ":" + instance.mc.R(), m.channel);
		        return;

            case "list":
                srv.send(m.channel, m.author, resolvePlayers(), m.channel);
                return;

	        case "info":
		        GameProfile p = findPlayer(s.replace("info ", ""));
		        if(p == null){
			        srv.send(m.channel, m.author, "Player '"+ s.replace("info ", "") +"' is not online!", m.channel);
			        return;
		        }

		        srv.send(m.channel, m.author, p.toString(), m.channel);
		        return;

            case "raw":
                if(m.author.equalsIgnoreCase("Natsumi") && srv.getUser(m.author).isBotOp) {
                    instance.write(s.replace("raw ", ""));

                } else {
                    srv.send(m.channel, m.author, "You are not permitted to do this!", m.channel);
                }
                return;

            case "kick":
                if(srv.getUser(m.author).isBotOp) {
                    instance.write(s);

                } else {
                    srv.send(m.channel, m.author, "You are not permitted to do this!", m.channel);
                }
                return;

	        case "link":
		        srv.send(m.channel, m.author, _MinecraftLogin.link(s, m.author, srv), m.channel);
		        return;

	        case "tps":
		        instance.write("tps");
		        return;

	        case "login":
		        srv.send(m.channel, m.author, _MinecraftLogin.login(s, m.author, srv), m.channel);
		        return;

	        case "ghost":
                try {
                    srv.send(m.channel, m.author, _MinecraftLogin.ghost(s, m.author, srv, instance.writer), m.channel);
                } catch (IOException e) {
                    e.printStackTrace();
                    srv.send(m.channel, m.author, "GHOST failed!", m.channel);
                }
                return;

            default:
                help("", m, srv);
        }
    }

	private String resolvePlayers() {
	/*	ArrayList<String> ret = new ArrayList<>();
        Collections.addAll(ret, instance.mc.getPlayerList().f());
		return Arrays.toString(ret.toArray(new String[ret.size()]));*/

        return "";
	}

	public GameProfile findPlayer(String name) {
	/*	for(GameProfile s : instance.mc.getPlayerList().g()){
			if(s.getName().equals(name)){
				return s;
			}
		}*/

		return null;
	}

	private void help(String h, Message m, Server srv) {
        if(h.equals("")){
	        srv.send(m.channel, m.author, "Available commands: help, link, login, ghost, ip, list, info, raw, kick", m.channel);
            srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc help _command_", m.channel);

        } else {
            switch (h){
                case "help":
                    srv.send(m.channel, m.author, "Sorry, I can not help you!", m.channel);
                    return;

                case "list":
                    srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc list", m.channel);
                    srv.send(m.channel, m.author, "Lists all online users on target server", m.channel);
                    return;

	            case "tps":
		            srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc tps", m.channel);
		            srv.send(m.channel, m.author, "Gets the TicksPerSecond values from server. 20 is default, any less means there was lag.", m.channel);
		            return;

	            case "info":
		            srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc info _user_", m.channel);
		            srv.send(m.channel, m.author, "Tells more about user on target server", m.channel);
		            return;

	            case "ip":
		            srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc ip", m.channel);
		            srv.send(m.channel, m.author, "Tells the currently used IP on the server", m.channel);
		            return;

                case "raw":
                    srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc raw _command_", m.channel);
                    srv.send(m.channel, m.author, "Send raw command to the console", m.channel);
                    return;

                case "kick":
                    srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc raw _user_", m.channel);
                    srv.send(m.channel, m.author, "Kick specified user from the server.", m.channel);
                    return;

	            case "link":
		            srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc link _account_ [password]", m.channel);
		            srv.send(m.channel, m.author, "Link Minecraft username with IRC account. "+
				            "NOTE: Password is required to access your account with unregistered users and different usernames!", m.channel);
		            return;

	            case "login":
		            srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc login [password]", m.channel);
		            srv.send(m.channel, m.author, "Allow to login to Minecraft server once. NOTE: Password may be required!", m.channel);
		            return;

	            case "ghost":
		            srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc ghost [password]", m.channel);
		            srv.send(m.channel, m.author, "Kicks user using your account. NOTE: Password may be required!", m.channel);
		            return;

                default:
                    srv.send(m.channel, m.author, "Available commands: help, link, login, ghost, ip, list, info, raw, kick", m.channel);
                    srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc help _command_", m.channel);
            }
        }
    }

    @Override
	public String[] reserved() {
		return new String[]{ "" };
	}

    @Override
    public void close() {
        try {
            instance.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

