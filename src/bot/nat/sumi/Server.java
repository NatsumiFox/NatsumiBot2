package bot.nat.sumi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
	private boolean KeepConnection = true;
	private Socket conn;
	private ArrayList<Channel> ch;
	private ArrayList<User> users;
	private static final int TIMEOUT = 5000;

	public User nick;
	public final String baseNick;
	public final String password;
	public String IP;
	public int port;
	public final String cfgName;

	private int connAttempts = 0;

	public Server(ConfigFile cfg) {
		/* get config name */
		String[] adr = cfg.getFile().replace("\\", "/").split("/");
		cfgName = adr[adr.length - 1].split("\\.")[0];

		/* resolve nickname */
		password = cfg.getField("Nickname password").getValue();
		nick = new User(cfg.getField("Preferred nickname").getValue());
		baseNick = nick.name;   // created to use new nick on connection restart

		/* resolve port and IP */
		IP = cfg.getField("Server address").getValue();
		port = Integer.parseInt(cfg.getField("Server port").getValue());

		/* create channel and user list */
		ch = new ArrayList<>();
		users = new ArrayList<>();
		users.add(nick);

		/* load channels to memory */
		loadChannels(cfg.getField("Join Channels").getValue().split(" "));

		/* create main loop */
		new Timer("server "+ IP +":"+ port).schedule(new TimerTask() {
			@Override
			public void run() {
				connectLoop();
				send("USER "+ nick.name +" 0 0 :" +"IRC bot by Natsumi", "");
				nickDo("");

				while(KeepConnection) {
					chkConn();
					if(chkAvailable()) {
						parseMessage(readMessageFull());
					}

					sleep(10);
				}
			}
		}, 10);
	}

	/* parse read messages */
	private void parseMessage(String msg) {
		for(String ln : msg.split("\r\n")){
			System.out.println(ln);
			String[] data = ln.split(" ");

			if(data[0].equals("PING")){
				send("PONG "+ data[1], "PINGPONG");

			} else if(data[1].equals("PRIVMSG")){
				for(Module mod : Main.modules){
					if(hasCommand(mod.reserved(), data[3].replace(":", ""))){
						final Module run = mod;
						final Message message = new Message(data[0].replace(":", "").split("!")[0], data[1], data[2], ln.replace(data[0] +" "+ data[1] +" "+ data[2] +" :", ""));
						final Server srv = this;

						Thread t = new Thread("server "+ IP +" "+ data[2]){
							@Override
							public void run(){
								run.command(message, srv);
							}
						};
						t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
							@Override
							public void uncaughtException(Thread t, Throwable e) {
								send("PRIVMSG "+ t.getName().split(" ")[2] +" :"+ Format.BOLD + Format.Color.RED + e.toString(), "ERROR");
								e.printStackTrace();
							}
						});
						t.start();
					}
				}

			} else {
				for(Module mod : Main.spec){
					if(hasCommand(mod.reserved(), data[1])){
						final Module run = mod;
						final Message message = new Message(data[0], data[1], data[2], ln.replace(data[0] +" "+ data[1] +" "+ data[2], ""));
						final Server srv = this;

						new Thread("server "+ IP +" <<<"){
							@Override
							public void run(){
								run.command(message, srv);
							}
						}.start();
					}
				}
			}
		}
	}

	/* if module contains the command */
	private boolean hasCommand(String[] lst, String cmd) {
		for(String c : lst){
			if(c.equals("")){
				return true;
			}

			if(cmd.toLowerCase().startsWith(c) && c.startsWith(cmd.toLowerCase())){
				return !cmd.equals("");
			}
		}

		return false;
	}

	/* read next message */
	private String readMessageFull() {
		String ret = "";
		while(chkAvailable()){
			byte[] in = new byte[512];

			try {
				conn.getInputStream().read(in);
				ret += new String(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return ret.replace("\0", "");
	}

	/* check if anything would come */
	private boolean chkAvailable() {
		try {
			return conn.getInputStream().available() != 0;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/* check if connected */
	private void chkConn() {
		if(conn.isClosed()) {
			connectLoop();
			send("USER " + nick.name + " 0 0 :" + "IRC bot by Natsumi", "");
			nickDo("");
		}
	}

	/* attempt next nickname */
	public void nextNick() {
		connAttempts ++;
		nickDo(String.valueOf(connAttempts));
	}

	/* send nick */
	public void nickDo(String n) {
		send("NICK " + baseNick + n, "");
	}

	/* send text to server */
	public boolean send(String msg, String sender) {
		try {   // attempt to write message to the output stream
			conn.getOutputStream().write((msg +"\r\n").getBytes());
			System.out.println(":::"+ sender +" Sent: "+ msg);
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/* send text to server */
	public boolean send(String channel, String user, String msg, String sender) {
		try {   // attempt to write message to the output stream
			String message;
			if(channel.startsWith("#")){
				message = "PRIVMSG "+ channel +" :"+ user +": "+ msg;

			} else {
				message = "PRIVMSG "+ user +" :"+ msg;
			}

			conn.getOutputStream().write((message +"\r\n").getBytes());
			System.out.println(":::"+ sender +" Sent: "+ message);
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/* attempt to create a connection */
	private void connectLoop() {
		while(!connect()){
			System.out.println(":::Connection error. Trying again in 1 second");
			sleep(1000);
		}
	}

	/* connect to server */
	private boolean connect() {
		try {
			conn = new Socket();
			conn.connect(new InetSocketAddress(IP, port), TIMEOUT);
			System.out.println(":::Successfully connected to "+ IP +" port "+ port);
			connAttempts = 0;
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/* load channels */
	private void loadChannels(String[] names) {
		for(String n : names){
			ch.add(new Channel(n));
		}
	}

	/* wait for a time */
	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/* join loaded channels */
	public void joinChannels() {
		for(Channel c : ch){
			send("JOIN "+ c.name, "");
		}
	}

	/* get name from name */
	public Channel getChannel(String chan) {
		for(Channel c : ch){
			if(c.name.equalsIgnoreCase(chan)){
				return c;
			}
		}

		return null;
	}

	/* get all channels */
	public Channel[] getChannels() {
		return ch.toArray(new Channel[ch.size()]);
	}

	/* add user */
	public void addUser(User user) {
		if(getUser(user.name) == null){
			users.add(user);
		}
	}

	/* get User from name */
	public User getUser(String user) {
		for(User u : users){
			if(u.name.equalsIgnoreCase(user)){
				return u;
			}
		}

		return null;
	}

	/* remove user with name */
	public void rmvUser(String user) {
		User u = getUser(user);

		if(u != null){
			users.remove(u);
		}
	}

	/* get all Users */
	public User[] getUsers() {
		return users.toArray(new User[users.size()]);
	}

	/* get all Modules */
	public Module[] getModules() {
		return Main.modules.toArray(new Module[Main.modules.size()]);
	}

	/* get all Modules */
	public void reloadModules() {
        try {
            Main.modules = null;
            Main.loadJARs();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

	/* unload module */
	public boolean unload(String jar) {
		try {
			Main.unload(jar);
			return true;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return false;
	}
}
