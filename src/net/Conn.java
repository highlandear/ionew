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
import snd.RawProtocol;

/**
 * Abstract Connection Entity
 * 
 * @author hzs
 *
 */
public abstract class Conn implements Runnable {

	volatile private SelectionKey selKey;
	private ByteBuffer rb = ByteBuffer.allocate(config.NetConfig.ALLOC_RECV_BUFF_SIZE);
	private Queue<ByteBuffer> wbs = new java.util.ArrayDeque<>();

	public static ThreadPoolExecutor pool = new ThreadPoolExecutor(TPoolConfig.THREAD_POOL_SIZE,
			TPoolConfig.THREAD_POOL_SIZE, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	private	Queue<byte[]> msgs =  new LinkedBlockingQueue<byte[]>();

	public void attachKey(SelectionKey k) throws SocketException {
		selKey = k;
		Socket socket = ((SocketChannel) k.channel()).socket();
		socket.setReceiveBufferSize(config.NetConfig.NIO_RECV_BUFF_SIZE);
		socket.setSendBufferSize(config.NetConfig.NIO_SEND_BUFF_SIZE);
		socket.setKeepAlive(true);
	}

	public void send(byte[] data) {
		ByteBuffer wb = ByteBuffer.allocate(data.length);
		wb.flip();
		wb.clear();
		wb.put(data);
		wb.flip();
		synchronized (wbs) {			
			wbs.add(wb);
		}

		enableWrite();
	}

	public void send(RawProtocol p) {
		this.send(p.toBytes());
	}

	public void enableWrite() {
		if (selKey == null) {
			System.out.println("key is null");
			return;
		}
		selKey.interestOps(SelectionKey.OP_WRITE);
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
			System.out.println("sel key is null");
			return;
		}
		selKey.channel().close();
	}

	@Override
	public void run() {
		while (!msgs.isEmpty()) {
			byte[] b = msgs.poll();
			ByteBuffer bf = ByteBuffer.wrap(b);
			System.out.println(new String(b));

			/*
			int ver = bf.getInt();
			System.out.print("ver:" + ver);
			*/
		}
	}

	public void collect(byte[] data) {
		msgs.add(data);
	//	System.out.println(new String(data));
		pool.execute(this);
	}
}
