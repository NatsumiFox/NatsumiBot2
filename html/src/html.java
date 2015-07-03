import bot.nat.sumi.*;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class html extends Module {
	@Override
	public void command(Message m, Server srv) {
		URL[] url;

		try {
			if((url = getUrl(m.text)) != null){
				resolveURL(url, m.channel, srv);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/* checks if text contains url. if does returns all of them */
	private URL[] getUrl(String text) throws UnsupportedEncodingException {
		ArrayList<URL> urls = new ArrayList<>();

		/* loop for all text and pick up valid links */
		while(!text.equals("")){
			String curr = text;

			/* does text contain space? */
			if(curr.contains(" ") && curr.replace(" ", "").length() > 0){
				/* get until next space and then replace this text away */
				text = text.substring(curr.split(" ")[0].length() + 1, curr.length());
				curr = curr.split(" ")[0];

			} else {
				/* clear text */
				curr = text;
				text = "";
			}

			/* if text contains a period */
			if(curr.indexOf('.') != -1){
				try {
					/* if text does not contain http or https, add http manually */
					if(!curr.startsWith("http://") && !curr.startsWith("https://")){
						curr = "http://"+ curr;
					}

					/* make sure is a valid url */
					if(isValid(curr)) {
						/* test if you can make new URL */
						URL t = new URI(encodeURL(curr)).toURL();
						urls.add(t);
					}

				} catch (MalformedURLException | URISyntaxException e) {
					// this is not valid URL
					;
				}
			}
		}

		/* return the URL array */
		return urls.size() == 0 ? null : urls.toArray(new URL[urls.size()]);
	}

	/* is used to encode an URL */
	private String encodeURL(String url) throws MalformedURLException, UnsupportedEncodingException {
		URL u = new URL(new String(url.getBytes(Charset.forName("CP1252")), Charset.forName("UTF-8")));
	//	String ret = u.getProtocol() +"://"+ IDN.toASCII(u.getHost()) +(u.getPort() == -1 ? "/" : ":"+ u.getPort() +"/")+ URLEncoder.encode(u.getFile(), "UTF-8");
		String ret = u.getProtocol() +"://"+ IDN.toASCII(u.getHost()) + (u.getPort() == -1 ? "" : ":"+ u.getPort() )+ u.getFile();
		return ret.endsWith("/") ? ret.substring(0, ret.length() -1) : ret;
	}

	/* check if URL is valid url */
	private boolean isValid(String curr) {
		String t = curr.replace("https://", "").replace("http://", "");
		/* if String contains forward slash, only get data before it */
		if(t.contains("/")){
			t = t.split("/")[0];
		}

		String[] chk = t.split("\\.");

		/* check for all the parts so they only contain alphanumerics */
		for(String c : chk){
			/* if contains some non-alphanumerics or no letters at all, stop */
			if(!c.matches("^[a-zA-Z0-9]*$") && ((!(curr.contains("https://") || curr.contains("http://"))) && !c.matches(".*[a-zA-Z]+.*"))){
				return false;
			}

			/* if String is empty, return */
			if(c.length() == 0){
				return false;
			}
		}

		/* correct url */
		return chk.length != 0 && !t.endsWith(".") && !t.startsWith(".") && !t.contains("..");
	}

	/* return information about the URLs found */
	private void resolveURL(URL[] url, final String channel, final Server srv) {
		for(final URL u : url){
			final String rand = Math.random() +"";

			/* create new thread to read from */
			new Thread(() -> {
				InputStream is = null;

				try {
					File f = new File(Main.folder +"url"+ rand +".txt");
					String line;
					String data = "";
					is = u.openStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

					/* load the webpage */
					while ((line = br.readLine()) != null) {
						data += line +"\n";
						Main.write(f, line +"\n", true);
					}

					/* get content type */
					HttpURLConnection connection = (HttpURLConnection)  u.openConnection();
					connection.setRequestMethod("HEAD");
					connection.connect();
					String title = "";

					/* if has a title, load it */
					if(data.contains("<title>") && data.contains("</title>")){
						title = new String(data.substring(data.lastIndexOf("<title>") +"<title>".length(), data.lastIndexOf("</title>")).
								getBytes(Charset.forName("UTF-8")), Charset.forName("UTF-8"));

						/* if title is too long, cut it short */
						if(title.length() >= 75){
							title = title.substring(0, 72) +"...";
						}
					}
					
					srv.send("PRIVMSG "+ channel +
							" :url=\""+ getPureURL(u) +
							"\",size=\""+ getSize(data.length()) +
							"\",content_type=\""+ connection.getContentType() +
							"\""+ (connection.getLastModified() != 0 ? ",last_mod=\""+ lastMod(connection.getLastModified()) +"\"" : "") +
							(title.equals("") ? "" : (",title=\""+ title +"\"")), "HTTP");

				} catch (IOException e) {
					switch (e.toString().split(":")[0]) {
						case "java.net.UnknownHostException":
							srv.send("PRIVMSG " + channel + " :" + Format.BOLD.i + Format.RED.i + "URL not resolved: " + getPureURL(u), "HTTP");
							return;

						case "java.net.ConnectException":
							srv.send("PRIVMSG " + channel + " :" + Format.BOLD.i + Format.RED.i + "Connection timed out: " + getPureURL(u), "HTTP");
							return;

						case "java.io.FileNotFoundException":
							srv.send("PRIVMSG " + channel + " :" + Format.BOLD.i + Format.RED.i + "URL not found: " + getPureURL(u), "HTTP");
							return;

						case "java.net.SocketException":
							srv.send("PRIVMSG " + channel + " :" + Format.BOLD.i + Format.RED.i + "URL Exception (" + e.getLocalizedMessage() + "): " + getPureURL(u), "HTTP");
							return;


						default:
							srv.send("PRIVMSG " + channel + " :" + Format.BOLD.i + Format.RED.i + " Unknown exception: \"" + e.toString() + "\" for URL " + getPureURL(u), "HTTP");
					}
				}

				if (is != null) try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}, "URL resolver #"+ rand).start();
		}
	}

	private static final long SECOND =   1000;
	private static final long MINUTE =   60 * SECOND;
	private static final long HOUR =     60 * MINUTE;
	private static final long DAY =      24 * HOUR;
	private static final long WEEK =     7 * DAY;
	private static final long MONTH =    30 * DAY;
	private static final long YEAR =     365 * DAY;

	/* get info about when it was last modified */
	private String lastMod(long lm) {
		long t = System.currentTimeMillis() - lm;
		long y = t / YEAR, mo = t / MONTH % 12, wk = t / WEEK % 4, d = t / DAY % 7, hr = t / HOUR % 24, min = t / MINUTE % 60, sec = t / SECOND % 60;

		return (y != 0 ? y +" years and "+ mo +" months" : (mo != 0 ? mo +" months and "+ wk +" weeks" : (wk != 0 ? wk +" weeks and "+ d +" days" : (d != 0 ? d +" days and "+ hr +" hours" :
				(hr != 0 ? hr +" hours and "+ min +" minutes" : (min != 0 ? min +" minutes and "+ sec +" seconds" : sec +" seconds")))))) +" ago";
	}

	private String getPureURL(URL u) {
		return u.toString().replace("https//", "").replace("http://", "");
	}

	private String getSize(long size) {
		if (size < 1000) return size + " B";
		int exp = (int) (Math.log(size) / Math.log(1000));
		return String.format("%.2f %sB", size / Math.pow(1000, exp), "KMGTPE".charAt(exp - 1)).replace(",", ".");
	}

	@Override
	public String[] reserved() {
		return new String[]{ "" };
	}
}
