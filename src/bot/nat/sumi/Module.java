package bot.nat.sumi;

public abstract class Module {
	public abstract void command(Message m, Server srv);
	public abstract String[] reserved();
}
