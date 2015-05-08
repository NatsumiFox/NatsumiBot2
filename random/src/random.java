import bot.nat.sumi.Message;
import bot.nat.sumi.Module;
import bot.nat.sumi.Server;

import java.util.Random;

public class random extends Module {
	private final Random r;

	public random(){
		r = new Random(System.nanoTime());
	}

	@Override
	public void command(Message m, Server srv) {
		String[] arg = m.text.split(" ");

		if(arg.length <= 1){
			help(m, srv);
			return;
		}

		if(arg[1].equalsIgnoreCase("help")){
			help(m, srv, arg);

		} else if(arg[1].equalsIgnoreCase("string")){
			string(m, srv, arg);

		} else if(arg[1].equalsIgnoreCase("int")){
			rint(m, srv, arg);

		} else if(arg[1].equalsIgnoreCase("long")){
			rlong(m, srv, arg);

		} else if(arg[1].equalsIgnoreCase("double")){
			rdouble(m, srv, arg);

		} else if(arg[1].equalsIgnoreCase("byte")){
			rbyte(m, srv, arg);

		} else if(arg[1].equalsIgnoreCase("short")){
			rshort(m, srv, arg);

		} else if(arg[1].equalsIgnoreCase("bool")){
			rbool(m, srv, arg);

		}
	}

	private void rbool(Message m, Server srv, String[] arg) {
		if(arg.length == 3){
			r.setSeed(Long.parseLong(arg[2]));
		}

		srv.send(m.channel, m.author, r.nextBoolean() +"", m.channel);
	}

	private void rshort(Message m, Server srv, String[] arg) {
		if(arg.length == 3){
			r.setSeed(Long.parseLong(arg[2]));
		}

		srv.send(m.channel, m.author, (short)r.nextInt() +"", m.channel);
	}

	private void rbyte(Message m, Server srv, String[] arg) {
		if(arg.length == 3){
			r.setSeed(Long.parseLong(arg[2]));
		}

		srv.send(m.channel, m.author, (byte)r.nextInt() +"", m.channel);
	}

	private void rdouble(Message m, Server srv, String[] arg) {
		if(arg.length == 3){
			r.setSeed(Long.parseLong(arg[2]));
		}

		srv.send(m.channel, m.author, r.nextDouble() +"", m.channel);
	}

	private void rlong(Message m, Server srv, String[] arg) {
		if(arg.length == 3){
			r.setSeed(Long.parseLong(arg[2]));
		}

		srv.send(m.channel, m.author, r.nextLong() +"", m.channel);
	}

	private void rint(Message m, Server srv, String[] arg) {
		if(arg.length == 3){
			r.setSeed(Long.parseLong(arg[2]));
		}

		srv.send(m.channel, m.author, r.nextInt() +"", m.channel);
	}

	private void string(Message m, Server srv, String[] arg) {
		if(arg.length == 2){
			srv.send(m.channel, m.author, getRandomString(20), m.channel);

		} else if(arg.length == 3){
			int l = Integer.parseInt(arg[2]);

			if(l <= 50){
				srv.send(m.channel, m.author, getRandomString(l), m.channel);

			} else {
				srv.send(m.channel, m.author, "I am not planning to make string "+ l +" characters long!", m.channel);
			}

		} else if(arg.length == 4){
			int l = Integer.parseInt(arg[2]);

			if(l <= 50){
				r.setSeed(Long.parseLong(arg[3]));
				srv.send(m.channel, m.author, getRandomString(l), m.channel);

			} else {
				srv.send(m.channel, m.author, "I am not planning to make string "+ l +" characters long!", m.channel);
			}
		}
	}

	/* generate a new String */
	private String getRandomString(int len) {
		final String gen = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		String ret = "";

		for(int i = 0;i < len;i ++){
			ret += gen.charAt(Math.abs(r.nextInt() % gen.length()));
		}

		return ret;
	}

	private void help(Message m, Server srv, String[] arg) {
		switch (arg[2].toLowerCase()) {
			case "help":
				srv.send(m.channel, m.author, "What are you doing with your life?", m.channel);
				break;

			case "string":
				srv.send(m.channel, m.author, "Usage: $random string [length] [seed]", m.channel);
				srv.send(m.channel, m.author, "Function: creates a random string with optional [length] and [seed]", m.channel);
				break;

			case "double":
				srv.send(m.channel, m.author, "Usage: $random double [seed]", m.channel);
				srv.send(m.channel, m.author, "Function: creates a random double with optional [seed]", m.channel);
				break;

			case "long":
				srv.send(m.channel, m.author, "Usage: $random long [seed]", m.channel);
				srv.send(m.channel, m.author, "Function: creates a random long with optional [seed]", m.channel);
				break;

			case "int":
				srv.send(m.channel, m.author, "Usage: $random int [seed]", m.channel);
				srv.send(m.channel, m.author, "Function: creates a random int with optional [seed]", m.channel);
				break;

			case "short":
				srv.send(m.channel, m.author, "Usage: $random short [seed]", m.channel);
				srv.send(m.channel, m.author, "Function: creates a random short with optional [seed]", m.channel);
				break;

			case "byte":
				srv.send(m.channel, m.author, "Usage: $random byte [seed]", m.channel);
				srv.send(m.channel, m.author, "Function: creates a random byte with optional [seed]", m.channel);
				break;

			case "bool":
				srv.send(m.channel, m.author, "Usage: $random bool [seed]", m.channel);
				srv.send(m.channel, m.author, "Function: creates a random boolean with optional [seed]", m.channel);
				break;
		}
	}

	private void help(Message m, Server srv) {
		srv.send(m.channel, m.author, "Available commands: string, bool, byte, short, int, long, double", m.channel);
		srv.send(m.channel, m.author, "Usage: $random help _command_", m.channel);
	}

	@Override
	public String[] reserved() {
		return new String[]{ "$random" };
	}
}
