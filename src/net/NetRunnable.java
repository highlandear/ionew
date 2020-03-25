package net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;

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
		System.out.println(name + " _______RUNNING_____");

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
		System.out.println("_____NET_STOP____");
	}

	private void onAcceptable(SelectionKey k) throws IOException {
		SocketChannel channel = ((ServerSocketChannel) k.channel()).accept();
		Listener l = (Listener) k.attachment();
		Connectee c = new Connectee(l);
		channel.configureBlocking(false);
		c.attachKey(channel.register(sel, SelectionKey.OP_READ, c));

		System.out.println(channel.getRemoteAddress() + "------> ");
		System.out.println("rev buff size:" + channel.socket().getReceiveBufferSize());
	}

	private void onConnectable(SelectionKey k) throws IOException {
		SocketChannel ch = (SocketChannel) k.channel();
		Connector c = (Connector) k.attachment();
		c.attachKey(k);

		if (!ch.finishConnect())
			throw new IOException("cannot connect");
		k.interestOps(SelectionKey.OP_READ);

		System.out.println(ch.getRemoteAddress() + "：connect OK!");
	}

	private void onReadable(SelectionKey k) throws IOException {

		SocketChannel ch = (SocketChannel) k.channel();
		Conn conn = (Conn) k.attachment();
		ByteBuffer rb = conn.getReadBuffer();
		// synchronized (rb) {
		rb.clear();
		int num = ch.read(rb);
		rb.flip();
		if (num == -1) {
			System.out.println(ch.getRemoteAddress() + " --X--");
			ch.close();
			return;
		}
		// }

		conn.collect(toNewBytes(rb));
		k.interestOps(SelectionKey.OP_READ);
	}

	private byte[] toNewBytes(ByteBuffer bb) {
		byte[] dst = new byte[bb.limit()];
		bb.get(dst, 0, bb.limit());

		return dst;
	}

	private void onBadRead(SelectionKey k) throws IOException {
		SocketChannel ch = (SocketChannel) k.channel();
		System.out.println(ch.getRemoteAddress() + "：connect OVER!");
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

	//		System.out.println(name + " send:{" + new String(wb.array()) + "}" + wb.limit());
		}
	}

	private void onClose(SelectionKey k) throws IOException {
		Conn c = (Conn) k.attachment();
		if (c == null)
			return;

		c.close();
	}

}
