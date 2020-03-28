package snd;

import java.nio.ByteBuffer;

public class RawProtocol {
	private byte[] data;

	public RawProtocol(byte[] data) {
		this.data = data;
	}

	public String toString() {
		return "size:" + data.length + "[" +  new String(data) + "]";
	}

	public ByteBuffer buff() {

		ByteBuffer bf = ByteBuffer.allocate(data.length + 4);
		bf.putInt(data.length);
		bf.put(data);
		return bf;
	}

	public static RawProtocol bf2Raw(ByteBuffer bf) {

		bf.flip();
		int sz = bf.getInt();
		byte[] d = new byte[sz];
		bf.get(d);

		return new RawProtocol(d);
	}

	public static RawProtocol wrap(byte [] bs) {
		return new RawProtocol(bs);
	}
	
	public byte[] getSendData() {

		return buff().array();
	}

	public static void main(String[] args) {
		String msg = "testme";
		RawProtocol a = new RawProtocol(msg.getBytes());

		ByteBuffer bf = a.buff();
		System.out.println("a:\t" + a);
		RawProtocol b = RawProtocol.bf2Raw(bf);
		System.out.println("b:\t" + b);
	}

}
