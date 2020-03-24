package snd;

import java.nio.ByteBuffer;

public class TestProtocol implements Protocol {
	
	public static int id = 100275;
	
	private String mes;
	
	public void setMes(String m)
	{
		this.mes = m;
	}
	
	public void process()
	{
		System.out.println(this);
	}
	
	@Override
	public ByteBuffer buf() {
		
		byte[] mb = mes.getBytes();
		int size = mb.length;
		ByteBuffer bf = ByteBuffer.allocate(size + 8);		
		bf.putInt(id);
		bf.putInt(size);
		bf.put(mb);
		return bf;		
	}
	
	public static Protocol toProtocol(ByteBuffer bf)
	{
		int id = bf.getInt();
		if(id != TestProtocol.id)
			return null;
		
		int size = bf.getInt();
		
		TestProtocol tp = new TestProtocol();
		byte[] mb = new byte[size];
		bf.get(mb);
		tp.mes = new String(mb);
		return tp;
	}
	
	public static Protocol getProtocol(ByteBuffer bf)
	{
		int id = bf.getInt();
		if(id != TestProtocol.id)
			return null;
		
		int size = bf.getInt();
		
		TestProtocol tp = new TestProtocol();
		byte[] mb = new byte[size];
		bf.get(mb);
		tp.mes = new String(mb);
		return tp;
	}
	
	public String toString()
	{
		return id + "\t" + mes;
	}
	
	public static void main(String argv[])
	{
		TestProtocol test = new TestProtocol();
		TestProtocol.id = 10036;
		test.mes = "test my own";
		
		System.out.println(test);
		ByteBuffer bf = test.buf();
		
		bf.flip();
		System.out.println(bf);
		Protocol res = TestProtocol.toProtocol(bf);
		
		System.out.println(res);		
	}
	
}
