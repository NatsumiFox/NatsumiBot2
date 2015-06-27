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
			IRCToMC(m);
		}
	}

	private void IRCToMC(Message m) {
		String cl = Format.GRAY.j, type = "", orig = m.text.replace("\"", "\\\""), ret = "tellraw @a {text:\"<"+ m.author +"> \"";

		if(orig.contains("\001ACTION")){
			orig = orig.replace("\001ACTION", "").replace("\001", "");
			ret = "tellraw @a {text:\"* "+ m.author +"\"";
		}

		ret += Format.GRAY.j +",hoverEvent:{action:\"show_text\",value:\""+ Minecraft.getString(instance.srv.getUser(m.author)) +"\"},extra:[";

		while(!orig.equals("")){
			String add = "{text:\"";
			int indx = Integer.MAX_VALUE, chk;
			boolean normal = true, set = false;

			for(Format f : Format.values()){
				chk = orig.indexOf(f.i);

				if(chk != -1 && chk < indx){
					indx = chk;
				}
			}

			if(((chk = orig.indexOf("http://")) != -1 && chk < indx) || ((chk = orig.indexOf("https://")) != -1 && chk < indx)){
				indx = chk;
			}

			back:
			if(indx != Integer.MAX_VALUE){
				if(indx != 0){
					add += orig.substring(0, indx) +"\"";
					orig = orig.substring(indx, orig.length());

				} else {
					if(orig.startsWith("http://") || orig.startsWith("https://")){
						normal = false;
						int last = orig.indexOf(' ');
						if(last <= 0){
							last = orig.length();
						}

						String url = orig.substring(0, last);

						add += url +"\""+ cl + type + style("click", "open_url", url) + style("hover", "show_text", url.split("//")[1].split("/")[0]);
						orig = orig.substring(last, orig.length());

					} else {
						for(Format f : Format.values()){
							if(orig.startsWith(f.i)){
								if(f.equals(Format.RESET)){
									type = "";
									cl = Format.GRAY.j;

								} else if(f.icl){
									type = f.j;

								} else {
									 cl = f.j;
								}

								orig = orig.substring(f.i.length(), orig.length());
								add += orig.substring(0, indx) +"\"";
								orig = orig.substring(indx, orig.length());
								set = true;
								break back;
							}
						}

						orig = orig.substring(1, orig.length());
					}
				}

			} else {
				add += orig +"\"";
				orig = "";
			}


			if(!set) {
				if (normal) {
					add += cl + type + style("hover", "show_text", m.channel) + "";
				}
				ret += add + "},";
			}
		}

		instance.write(ret.substring(0, ret.length() -1) +"]}");
	}

	private String style(String type, String event, String text) {
		return ","+ type +"Event:{action:\""+ event +"\",value:\"" + text + "\"}";
	}

	private void not(final Message m, Server srv) {
        switch (m.type){
            case "JOIN":
                instance.write("tellraw @a {text:\"" + SpecialModules.getUser(m.author) + "\""+ Format.YELLOW.j+",hoverEvent:{action:\"show_text\",value:\"" + m.channel.replace(":", "") + "\"},extra:[{text:\" joined the channel.\""+ Format.YELLOW.j+"}]}");
                return;

            case "PART":
                instance.write("tellraw @a {text:\""+ SpecialModules.getUser(m.author) +"\""+ Format.YELLOW.j+",hoverEvent:{action:\"show_text\",value:\""+ m.channel +"\"},extra:[{text:\" left the channel.\""+ Format.YELLOW.j+",hoverEvent:{action:\"show_text\",value:\""+ m.text.replace(" :", "") +"\"}}]}");
                return;

            case "QUIT":
                instance.write("tellraw @a {text:\""+ SpecialModules.getUser(m.author) +"\""+ Format.YELLOW.j +",extra:[{text:\" quit the channel.\""+ Format.YELLOW.j+",hoverEvent:{action:\"show_text\",value:\""+ m.channel.replace(":", "") + m.text +"\"}}]}");
                return;

            case "NICK":
                User u = srv.getUser(SpecialModules.getUser(m.channel));
                instance.write("tellraw @a {text:\""+ SpecialModules.getUser(m.author) +" is now known as "+ m.channel +"!\""+ Format.YELLOW.j+",hoverEvent:{action:\"show_text\",value:\""+ Minecraft.getString(u) +"\"}}");
                return;
        }
    }

    private void cmd(Message m, Server srv) {
        String s = m.text.replace(Main.cmd +"mc ", "");

        switch (s.split(" ")[0].split(":")[0]){
            case "help":
                help(s.replace("help", "").replace(" ", ""), m, srv);
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

	        case "ip":
		        srv.send(m.channel, m.author, _MinecraftLogin.ip(s, m.author, srv), m.channel);
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
		            srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc login:[nick] [password]", m.channel);
		            srv.send(m.channel, m.author, "Allow to login to Minecraft server once. :[nick] can be specified to target specified user (if valid). NOTE: Password may be required!", m.channel);
		            return;

	            case "ghost":
		            srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc ghost:[nick] [password]", m.channel);
		            srv.send(m.channel, m.author, "Kicks user using your account. :[nick] can be specified to target specified user (if valid). NOTE: Password may be required!", m.channel);
		            return;

	            case "ip":
		            srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc ip:[nick] _mode_ _ip_ [password]", m.channel);
		            srv.send(m.channel, m.author, "modifies IP to whitelisted IP's list. _mode_ is either; 'add', 'rmv'. _ip_ is your specific IP you'd like to modify. " +
				            ":[nick] can be specified to target specified user (if valid). NOTE: Password may be required!", m.channel);
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

