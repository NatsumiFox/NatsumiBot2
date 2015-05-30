package net.minecraft.server.v1_8_R3;

public class ExceptionEntityNotFound extends CommandException {

    public ExceptionEntityNotFound() {
        this("commands.generic.entity.notFound", new Object[0]);
    }

    public ExceptionEntityNotFound(String s, Object... aobject) {
        super(s, aobject);
    }
}
