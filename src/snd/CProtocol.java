package snd;

import java.nio.ByteBuffer;

import helper.Logger;

public class CProtocol extends Protocol {
	static int id = 101;
	String msg;

	public CProtocol(String m)
	{
		msg = m;
	}
	
	@Override
	public RawData toRawData() {
		ByteBuffer bf = ByteBuffer.allocate(4+msg.length());
		bf.putInt(id);
		bf.put(msg.getBytes());
		bf.flip();
		return RawData.newRawData(bf);
	}

	@Override
	public void process() {
		Logger.log("i got some msg from Client: " +  msg + " and i send back som info");
		this.getPeer().send(new SProtocol("info from Server"));
	}

}
