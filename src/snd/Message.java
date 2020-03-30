package snd;

import java.nio.ByteBuffer;

import net.Conn;

public class Message {

	
	public static void decode(Conn peer, byte [] data)
	{
	
		Protocol p = null;
		ByteBuffer bf = ByteBuffer.wrap(data);
		int id = bf.getInt();
		
		byte [] bs = new byte[bf.remaining()];
		bf.get(bs);
		if(id == 101)
		{
			p = new CProtocol(new String(bs));
			p.setPeer(peer);			
		}
		else if(id == 102)
		{
			p = new SProtocol(new String(bs));
			p.setPeer(peer);
		}
		
		peer.execute(p);
	}
	
}
