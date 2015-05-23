package bot.nat.sumi;

public abstract class Module {
    public String jar;
	public abstract void command(Message m, Server srv);
	public abstract String[] reserved();
}
