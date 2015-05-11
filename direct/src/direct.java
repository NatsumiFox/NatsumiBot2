import bot.nat.sumi.Message;
import bot.nat.sumi.Module;
import bot.nat.sumi.Server;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

public class direct extends Module {
	private ChatterBotSession current;

	public direct() throws Exception {
		ChatterBotFactory factory = new ChatterBotFactory();

		ChatterBot bot1 = factory.create(ChatterBotType.CLEVERBOT);
		current = bot1.createSession();
	}

	@Override
	public void command(Message m, Server srv) {
		if(!m.text.toLowerCase().startsWith(srv.nick.name.toLowerCase() + ": ")){
			return;
		}

		String thought = m.text.substring((srv.nick.name +": ").length(), m.text.length());

		if(thought.equalsIgnoreCase("*cl")){
			try {
				ChatterBotFactory factory = new ChatterBotFactory();
				ChatterBot bot2 = factory.create(ChatterBotType.CLEVERBOT);
				current = bot2.createSession();
				srv.send(m.channel, m.author, "using CLEVERBOT", m.channel);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return;

		} else if(thought.equalsIgnoreCase("*jab")){
			try {
				ChatterBotFactory factory = new ChatterBotFactory();
				ChatterBot bot2 = factory.create(ChatterBotType.JABBERWACKY);
				current = bot2.createSession();
				srv.send(m.channel, m.author, "using JABBERWACKY", m.channel);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return;

		} else if(thought.equalsIgnoreCase("*pan")){
			try {
				ChatterBotFactory factory = new ChatterBotFactory();
				ChatterBot bot2 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
				current = bot2.createSession();
				srv.send(m.channel, m.author, "using PANDORABOTS", m.channel);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return;

		} else if(thought.equalsIgnoreCase("*")){
			srv.send(m.channel, m.author, "Available: [cl, jab, pan]", m.channel);
			return;
		}

		try {
			String think = current.think(thought);
			int amt = 0;

			while(think.replace(" ", "").equals("")){
				think = current.think(thought);
				amt ++;

				if(amt >= 50){
					srv.send(m.channel, m.author, "Could not think!", m.channel);
					return;
				}
			}

			srv.send(m.channel, m.author, think.replace("\n", "").replace("\r", ""), m.channel);
			return;

		} catch (Exception e) {
			e.printStackTrace();
		}

		srv.send(m.channel, m.author, "Could not think!", m.channel);
	}

	@Override
	public String[] reserved() {
		return new String[]{ "" };
	}
}
