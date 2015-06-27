import bot.nat.sumi.Main;
import bot.nat.sumi.Message;
import bot.nat.sumi.Module;
import bot.nat.sumi.Server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class reload extends Module {
	@Override
	public void command(Message m, Server srv) {
		if(!srv.getUser(m.author).isBotOp){
			srv.send(m.channel, m.author, "You need to be a BotOP to perform this", m.channel);
			return;
		}

		if(m.text.startsWith("$reload")){
			try {
				Main.reload(Main.folder +"commands\\"+ m.text.split(" ")[1].replace(".jar", "") +".jar");
				srv.send(m.channel, m.author, "Successfully reloaded '"+ m.text.split(" ")[1].replace(".jar", "") +".jar'", m.channel);

			} catch (URISyntaxException | ClassNotFoundException | IOException e) {
				e.printStackTrace();
				srv.send(m.channel, m.author, "failed to reload '"+ m.text.split(" ")[1].replace(".jar", "") +".jar'", m.channel);
			}

		} else {
			if(srv.unload(Main.folder +"commands\\"+ m.text.split(" ")[1].replace(".jar", "") +".jar")){
				if(m.text.split(" ")[1].replace(".jar", "").equals("reload")){
					srv.send(m.channel, m.author, "Can not unload '"+ m.text.split(" ")[1].replace(".jar", "") +".jar'", m.channel);


				} else {
					srv.send(m.channel, m.author, "Successfully unloaded '"+ m.text.split(" ")[1].replace(".jar", "") +".jar'", m.channel);
				}

			} else {
				srv.send(m.channel, m.author, "failed to unload '"+ m.text.split(" ")[1].replace(".jar", "") +".jar'", m.channel);
			}
		}
	}

	@Override
	public String[] reserved() {
		return new String[]{ "$reload", "$unload" };
	}
}
