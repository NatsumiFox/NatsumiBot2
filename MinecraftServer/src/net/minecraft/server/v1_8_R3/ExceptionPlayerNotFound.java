package net.minecraft.server.v1_8_R3;

public class ExceptionPlayerNotFound extends CommandException {

    public ExceptionPlayerNotFound() {
        this("commands.generic.player.notFound", new Object[0]);
    }

    public ExceptionPlayerNotFound(String s, Object... aobject) {
        super(s, aobject);
    }
}
