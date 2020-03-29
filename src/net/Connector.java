package net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * The Real client
 * @author hzs
 *
 */
public class Connector extends Conn {
	private SocketChannel channel = null;
	private String addr = null;
	private int port = 0;


	public SocketChannel getChannel() {
		return channel;
	}

	public Connector(String a, int p) {
		this.addr = a;
		this.port = p;
	}

	public void connect() throws IOException {
		channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(new InetSocketAddress(addr, port));
	}

	@Override
	public String toString() {
		return "connector to " + addr + ":" + port;
	}
}
