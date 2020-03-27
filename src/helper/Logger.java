package helper;

import java.nio.ByteBuffer;

public class Logger {

	public static void log(String s)
	{
		System.out.println(s);
	}
	
	public static void log(byte[] bs) {
		System.out.println(new String(bs));
	}
	
	public static void log(ByteBuffer bb) {
		System.out.println("pos: " + bb.position() + "\tlim: " + bb.limit() + "\tcap: " + bb.capacity());
	}
	
}
