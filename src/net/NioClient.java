package net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

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

	@Override
	public void onConnectable(SelectionKey k) throws IOException {
		SocketChannel ch = (SocketChannel) k.channel();
		if (!ch.finishConnect())
			throw new IOException("cannot connect");
		
		ch.configureBlocking(false);
		Connector cr = (Connector) k.attachment();
		cr.attachKey(ch.register(sel, SelectionKey.OP_READ, cr));
	}

	@Override
	public void onAcceptable(SelectionKey k) throws IOException {		
	}
}
