package net;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import config.TPoolConfig;
import helper.Logger;
import snd.Message;
import snd.Protocol;
import snd.RawData;

/**
 * Abstract Connection Entity
 * 
 * @author hzs
 *
 */
public abstract class Conn implements Runnable {
	public static ThreadPoolExecutor pool = new ThreadPoolExecutor(TPoolConfig.THREAD_POOL_SIZE,
			TPoolConfig.THREAD_POOL_SIZE, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	volatile private SelectionKey selKey;

	private Queue<ByteBuffer> wbs = new java.util.ArrayDeque<>();
	private ByteBuffer rb = ByteBuffer.allocate(config.NetConfig.ALLOC_RECV_BUFF_SIZE);
	private ByteBuffer hold = ByteBuffer.allocate(0);
	private ByteBuffer pack = null;

	public void attachKey(SelectionKey k) throws SocketException {
		selKey = k;
		Socket socket = ((SocketChannel) k.channel()).socket();
		socket.setReceiveBufferSize(config.NetConfig.NIO_RECV_BUFF_SIZE);
		socket.setSendBufferSize(config.NetConfig.NIO_SEND_BUFF_SIZE);
		socket.setKeepAlive(true);
	}

	public void send(byte[] data) {
		ByteBuffer wb = ByteBuffer.wrap(data);
		synchronized (wbs) {
			wbs.add(wb);
		}
		enableWrite();
	}

	public void send(RawData p) {
		this.send(p.getSendData());
	}

	public void send(Protocol p) {
		this.send(p.toRawData());
	}

	public void enableWrite() {
		if (selKey == null) {
			// Logger.log("key is null");
			return;
		}
		selKey.interestOps(selKey.interestOps() | SelectionKey.OP_WRITE);
		selKey.selector().wakeup();
	}

	public Queue<ByteBuffer> getWriteBuffer() {
		return wbs;
	}

	public ByteBuffer getReadBuffer() {
		return rb;
	}

	public void close() throws IOException {
		if (selKey == null) {
			// Logger.log("key is null");
			return;
		}
		selKey.channel().close();
	}

	@Override
	public void run() {
		synchronized (hold) {
			this.decode(hold);
		}
	}

	public void collect() {
		rb.flip();
		byte[] bs = new byte[rb.limit()];
		rb.get(bs);
		rb.clear();

		synchronized (hold) {
			hold = ByteBuffer.wrap(bs);
		}

		pool.execute(this);
	}

	public void decode(ByteBuffer bb) {
		if (pack == null)
			pack = bb;
		else if (pack.remaining() < bb.limit())
			pack = (ByteBuffer) ByteBuffer.allocate(pack.limit() + bb.limit()).put((ByteBuffer) pack.flip()).put(bb)
					.flip();

		while (pack.remaining() >= 4) {
			int sz = pack.getInt();
			if (pack.remaining() < sz) {
				pack.rewind();
				pack.compact();
				return;
			}

			byte[] dst = new byte[sz];
			pack.get(dst);
			Message.decode(this, dst);
		}
		if (pack.hasRemaining())
			pack.compact();
		else
			pack = null;
	}

}
