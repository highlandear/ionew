package net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class NioClient extends NetRunnable {

	public NioClient(String n) {
		super(n);
	}
	
	public void open()
	{
		try {
			sel = Selector.open();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		try {
			sel.wakeup();
			sel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Connector addConnector(String a, int p) {
		Connector c = new Connector(a, p);

		try {
			c.connect();
			c.getChannel().register(sel, SelectionKey.OP_CONNECT, c);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return c;
	}

}
