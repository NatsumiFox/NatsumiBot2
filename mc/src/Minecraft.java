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
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        String line;
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
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        process = null;
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

    private String getString(User u) {
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
        write("stop");
        process.waitFor();
        run = false;
    }
}
