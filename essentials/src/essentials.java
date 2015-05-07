import bot.nat.sumi.*;

import java.util.Timer;
import java.util.TimerTask;

public class essentials extends Module {
	@Override
	public void command(Message m, Server srv) {
		String[] arg = m.text.split(" ");

		if(arg.length >= 2 && arg[0].equalsIgnoreCase("$raw")){
			if(srv.getUser(m.author).isBotOp){
				srv.send(m.text.replace("$raw ", ""), m.author);

			} else {
				srv.send(m.channel, m.author, "You need to be BotOP to perform this.", m.channel);
			}

		} else if(arg.length == 2){
			if(arg[0].equalsIgnoreCase("$join")){
				srv.send("JOIN "+ arg[1], m.channel);

			} else if(arg[0].equalsIgnoreCase("$quit")){
				srv.send("QUIT :"+ arg[1], m.channel);
				quit();

			} else if(arg[0].equalsIgnoreCase("$part")){
				if(!arg[1].equals("*")){
					srv.send("PART "+ arg[1], m.channel);

					User u = srv.getUser(srv.nick.name);
					u.chan.remove(u.getChan(arg[1]));

				} else {
					for(Chan ch : srv.nick.chan){
						srv.send("PART "+ ch, m.channel);
						srv.getUser(srv.nick.name).chan.remove(ch);
					}
				}

			} else if(arg[0].equalsIgnoreCase("$invite")){
				srv.send("INVITE "+ m.channel +" "+ arg[1], m.channel);

			} else if(arg[0].equalsIgnoreCase("$nick")){
				srv.send("NICK "+ arg[1], m.channel);

			}

		} else if(arg.length == 3){
			if(arg[0].equalsIgnoreCase("$part")){
				if(!arg[1].equals("*")){
					srv.send("PART "+ arg[1] +" :"+ arg[2], m.channel);

					User u = srv.getUser(srv.nick.name);
					u.chan.remove(u.getChan(arg[1]));

				} else {
					for(Chan ch : srv.nick.chan){
						srv.send("PART "+ ch.name +" :"+ arg[2], m.channel);
						srv.getUser(srv.nick.name).chan.remove(ch);
					}
				}

			} else if(arg[0].equalsIgnoreCase("$invite")){
				srv.send("INVITE "+ arg[1] +" "+ arg[2], m.channel);
			}

		} else if(arg[0].equalsIgnoreCase("$quit")){
			srv.send("QUIT", m.channel);
			quit();

		} else if(arg[0].equalsIgnoreCase("$part")){
			srv.send("PART "+ m.channel, m.channel);

			User u = srv.getUser(srv.nick.name);
			u.chan.remove(u.getChan(arg[1]));

		} else {
			srv.send(m.channel, m.author, "Not proper arguments!", m.channel);
		}
	}

	private void quit() {
		new Timer("quit").schedule(new TimerTask() {
			@Override
			public void run() {
				System.exit(0);
			}
		}, 300);
	}

	@Override
	public String[] reserved() {
		return new String[]{ "$join", "$part", "$quit", "$invite", "$nick", "$raw" };
	}
}
