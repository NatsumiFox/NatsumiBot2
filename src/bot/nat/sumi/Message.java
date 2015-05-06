package bot.nat.sumi;

public class Message {
	public final String author;
	public final String type;
	public final String text;
	public final String channel;

	public Message(String author, String type, String chan, String text){
		this.author = author;
		this.type = type;
		this.channel = chan;
		this.text = text;
	}
}
