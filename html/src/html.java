import bot.nat.sumi.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class html extends Module {
	@Override
	public void command(Message m, Server srv) {
		URL[] url;

		if((url = getUrl(m.text)) != null){
			resolveURL(url, m.channel, srv);
		}
	}

	/* checks if text contains url. if does returns all of them */
	private URL[] getUrl(String text) {
		ArrayList<URL> urls = new ArrayList<>();

		/* loop for all text and pick up valid links */
		while(!text.equals("")){
			String curr = text;

			/* does text contain space? */
			if(curr.contains(" ")){
				/* get until next space and then replace this text away */
				curr = curr.split(" ")[0];
				text = text.replace(curr +" ", "");

			} else {
				/* clear text */
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
						URL t = new URL(curr);
						urls.add(t);
					}

				} catch (MalformedURLException e) {
					// this is not valid URL
					;
				}
			}
		}

		/* return the URL array */
		return urls.size() == 0 ? null : urls.toArray(new URL[urls.size()]);
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
			/* if contains some non-alphanumerics, stop */
			if(!c.matches("^[a-zA-Z0-9]*$")){
				return false;
			}

			/* if String is empty, return */
			if(c.length() == 0){
				return false;
			}
		}

		/* correct url */
		return chk.length != 0;
	}

	/* return information about the URLs found */
	private void resolveURL(URL[] url, final String channel, final Server srv) {
		for(final URL u : url){
			final String rand = Math.random() +"";

			/* create new thread to read from */
			new Thread(() -> {
				InputStream is;

				try {
					File f = new File(Main.folder +"url"+ rand +".txt");
					String line;
					String data = "";
					is = u.openStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(is));

					/* load the webpage */
					while ((line = br.readLine()) != null) {
						data += line +"\n";
						Main.write(f, line +"\n", false);
					}

					/* get content type */
					HttpURLConnection connection = (HttpURLConnection)  u.openConnection();
					connection.setRequestMethod("HEAD");
					connection.connect();
					String title = "";

					/* if has a title, load it */
					if(data.contains("<title>") && data.contains("</title>")){
						title = data.substring(data.lastIndexOf("<title>") + "<title>".length(), data.lastIndexOf("</title>"));
						
						/* if title is too long, cut it short */
						if(title.length() >= 75){
							title = title.substring(0, 72) +"...";
						}
					}
					
					srv.send("PRIVMSG "+ channel +
							" :url=\""+ getPureURL(u) +
							"\",size=\""+ getSize(data.length()) +
							"\",content_type=\""+ connection.getContentType() +
							"\""+ (title.equals("") ? "" : (",title=\""+ title +"\"")), "HTTP");

				} catch (IOException e) {
					switch (e.toString().split(":")[0]){
						case "java.net.UnknownHostException":
							srv.send("PRIVMSG "+ channel +" :"+ Format.BOLD.i + Format.RED.i +"URL not resolved: "+ getPureURL(u), "HTTP");
							return;

						case "java.net.ConnectException":
							srv.send("PRIVMSG "+ channel +" :"+ Format.BOLD.i + Format.RED.i +"Connection timed out: "+ getPureURL(u), "HTTP");
							return;

						default:
							srv.send("PRIVMSG "+ channel +" :"+ Format.BOLD.i + Format.RED.i + e.toString(), "HTTP");
					}
				}

			}, "URL resolver #"+ rand).start();
		}
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
