import bot.nat.sumi.Message;
import bot.nat.sumi.Module;
import bot.nat.sumi.Server;

public class nick extends Module {
	@Override
	public void command(Message m, Server srv) {
		if(m.text.split(" ").length >= 1){
			srv.send("NICK "+ m.text.split(" ")[1], m.channel);

		} else {
			srv.send(m.channel, m.author, "No nickname supplied!", m.channel);
		}
	}

	@Override
	public String[] reserved() {
		return new String[]{ "$nick" };
	}
}
