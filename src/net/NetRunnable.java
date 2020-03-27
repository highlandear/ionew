package net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
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

	private void onAcceptable(SelectionKey k) throws IOException {
		SocketChannel channel = ((ServerSocketChannel) k.channel()).accept();
		Listener l = (Listener) k.attachment();
		Connectee c = new Connectee(l);
		channel.configureBlocking(false);
		c.attachKey(channel.register(sel, SelectionKey.OP_READ, c));

		Logger.log(channel.getRemoteAddress() + "------> ");
	}

	private void onConnectable(SelectionKey k) throws IOException {
		SocketChannel ch = (SocketChannel) k.channel();
		Connector c = (Connector) k.attachment();
		c.attachKey(k);

		if (!ch.finishConnect())
			throw new IOException("cannot connect");
		k.interestOps(SelectionKey.OP_READ);

		Logger.log(ch.getRemoteAddress() + "：connect OK!");
	}

	private void onReadable(SelectionKey k) throws IOException {

		SocketChannel ch = (SocketChannel) k.channel();
		Conn conn = (Conn) k.attachment();
		ByteBuffer rb = conn.getReadBuffer();
		// synchronized (rb) {
		int num = ch.read(rb);
		if (num == -1) {
			System.out.println(ch.getRemoteAddress() + " --X--");
			ch.close();
			return;
		}
		/*
		if (!rb.hasRemaining()) {
			k.interestOps(k.interestOps() & ~SelectionKey.OP_READ);

		}
		*/
		// }
		/*
		if(rb.hasRemaining())
		{
			return;
		}
		else
		*/
		conn.collect();
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
			k.interestOps(k.interestOps() & ~SelectionKey.OP_WRITE);

		//	Logger.log(name + " send:{" + new String(wb.array()) + "}" + wb.limit());
		}
	}

	private void onClose(SelectionKey k) throws IOException {
		Conn c = (Conn) k.attachment();
		if (c == null)
			return;

		c.close();
	}

}
