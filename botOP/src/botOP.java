import bot.nat.sumi.*;

import java.io.*;
import java.util.Arrays;

public class botOP extends Module {
	@Override
	public void command(Message m, Server srv) {
		String[] arg = m.text.split(" ");

		if(arg.length < 2){
			srv.send(m.channel, m.author, "Available commands: add, remove, list, online", m.channel);
			return;
		}

		if(arg[1].equals("add") && arg.length == 3){
			add(m, srv, arg);

		} else if(arg[1].equals("list")){
			list(m, srv);

		} else if(arg[1].equals("remove") && arg.length == 3){
			rmv(m, srv, arg);

		} else if(arg[1].equals("online")){
			on(m, srv);
		}
	}

	private void on(Message m, Server srv) {
		String s = "";

		for(User u : srv.getUsers()){
			if(u.isBotOp){
				s += u.name +" ";
			}
		}

		srv.send(m.channel, m.author, s, m.channel);
	}

	private void rmv(Message m, Server srv, String[] arg) {

	}

	private void list(Message m, Server srv) {
		srv.send(m.channel, m.author, Arrays.toString(Main.read(new File(Main.folder + "BOTOPS." + srv.cfgName + ".txt")).replace("\r", "").split("\n")), m.channel);
	}

	private void add(Message m, Server srv, String[] arg) {
		User u = srv.getUser(arg[2]);

		if(u == null){
			srv.send(m.channel, m.author, "user "+ arg[2] +" not found!", m.channel);
			return;
		}

		if(u.isLoggedIn){
			u.isBotOp = true;
			srv.send(m.channel, m.author, "user "+ u.name +" is now botOP!", m.channel);

			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Main.folder +"BOTOPS."+ srv.cfgName +".txt", true)))) {
				out.println(u.name +"\n");

			} catch (IOException e) {
				//exception handling left as an exercise for the reader
			}

		} else {
			u.isBotOp = true;
			srv.send(m.channel, m.author, "user "+ u.name +" is not logged in; temporary BotOP status granted.", m.channel);
		}
	}

	@Override
	public String[] reserved() {
		return new String[]{ "$botop" };
	}
}
