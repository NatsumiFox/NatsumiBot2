
public enum PacketID {
	JoinGame(1), ChatMessage(2), TimeUpdate(3), Health(6), Confirm(0x32), Disconnect(0x40),					// clientbound
	HandShake(0), KeepAlive(0), ChatMessageSrv(1), ConfirmSrv(0x0F), ClientSets(0x15), ClientStatus(0x16),	// serverbound
	Request(0), Ping(1), Response(0), Pong(1),
	DisconnectThis(0), LoginSuccess(2), LoginStart(0);


	public final byte val;
	PacketID(int i) {
		val = (byte) i;
	}
}
