package snd;

import java.nio.ByteBuffer;

import helper.Logger;

public class SProtocol extends Protocol {

	static int id = 102;
	String msg;

	public SProtocol(String m)
	{
		msg = m;
	}
	
	@Override
	public RawData toRawData() {
		ByteBuffer bf = ByteBuffer.allocate(4+msg.length());
		bf.putInt(id);
		bf.put(msg.getBytes());
		
		return RawData.newRawData(bf);
	}

	@Override
	public void process() {
		Logger.log("I got mes from Server: " + msg);
	}

}
