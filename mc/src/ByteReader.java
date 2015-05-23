import java.io.IOException;
import java.util.ArrayList;

public class ByteReader {
	private ArrayList<Byte> bytes;

	public ByteReader(byte[] b){
		bytes = new ArrayList<>();

		for(byte bs : b){
			bytes.add(bs);
		}
	}

	public int[] var(int pos) throws IOException {
		int ret = 0;
		int size = 0;

		do {
			byte b;
			if(((b = bytes.get(pos + size)) & 128) != 128) {
				return new int[]{ ret, size + 1 };   // make sure works

			}

			ret |= (b & 127) << size++ * 7;
		} while(size <= 5);

		throw new IOException("Varint too big");
	}

	public int rmvvar(){
		try {
			int[] r = var(0);
			for(int i = 0;i < r[1];i ++){
				bytes.remove(0);
			}

			return r[0];

		} catch (IOException e) {
			e.printStackTrace();
		}

		return -1;
	}

	public int readByte(int pos){
		return bytes.get(pos);
	}

	public int size() {
		return bytes.size();
	}

	public ByteReader extract(int len) {
		if(bytes.size() > 0) {
			byte[] b = new byte[len];

			for (int i = 0; i < len; i++) {
				b[i] = bytes.get(0);
				bytes.remove(0);
			}

			return new ByteReader(b);
		}

		return new ByteReader(new byte[0]);
	}

	public byte[] toArray() {
		byte[] r = new byte[bytes.size()];

		for(int i = 0;i < bytes.size();i ++){
			r[i] = bytes.get(i);
		}

		return r;
	}
}
