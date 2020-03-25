package net;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import config.TPoolConfig;

/**
 * Abstract Connection Entity
 * @author hzs
 *
 */
public abstract class Conn implements Runnable {

	volatile private SelectionKey selKey;	
	private ByteBuffer rb = ByteBuffer.allocate(config.NetConfig.ALLOC_RECV_BUFF_SIZE);
	private ByteBuffer wb = ByteBuffer.allocate(0);
	public static ThreadPoolExecutor pool  = new ThreadPoolExecutor(TPoolConfig.THREAD_POOL_SIZE, TPoolConfig.THREAD_POOL_SIZE, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	public void attachKey(SelectionKey k) throws SocketException {
		selKey = k;		
		Socket socket = ((SocketChannel) k.channel()).socket();
		socket.setReceiveBufferSize(config.NetConfig.NIO_RECV_BUFF_SIZE);
		socket.setSendBufferSize(config.NetConfig.NIO_SEND_BUFF_SIZE);
		socket.setKeepAlive(true);
	}

	public void send(byte[] data) {
		synchronized(wb) {
			wb = ByteBuffer.allocate(data.length);
			wb.flip();
			wb.clear();
			wb.put(data);
		}

		enableWrite();
	}
	
	public byte[] recv()
	{
		byte [] dst = null;
		synchronized(rb) {
			dst = new byte[rb.limit()];
			rb.get(dst, 0, rb.limit());
		}
		return dst;
	}
	
	public ByteBuffer rev()
	{
		ByteBuffer b;
		synchronized(rb)
		{
			b= rb.duplicate();
		}
		
		return b;
	}

	public void enableWrite() {
		if (selKey == null) {
			System.out.println("key is null");
			return;
		}
		selKey.interestOps(SelectionKey.OP_WRITE);
		selKey.selector().wakeup();
	}

	public ByteBuffer getWriteBuffer() {
		return wb;
	}

	public ByteBuffer getReadBuffer() {
		return rb;
	}
	
	public void close() throws IOException
	{
		if(selKey == null)
		{
			System.out.println("sel key is null");
			return;
		}
		selKey.channel().close();
		
	}
	
	public void addPool()
	{
		pool.execute(this);
	}
	
	@Override
	public void run() 
	{
	//	ByteBuffer res = rev();
		System.out.println("##############:[" + new String(recv()) + "] " );
	}
}
