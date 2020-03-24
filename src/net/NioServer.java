package net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class NioServer extends NetRunnable {

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
}
