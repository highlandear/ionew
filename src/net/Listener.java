package net;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

/**
 * the Listener of Server used to listen
 * @author hzs
 *
 */
public class Listener extends Conn {

	private int port = 0;
	private ServerSocketChannel channel = null;

	public ServerSocketChannel getChannel() {
		return channel;
	}

	public Listener(int p) {
		this.port = p;
	}

	public void listen() throws IOException {
		channel = ServerSocketChannel.open();
		channel.bind(new java.net.InetSocketAddress(port));
		channel.configureBlocking(false);
	}

	@Override
	public String toString() {
		return "listen from:" + port;
	}

}
