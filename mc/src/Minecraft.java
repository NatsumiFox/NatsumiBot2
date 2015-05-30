import bot.nat.sumi.*;

import java.io.*;
import java.util.*;

public class Minecraft implements Runnable {
	public final static int PORT_RANGE = 0x8FF;
	public final static int PORT_CENTER = 25565;
	public int port_current = 25565;

	public Server srv;
	public final static String mcIP = "boredmc.ddns.net";
	public final static String srvIP = "irc.badnik.net";
	public final static String Chan =  "#ducks";

	public final static String folder = "B:\\mc\\";
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
	    sleep(50);

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

			        if (line.contains(" logged in")) {
				        send(Format.YELLOW.i + line.split(": ")[1].split("\\[")[0] +" joined the game");

			        } else if (line.contains("lost connection:") && !line.contains("com.mojang.authlib.GameProfile")) {
				        String player = line.split("INFO\\]: ")[1].split(" lost connection:")[0];
				        send(Format.YELLOW.i + player + " left the game: " + line.split(player + " ")[1].split(", siblings")[0]);

			        } else if (line.contains("Can't keep up!")) {
				        send(Format.RED.i + line.split("\\? ")[1]);

			        } else if (line.contains("INFO]: <")) {
						String text = line.split("> ")[1], nick = line.substring(line.indexOf('<'), line.indexOf('>') - 1);
						if (!cmd(nick, text)) {
							MCToIRC(nick, text);
				        }

					} else if (line.contains("INFO]: * ")) {
						String text = line.split("\\* ")[1], nick = text.split(" ")[0];
						text = text.substring(nick.length() +1, text.length());

						if (!cmd(nick, text)) {
							MCToIRC("* "+ nick, text);
						}

			        } else if (line.contains("INFO]: Done (")) {
				        send(Format.DGREEN.i +"Server started in "+ line.split("Done \\(")[1].split("\\)!")[0] +" to "+ mcIP +":"+ port_current);

			        } else if (line.contains("TPS from last")) {
				        send(Format.DGREEN.i + line.split("INFO]: ")[1]);

			        } else if ((tmp = isDeathMessage(line.substring(line.indexOf("INFO]: ") + 7, line.length()))) != null) {
				        send(Format.RED + tmp);
			        }
		        }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

		System.out.println(Main.folder +"spigot.jar " + pmc.exitValue());
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

    private boolean cmd(String t, String person) {
        if(t.startsWith(Main.cmd)){
            switch (t.replace(Main.cmd, "")){
	            case "list":
		            write("tellraw " + person + " {text:\"\""+ Format.WHITE.j +",extra:[" + list() + "]}");
		            break;

	            default:
                    write("tellraw "+ person +" "+ help());
            }

            return true;
        } else {
            return false;
        }
    }

	private String help() {
		return "{text:\"Available commands: \""+ Format.WHITE.j +",extra:[" +
				hover("list", "usage: $list\\nShows users in current IRC channel") +"]}";
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
}
