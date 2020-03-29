package net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import helper.Logger;
import snd.RawData;

public class NioServer extends NetRunnable {
	private Conn  connection = null;

	public NioServer(String name, int p) {
		super(name);
	}

	public void open() {
		try {
			sel = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(byte[] bs)
	{
		if(this.connection !=  null)
			this.connection.send(RawData.wrap(bs));
	}
	
	public void close() {
		try {
			sel.wakeup();
			sel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Listener addListener(int p) {
		Listener l = new Listener(p);

		try {
			l.listen();
			l.getChannel().register(sel, SelectionKey.OP_ACCEPT, l);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return l;

	}

	@Override
	public void onAcceptable(SelectionKey k) throws IOException {
		SocketChannel channel = ((ServerSocketChannel) k.channel()).accept();
		Listener l = (Listener) k.attachment();
		Connectee ce = new Connectee(l);
		channel.configureBlocking(false);
		ce.attachKey(channel.register(sel, SelectionKey.OP_READ, ce));

		this.connection = ce;
		Logger.log(channel.getRemoteAddress() + "------> ");
		
	}

	@Override
	public void onConnectable(SelectionKey k) throws IOException {	
	}
}
