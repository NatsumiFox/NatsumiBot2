import bot.nat.sumi.Message;
import bot.nat.sumi.Module;
import bot.nat.sumi.Server;

@SuppressWarnings("deprecation")
public class mc extends Module {
	private final Thread minecraft;

	public mc(){
		minecraft = new Thread(new Minecraft(), "Minecraft");
		minecraft.start();
	}

	@Override
	public void command(Message m, Server srv) {
	}

	@Override
	public String[] reserved() {
		return new String[]{ "" };
	}

	@Override
	public void finalize() throws Throwable {
		super.finalize();
		minecraft.stop();
	}
}

