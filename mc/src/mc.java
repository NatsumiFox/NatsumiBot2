import bot.nat.sumi.*;

import java.io.IOException;
import java.util.Arrays;

@SuppressWarnings("deprecation")
public class mc extends Module implements Closed {
    private final Minecraft instance;

	public mc(){
        instance = new Minecraft();
        Thread minecraft = new Thread(instance, "Minecraft");
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
		minecraft.start();

        Main.spec.add(0, this);
	}

	@Override
	public void command(Message m, Server srv) {
        if(!m.type.equals("PRIVMSG")){
            not(m, srv);

        } else if(m.text.startsWith(Main.cmd + "mc")){
            cmd(m, srv);

        } else if(Minecraft.Chan.equals(m.channel)) {
            instance.write("tellraw @a {text:\"<" + m.author + "> " + m.text + "\",color:\"gray\",hoverEvent:{action:\"show_text\",value:\"" + m.channel + "\"}}");
        }
	}

    private void not(final Message m, Server srv) {
        switch (m.type){
            case "JOIN":
                instance.write("tellraw @a {text:\""+ SpecialModules.getUser(m.author) +"\",color:\"white\",hoverEvent:{action:\"show_text\",value:\""+ m.channel.replace(":", "") +"\"},extra:[{text:\" joined the channel.\",color:\"white\"}]}");
                return;

            case "PART":
                instance.write("tellraw @a {text:\""+ SpecialModules.getUser(m.author) +"\",color:\"white\",hoverEvent:{action:\"show_text\",value:\""+ m.channel +"\"},extra:[{text:\" left the channel.\",color:\"white\",hoverEvent:{action:\"show_text\",value:\""+ m.text.replace(" :", "") +"\"}}]}");
                return;

            case "QUIT":
                instance.write("tellraw @a {text:\""+ SpecialModules.getUser(m.author) +"\",color:\"white\",extra:[{text:\" quit the channel.\",color:\"white\",hoverEvent:{action:\"show_text\",value:\""+ m.channel +" "+ m.text.replace(" :", "") +"\"}}]}");
                return;

            case "NICK":
                User u = srv.getUser(SpecialModules.getUser(m.author));
                instance.write("tellraw @a {text:\""+ SpecialModules.getUser(m.author) +" is now know as "+ m.channel +"!\",color:\"white\",hoverEvent:{action:\"show_text\",value:\""+ instance.getString(u) +"\"}}");
                return;
        }
    }

    private void cmd(Message m, Server srv) {
        String s = m.text.replace(Main.cmd +"mc ", "");

        switch (s.split(" ")[0]){
            case "help":
                help(s.replace("help", "").replace(" ", ""), m, srv);
                return;

            case "list":
                srv.send(m.channel, m.author, Arrays.toString(instance.players.toArray(new String[instance.players.size()])), m.channel);
                return;

            case "raw":
                if(m.author.equalsIgnoreCase("Natsumi") && srv.getUser(m.author).isBotOp) {
                    instance.write(s.replace("raw ", ""));

                } else {
                    srv.send(m.channel, m.author, "You are not permitted to do this!", m.channel);
                }
                return;

            default:
                help("", m, srv);
        }
    }

    private void help(String h, Message m, Server srv) {
        if(h.equals("")){
            srv.send(m.channel, m.author, "Available commands: help, list", m.channel);
            srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc help _command_", m.channel);

        } else {
            switch (h){
                case "help":
                    srv.send(m.channel, m.author, "Sorry, I can not help you!", m.channel);
                    return;

                case "list":
                    srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc list", m.channel);
                    srv.send(m.channel, m.author, "Lists all online users on target server", m.channel);
                    return;

                case "raw":
                    srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc raw _command_", m.channel);
                    srv.send(m.channel, m.author, "Send raw command to the console", m.channel);
                    return;

                default:
                    srv.send(m.channel, m.author, "Available commands: help, list, raw", m.channel);
                    srv.send(m.channel, m.author, "Usage: "+ Main.cmd +"mc help _command_", m.channel);
            }
        }
    }

    @Override
	public String[] reserved() {
		return new String[]{ "" };
	}

    @Override
    public void close() {
        try {
            instance.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

