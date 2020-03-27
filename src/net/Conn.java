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
import snd.RawProtocol;

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
	private ByteBuffer rb = ByteBuffer.allocate(config.NetConfig.ALLOC_RECV_BUFF_SIZE);
	private Queue<ByteBuffer> wbs = new java.util.ArrayDeque<>();
	private ByteBuffer packs = ByteBuffer.allocate(10240);

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

	public void send(RawProtocol p) {
		this.send(p.getSendData());
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

	//@Override
	public void run() {
		synchronized (packs) {
			packs.flip();
			while (packs.remaining() >= 4) {
				int sz = packs.getInt();
				
				if(sz <= 0)
				{
					return;
				}
				
				if (packs.remaining() < sz) {
					packs.rewind();					
					break;
				}

				byte[] dst = new byte[sz];
				packs.get(dst);
				RawProtocol rp =RawProtocol.wrap(dst);
				Logger.log(rp.toString());
			
			}
			packs.compact();
		}
	}

	public void collect() {
		rb.flip();
		byte[] bs = new byte[rb.limit()];
		rb.get(bs);
		rb.clear();

		synchronized (packs) {
			packs.put(bs);
		}

		pool.execute(this);

	}
}
