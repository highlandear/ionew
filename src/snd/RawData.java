package snd;

import java.nio.ByteBuffer;

public class RawData {

	private ByteBuffer bf;

	private RawData(ByteBuffer bf) {
		this.bf = bf;
	}

	private RawData(byte[] data) {
		bf = ByteBuffer.allocate(data.length + 4);
		bf.putInt(data.length);
		bf.put(data);
	}

	public static RawData wrap(byte[] bs) {
		return new RawData(ByteBuffer.wrap(bs));
	}

	public static RawData newRawData(ByteBuffer bf) {
		return new RawData(bf.array());
	}

	public byte[] getSendData() {

		return bf.array();
	}

	public static void main(String[] args) {

	}

}
