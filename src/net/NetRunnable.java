package net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;

import helper.Logger;

/**
 * @author hzs
 *
 */
public abstract class NetRunnable implements Runnable {

	private String name;

	volatile private boolean running = true;

	protected Selector sel = null;

	public NetRunnable(String n) {
		this.name = n;
	}

	public String getName() {
		return name;
	}

	public void setRunning() {
		running = true;
	}

	public void stop() {
		running = false;
		sel.wakeup();
	}
	abstract public void onAcceptable(SelectionKey k) throws IOException;
	
	abstract public void onConnectable(SelectionKey k) throws IOException;
	@Override
	public void run() {
		Logger.log(name + " _______RUNNING_____");
		while (running) {
			try {
				sel.selectedKeys().clear();
				sel.select();
				for (SelectionKey k : sel.selectedKeys()) {
					if (k.isAcceptable()) {
						onAcceptable(k);
					} else if (k.isConnectable()) {
						onConnectable(k);
					} else if (k.isReadable()) {
						try {
							onReadable(k);
						} catch (IOException e) {
							// e.printStackTrace();
							onBadRead(k);
						}
					} else if (k.isWritable()) {
						try {
							onWritable(k);
						} catch (IOException e) {
							e.printStackTrace();
							onClose(k);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (!running) {
					try {
						for (SelectionKey key : sel.keys())
							onClose(key);
						sel.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		Logger.log("_____NET_STOP____");
	}


	
	private void onReadable(SelectionKey k) throws IOException {

		SocketChannel ch = (SocketChannel) k.channel();
		Conn conn = (Conn) k.attachment();
		ByteBuffer rb = conn.getReadBuffer();

		int num = ch.read(rb);
		if (num == -1) {
			Logger.log(ch.getRemoteAddress() + "disconnected x");
			ch.close();
			return;
		}

		Logger.log("read: " + new String(rb.array()));
		conn.collect();
		rb.clear();
		k.interestOps(SelectionKey.OP_READ);
	}

	private void onBadRead(SelectionKey k) throws IOException {
		SocketChannel ch = (SocketChannel) k.channel();
		Logger.log(ch.getRemoteAddress() + "：connect OVER!");
		onClose(k);
	}

	private void onWritable(SelectionKey k) throws IOException {
		SocketChannel channel = (SocketChannel) k.channel();
		Conn conn = (Conn) k.attachment();
		Queue<ByteBuffer> wbs = conn.getWriteBuffer();

		synchronized (wbs) {
			if (!wbs.isEmpty()) {
				ByteBuffer [] bs = wbs.toArray(new ByteBuffer[0]);	
				channel.write(bs);
				wbs.clear();
			}
		}
		k.interestOps(k.interestOps() & ~SelectionKey.OP_WRITE);
	}

	private void onClose(SelectionKey k) throws IOException {
		Conn c = (Conn) k.attachment();
		if (c == null)
			return;

		c.close();
	}

}
