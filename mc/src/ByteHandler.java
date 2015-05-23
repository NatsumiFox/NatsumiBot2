import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ByteHandler {
	private ArrayList<Byte> bytes;

	public ByteHandler(){
		bytes = new ArrayList<>();
	}

	public ByteHandler var(int i){
		while((i & -128) != 0) {
			add((byte)(i & 127 | 128));
			i >>>= 7;
		}

		add((byte)i);
		return this;
	}

	public ByteHandler var(int i, int pos){
		while((i & -128) != 0) {
			bytes.add(pos, (byte) (i & 127 | 128));
			i >>>= 7;
			pos ++;
		}

		bytes.add(pos, (byte) i);
		return this;
	}

	public ByteHandler add(long i){
		bytes.add(bytes.size(), (byte) (i & 0xFF));
		bytes.add(bytes.size(), (byte) (i & 0xFF00));
		bytes.add(bytes.size(), (byte) (i & 0xFF0000));
		bytes.add(bytes.size(), (byte) (i & 0xFF000000));
		bytes.add(bytes.size(), (byte) (i & 0xFF00000000L));
		bytes.add(bytes.size(), (byte) (i & 0xFF0000000000L));
		bytes.add(bytes.size(), (byte) (i & 0xFF000000000000L));
		bytes.add(bytes.size(), (byte) (i & 0xFF00000000000000L));
		return this;
	}

	public ByteHandler add(int i){
		bytes.add(bytes.size(), (byte) (i & 0xFF));
		bytes.add(bytes.size(), (byte) (i & 0xFF00));
		bytes.add(bytes.size(), (byte) (i & 0xFF0000));
		bytes.add(bytes.size(), (byte) (i & 0xFF000000));
		return this;
	}

	public ByteHandler add0(byte i){
		bytes.add(0, i);

		return this;
	}

	public ByteHandler add0(int i){
		bytes.add(0, (byte) (i & 0xFF));
		bytes.add(1, (byte) (i & 0xFF00));
		bytes.add(2, (byte) (i & 0xFF0000));
		bytes.add(3, (byte) (i & 0xFF000000));

		return this;
	}

	public ByteHandler add(short i){
		bytes.add(bytes.size(), (byte) (i & 0xFF));
		bytes.add(bytes.size(), (byte) (i & 0xFF00));

		return this;
	}

	public ByteHandler add(byte i){
		bytes.add(bytes.size(), i);

		return this;
	}

	public ByteHandler add(String s){
		try {
			var((byte) s.length());
			for(byte b : s.getBytes("UTF-8")){
				add(b);
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return this;
	}

	public ByteHandler add(byte[] in) {
		for(byte b : in){
			add(b);
		}

		return this;
	}

	public ByteHandler rmv(int pos){
		bytes.remove(pos);

		return this;
	}

	public byte[] form(PacketID id) {
		var(id.val, 0).var((byte) bytes.size(), 0);
		return toArray();
	}

	/* form array from bytes */
	public byte[] toArray() {
		byte[] r = new byte[bytes.size()];

		for(int i = 0;i < bytes.size();i ++){
			r[i] = bytes.get(i);
		}

		return r;
	}

	public ByteHandler del0() {
		for(int i = bytes.size() -1;i >= 0;i --){
			if(bytes.get(i) != 0){
				break;
			}

			bytes.remove(bytes.size() - 1);
		}

		return this;
	}
}
