import bot.nat.sumi.Main;
import bot.nat.sumi.Server;
import bot.nat.sumi.Servers;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Minecraft implements Runnable {
	public static Server srv;
	public final static String srvIP = "irc.badnik.net";
	public final static String Chan =  "#bored";

	public final static String IP = "localhost";
	public final static short port = 25565;
	public final static String nick = "IRC";
	public final static String pass = "";
	
	public final static byte protocol = 47;
	private static final int TIMEOUT = 5000;

	private Socket conn;
	private boolean KeepConnection = true;

	@Override
	public void run() {
		new Timer("GetServerMinecraft").schedule(new TimerTask() {
			@Override
			public void run() {
				srv = Servers.get(IP);
			}
		}, 10);

		connectLoop();
		while(KeepConnection) {
			chkConn();
			if(chkAvailable()) {
				parseMessage(readMessageFull());
			}
			
			sleep(10);
		}
	}

	/* perform handshake with the server */
	private void handShake() {
		send(PacketID.HandShake, new ByteHandler().add(protocol).add(IP).add(port).add((byte)2));

		/* login */
		send(PacketID.LoginStart, new ByteHandler().add(nick));
	}

	private void send(PacketID id, ByteHandler b) {
		try {
			byte[] s = b.form(id);
			conn.getOutputStream().write(s);
			System.out.println(":::Minecraft send: " + Arrays.toString(s));
			System.out.println(":::Minecraft send: " + new String(s).replace("\0", ""));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void parseMessage(byte[] b) {
		final ByteReader br = new ByteReader(b);

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (br.size() != 0) {
					parse(br.extract(br.rmvvar()));
                    sleep(1);
				}
			}

			private void parse(ByteReader br) {
				if (br.size() == 0) {
					return;
				}

				try {
                    System.out.print(": " + Integer.toHexString(br.var(0)[0]) + " ");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                switch (br.rmvvar()){
					case 0x00:
				//		send(PacketID.KeepAlive, new ByteHandler().add(br.toArray()));
				}
			}
		}, "MessageParserMinecraft").start();
	}
	
	/* read next message */
	private byte[] readMessageFull() {
		ByteHandler b = new ByteHandler();
		while(chkAvailable()){
			byte[] in = new byte[512];
			
			try {
				conn.getInputStream().read(in);
				b.add(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return b.del0().toArray();
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
		}
	}
	
	/* attempt to create a connection */
	private void connectLoop() {
		while(!connect()){
			System.out.println(":::Minecraft: Connection error. Trying again in 1 second");
			sleep(1000);
		}

		handShake();
	}

	/* connect to server */
	private boolean connect() {
		try {
			conn = new Socket();
			conn.connect(new InetSocketAddress(IP, port), TIMEOUT);
			System.out.println(":::Minecraft: Successfully connected to "+ IP +" port "+ port);
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/* wait for a time */
	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void finalize() throws Throwable {
		super.finalize();
		KeepConnection = false;
		conn.close();
		System.out.println(":::Minecraft: Connection closed.");
	}
}
