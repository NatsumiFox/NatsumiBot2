package net.minecraft.server.v1_8_R3;

public class ExceptionInvalidSyntax extends CommandException {

    public ExceptionInvalidSyntax() {
        this("commands.generic.snytax", new Object[0]);
    }

    public ExceptionInvalidSyntax(String s, Object... aobject) {
        super(s, aobject);
    }
}
