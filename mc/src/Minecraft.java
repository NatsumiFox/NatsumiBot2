import bot.nat.sumi.*;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class Minecraft implements Runnable {
	public final static int PORT_RANGE = 0x8FF;
	public final static int PORT_CENTER = 25565;
	public int port_current = 25565;

	public Server srv;
	public final static String mcIP = "boredmc.ddns.net";
	public final static String srvIP = "irc.badnik.net";
	public final static String Chan =  "#ducks";

	public final static String folder = "H:\\Minecraft\\bored";
    public final static int JAVA_MAX = 1024;
	public Process pmc;

    public BufferedWriter writer;
	private String read = "";

    @Override
	public void run() {
		new Timer("GetServerMinecraft").schedule(new TimerTask() {
			@Override
			public void run() {
                while (srv == null) {
                    sleep(5);
                    srv = Servers.get(srvIP);
                }
			}
		}, 1);

	//	generateRandomPort();
	    try {
		    generateProcess();
	    } catch (IOException e) {
		    e.printStackTrace();
	    }
	    sleep(100);

        /* get bufferedReader and BufferedWriter instances */
	    BufferedReader reader = new BufferedReader (new InputStreamReader(pmc.getInputStream()));
	    writer = new BufferedWriter(new OutputStreamWriter(pmc.getOutputStream()));

	    new Thread(new Runnable() {
		    @Override
		    public void run() {
			    int r;
			    while (pmc != null && pmc.isAlive()){
				    try {
					    if((r = System.in.read()) != -1){
							writer.write(r);
						    writer.flush();
					    }
				    } catch (IOException e) {
					    e.printStackTrace();
				    }
			    }
		    }
	    }, "Minecraft I->I").start();

        String tmp, line;
        try {
	        while(pmc != null && pmc.isAlive()){
				if((line = reader.readLine()) != null) {
			        System.out.println(line);

					if(line.contains("com.mojang.authlib.GameProfile") && line.contains("lost connection: ")) {
						send(Format.RED.i + line.split("name=")[1].split(",properties=")[0] + " failed to connect: " + line.split("lost connection: ")[1]);

					} else if(line.contains("com.mojang.authlib.GameProfile") && line.contains("Disconnecting")){

					} else if (line.contains(" logged in")) {
				        send(Format.YELLOW.i + line.split(": ")[1].split("\\[")[0] +" joined the game");

			        } else if (line.contains("lost connection:")) {
				        String player = line.split("INFO\\]: ")[1].split(" lost connection:")[0];
				        send(Format.YELLOW.i + player + " left the game: " + line.split(player + " ")[1].split(", siblings")[0]);

			        } else if (line.contains("Can't keep up!")) {
				        send(Format.RED.i + line.split("\\? ")[1]);

			        } else if (line.contains("INFO]: <")) {
						String text = line.split("> ")[1], nick = line.substring(line.indexOf('<'), line.indexOf('>') + 1);
						if (!cmd(nick.substring(1, nick.length() -1), text)) {
							MCToIRC(nick, text);
				        }

					} else if (line.contains("INFO]: * ")) {
						String text = line.split("\\* ")[1], nick = text.split(" ")[0];
						text = text.substring(nick.length() +1, text.length());
						MCToIRC("* "+ nick, text);

			        } else if (line.contains("INFO]: Done (")) {
				        send(Format.DGREEN.i +"Server started in "+ line.split("Done \\(")[1].split("\\)!")[0] +" to "+ mcIP +":"+ port_current);

			        } else if (line.contains("TPS from last")) {
				        send(Format.DGREEN.i + line.split("INFO]: ")[1]);

			        } else if ((tmp = isDeathMessage(line.substring(line.indexOf("INFO]: ") + 7, line.length()))) != null) {
				        send(Format.RED.i + tmp);
			        }
		        }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

		System.err.println("Closing: " + Main.folder + "spigot.jar " + pmc.exitValue());
    }

	/* translate Minecraft chat to IRC chat */
	private void MCToIRC(String nick, String text) {
		for(Format f : Format.values()){
			text = text.replace(f.c, f.i);
		}

		send(nick +' '+ text);
	}

	private String isDeathMessage(String line) {
        if(line.contains(" was shot by ") || line.contains(" was squashed ") || line.contains(" was burnt to a crisp ") ||
                line.contains(" was blown from a high place by ") || line.contains(" was shot off ") ||
                line.contains(" was doomed to fall by ") || line.contains(" was blown up by ") ||
                line.contains(" was slain by ") || line.contains(" was fireballed by ") || line.contains(" was killed by ") ||
                line.contains(" was killed while trying to hurt ") || line.contains(" was knocked into the void by ") ||
                line.contains(" was pummeled by ") /* unused but here for future support */){
            return line.split(" was ")[0] +" died: was "+ line.split(" was ")[1];

        } else if(line.endsWith(" was pricked to death")){
            return line.split(" was pricked to death")[0] +" died: was pricked to death";

        } else if(line.contains(" walked into a cactus ")){
            return line.split(" walked ")[0] +" died: walked "+ line.split(" walked ")[1];

        } else if(line.contains(" drowned")){
            return line.split(" drowne")[0] +" died: drowne"+ line.split(" drowne")[1];

        } else if(line.endsWith(" blew up")){
            return line.split(" blew up")[0] +" blew up";

        } else if(line.endsWith(" hit the ground too hard")){
            return line.split(" hit the ground too hard")[0] +" died: hit the ground too hard";

        } else if(line.endsWith(" fell from a high place")){
            return line.split(" fell from a high place")[0] +" died: fell from a high place";

        } else if(line.contains(" fell off ")){
            return line.split(" fell off ")[0] +" died: fell off "+ line.split(" fell off ")[1];

        } else if(line.contains(" fell out ")){
            return line.split(" fell out ")[0] +" died: fell off "+ line.split(" fell out ")[1];

        } else if(line.contains(" fell into ")){
            return line.split(" fell into ")[0] +" died: fell into "+ line.split(" fell into ")[1];

        } else if(line.contains(" walked into a fire ")){
            return line.split(" walked ")[0] +" died: walked " + line.split(" walked ")[1];

        } else if(line.endsWith(" went up in flames")) {
            return line.split(" went up in flames")[0] +" died: went up in flames";

        } else if(line.endsWith(" burned to death")){
            return line.split(" burned to death")[0] + " died: burned to death";

        } else if(line.contains(" tried to swim in lava")){
            return line.split(" tried ")[0] +" died: tried " + line.split(" tried ")[1];

        } else if(line.endsWith(" was struck by lightning")){
            return line.split(" was struck by lightning")[0] +" died: was struck by lightning";

        } else if(line.contains(" got finished off by ")){
            return line.split(" got ")[0] +" died: got "+ line.split(" got ")[1];

        } else if(line.endsWith(" suffocated in a wall")){
            return line.split(" suffocated in a wall")[0] +" died: suffocated in a wall";

        } else if(line.endsWith(" fell out of the world")){
            return line.split(" fell out of the world")[0] +" died: fell out of the world";

        } else if(line.endsWith(" fell from a high place and fell out of the world")){
            return line.split(" fell from a high place and fell out of the world")[0] +" died: fell from a high place and fell out of the world";

        } else if(line.endsWith(" withered away")){
            return line.split(" withered away")[0] +" died: withered away";

        } else if(line.endsWith(" starved to death")){
            return line.split(" starved to death")[0] +" died: starved to death";

        }

        return null;
    }

    private boolean cmd(String person, String t) {
        if(t.startsWith(Main.cmd)){
            switch (t.split(" ")[0].replace(Main.cmd, "")){
	            case "list":
		            write("tellraw " + person + " {text:\"\""+ Format.WHITE.j +",extra:[" + list() + "]}");
		            break;

	            case "difficulty":
		            write("tellraw @a {text:\""+ difficulty(t.replace(Main.cmd + "difficulty", "").replace(" ", "")) +"}");
					break;

	            default:
                    write("tellraw "+ person +" "+ help());
            }

            return true;
        } else {
            return false;
        }
    }

	private String difficulty(String d) {
		switch (d){
			case "":
				return diffCfg().getField("d").getValue();

			case "3":case "h":
				return difficulty0("hard", "3");

			case "4":case "s":
				return difficulty0("superhard", "4");

			case "0":case "p":
				return difficultyp();
		}

		return "invalid Symbol '"+ d +"'!\""+ Format.RED.j;
	}

	final int minTime = 2*60*1000;
	private String difficulty0(String mode, String num) {
		ConfigFile cfg = diffCfg();

		if(cfg.containsField("time") && Long.parseLong(cfg.getField("time").getValue()) + minTime > System.currentTimeMillis()){
			return "Cannot change mode: Not enough time has passed since last change! 2 minutes required!\""+ Format.RED.j;
		}

		if(cfg.containsField("mode") && cfg.getField("mode").getValue().equals(mode)){
			return "Already in this mode!\""+ Format.RED.j;
		}

		try {
			copyFile(new File(Main.folder + "spigot.yml" + num), new File(folder +"\\spigot.yml"));
			copyFile(new File(Main.folder + "bukkit.yml" + num), new File(folder + "\\bukkit.yml"));
			copyFile(new File(Main.folder + "hp.yml" + num), new File(folder + "\\plugins\\HarderPigmen\\config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
			return "Failed to change mode!\""+ Format.RED.j;
		}


		cfg.setField("time", System.currentTimeMillis() +"");
		cfg.setField("mode", mode);
		cfg.flush();

		write("reload");
		return "Successfully changed mode to "+ mode +"!\""+ Format.GREEN.j;
	}

	private String difficultyp() {
		ConfigFile cfg = diffCfg();

		if(cfg.containsField("time") && Long.parseLong(cfg.getField("time").getValue()) + minTime > System.currentTimeMillis()){
			return "Cannot change mode: Not enough time has passed since last change! 2 minutes required!\""+ Format.RED.j;
		}

		write("difficulty 0");

		new Timer("DifficultyTimer").schedule(new TimerTask() {
			@Override
			public void run() {
				write("difficulty 3");
			}
		}, 5 * 1000);


		cfg.setField("time", System.currentTimeMillis() +"");
		cfg.flush();
		return "Difficulty set to peaceful for 5 seconds!\""+ Format.GREEN.j;
	}

	private ConfigFile diffCfg() {
		return new ConfigFile(new File(Main.folder +"difficulty.txt"), ConfigFile.READ | ConfigFile.WRITE);
	}

	private String help() {
		return "{text:\"Available commands: \""+ Format.WHITE.j +",extra:[" +
				hover("list ", "usage: $list\\nShows users in current IRC channel") +","+
				hover("difficulty ", "usage: difficulty\\nChanges between hard or superhard difficulty\\nValid values are 3, 4, h and s") +"]}";
	}

    private String list() {
        String ret = "";

        for(User u : srv.getUsers()){
	        if(u != null){
                ret += hover(u.name +" ", getString(u)) +",";
	        }
        }

        return ret.substring(0, ret.length() -1);
    }
	
	private String hover(String name, String usage) {
		return "{text:\""+ name +"\""+ Format.WHITE.j +",hoverEvent:{action:\"show_text\",value:\""+ usage +"\"}}";
	}

    public static String getString(User u) {
	    if(u == null) return null;

        return u.userName +"@"+ u.ident +": "+ u.realName +"\\n"+
                u.chanToString() +"\\n"+
                "vars: isLoggedIn "+ u.isLoggedIn +"; isBotOp "+ u.isBotOp;
    }

    private void send(String s) {
        if(srv != null){
            srv.send("PRIVMSG "+ Chan +" :"+ s, "Minecraft");
        }
    }

    private void generateProcess() throws IOException {
        while(pmc == null || !pmc.isAlive()){
	        pmc = Runtime.getRuntime().exec("java -jar "+ Main.folder +"spigot.jar nogui --port "+ PORT_CENTER);
        }
    }

	private void generateRandomPort() {
		Random r = new Random(System.currentTimeMillis());
		File d = new File(folder +"server.properties");
		String file = Main.read(d);

		int pos1 = file.indexOf("server-port=") + "server-port=".length();
		port_current = Integer.parseInt(file.substring(pos1, file.indexOf('\r', pos1)));
		int n = port_current;

		while (n == port_current){
			n = (r.nextInt() & PORT_RANGE) + PORT_CENTER;
		}

		Main.write(d, file.replace("server-port="+ port_current, "server-port="+ n), false);
		port_current = n;
	}

    public void write(String text){
	    if(writer != null){
		    try {
			    writer.write(text);
			    writer.newLine();
			    writer.flush();
			    System.out.println("Minecraft write: " + text);

		    } catch (IOException e) {
			    e.printStackTrace();

			    if(pmc != null && pmc.isAlive()){
				    writer = new BufferedWriter(new OutputStreamWriter(pmc.getOutputStream()));
			    }
		    }
	    }
    }

	/* wait for a time */
	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException, InterruptedException {
		if (pmc != null && pmc.isAlive()) {
			write("stop");
			pmc.waitFor();
		}
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if(!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		finally {
			if(source != null) {
				source.close();
			}
			if(destination != null) {
				destination.close();
			}
		}
	}
}
