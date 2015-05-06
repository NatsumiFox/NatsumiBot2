import bot.nat.sumi.Main;
import bot.nat.sumi.Message;
import bot.nat.sumi.Module;
import bot.nat.sumi.Server;

public class reload extends Module {
	@Override
	public void command(Message m, Server srv) {
		if(m.text.startsWith("$reload")){
			srv.reloadModules();
			srv.send(m.channel, m.author, "Reload complete!", m.channel);

		} else {
			if(srv.unload(Main.folder +"commands\\"+ m.text.split(" ")[1] +".jar")){
				srv.send(m.channel, m.author, "Successfully unloaded '"+ m.text.split(" ")[1] +".jar'", m.channel);
			} else {
				srv.send(m.channel, m.author, "failed to unload '"+ m.text.split(" ")[1] +".jar'", m.channel);
			}
		}
	}

	@Override
	public String[] reserved() {
		return new String[]{ "$reload", "$unload" };
	}
}
