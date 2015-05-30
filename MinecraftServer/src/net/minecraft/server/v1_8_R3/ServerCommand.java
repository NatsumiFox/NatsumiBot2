package net.minecraft.server.v1_8_R3;

public class ServerCommand {

    public final String command;
    public final ICommandListener source;

    public ServerCommand(String s, ICommandListener icommandlistener) {
        this.command = s;
        this.source = icommandlistener;
    }
}
