import bot.nat.sumi.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Minecraft implements Runnable {
	public Server srv;
	public final static String srvIP = "irc.badnik.net";
	public final static String Chan =  "#ducks";

	public final static String folder = "B:\\mc\\";
    public final static int JAVA_MAX = 1024;
    private Process process;

    private boolean run = true;
    private BufferedWriter writer;
    public ArrayList<String> players;

    @Override
	public void run() {
        players = new ArrayList<>();
		new Timer("GetServerMinecraft").schedule(new TimerTask() {
			@Override
			public void run() {
                while (srv == null) {
                    sleep(5);
                    srv = Servers.get(srvIP);
                }
			}
		}, 1);

        generateProcess();

        /* get bufferedReader and BufferedWriter instances */
        BufferedReader reader = new BufferedReader (new InputStreamReader(process.getInputStream()));
        final BufferedReader in = new BufferedReader (new InputStreamReader(System.in));
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        new Thread(new Runnable() {
            public String line;

            @Override
            public void run() {
                try {
                    while(run){
                        if((line = in.readLine()) != null) {
                            write(line);
                        } else {
                            sleep(5);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "MinecraftI->I").start();

        String line, tmp;
        try {
            while((line = reader.readLine()) != null && run){
                System.out.println(line);

                if(line.endsWith(" joined the game")){
                    send(Format.Color.YELLOW + line.split(": ")[1]);
                    players.add(line.split(": ")[1].replace(" joined the game", ""));

                } else if(line.endsWith(" left the game")) {
                    players.remove(line.split(": ")[1].replace(" left the game", ""));

                } else if(line.contains("lost connection:")){
                    send(Format.Color.YELLOW + line.split(": ")[1].split(" lost")[0] +" left the game: "+ line.split("TextComponent\\{text='")[1].split("', siblings")[0]);

                } else if(line.contains("Can't keep up!")){
                    send(Format.Color.RED + line.split("\\? ")[1]);

                } else if(line.contains("INFO]: <")){
                    if(!cmd(line.split("> ")[1], line.substring(line.indexOf('<') +1, line.indexOf('>')))) {
                        send(line.split("\\] \\[Server thread/INFO\\]: ")[1]);
                    }

                } else if((tmp = isDeathMessage(line.substring(line.indexOf("INFO]: ") + 7, line.length()))) != null){
                    send(Format.Color.RED + tmp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        process = null;
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
                    write("tellraw "+ person +" {text:\"\",color:\"white\",extra:["+ list() +"]}");
                    break;

                default:
                    write("tellraw "+ person +" "+ help());
            }

            return true;
        } else {
            return false;
        }
    }

    private String list() {
        String ret = "";

        for(User u : srv.getUsers()){
            ret += hover(u.name +" ", getString(u)) +",";
        }

        return ret.substring(0, ret.length() -1);
    }

    public String getString(User u) {
        return u.userName +"@"+ u.ident +": "+ u.realName +"\\n"+
                u.chanToString() +"\\n"+
                "vars: isLoggedIn "+ u.isLoggedIn +"; isBotOp "+ u.isBotOp;
    }

    private String help() {
        return "{text:\"Available commands: \",color:\"white\",extra:[" +
                hover("list", "usage: $list\\nShows users in current IRC channel") +"]}";
    }

    private String hover(String name, String usage) {
        return "{text:\""+ name +"\",color:\"white\",hoverEvent:{action:\"show_text\",value:\""+ usage +"\"}}";
    }

    private void send(String s) {
        if(srv != null){
            srv.send("PRIVMSG "+ Chan +" :"+ s, "Minecraft");
        }
    }

    private void generateProcess() {
        while(process == null){
            try {
                process = Runtime.getRuntime().exec("java -Xmx" + JAVA_MAX + "M -jar " + folder + "minecraft_server.jar nogui");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(String text){
        try {
            writer.write(text);
            writer.newLine();
            writer.flush();
            System.out.println("Minecraft write: "+ text);
        } catch (IOException e) {
            e.printStackTrace();
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
        if(run) {
            write("stop");
            process.waitFor();
            run = false;
        }
    }
}
