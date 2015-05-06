import bot.nat.sumi.Message;
import bot.nat.sumi.Module;
import bot.nat.sumi.Server;

public class repo extends Module {
	@Override
	public void command(Message m, Server srv) {
		if(m.text.toLowerCase().startsWith("$sp")){
			srv.send(m.channel, m.author, "https://github.com/TheRetroSnake/SoniPlane-2.0-", m.channel);
			srv.send(m.channel, m.author, "https://github.com/TheRetroSnake/.SoniPlane", m.channel);

		} else {
			srv.send(m.channel, m.author, "https://github.com/TheRetroSnake/NatsumiBot2", m.channel);
		}
	}

	@Override
	public String[] reserved() {
		return new String[]{ "$repo", "$git", "$sp" };
	}
}
