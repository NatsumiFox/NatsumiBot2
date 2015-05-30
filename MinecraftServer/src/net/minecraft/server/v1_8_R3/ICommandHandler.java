package net.minecraft.server.v1_8_R3;

import java.util.List;
import java.util.Map;

public interface ICommandHandler {

    int a(ICommandListener icommandlistener, String s);

    List<String> a(ICommandListener icommandlistener, String s, BlockPosition blockposition);

    List<ICommand> a(ICommandListener icommandlistener);

    Map<String, ICommand> getCommands();
}
