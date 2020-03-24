package net;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Abstract Connection Entity
 * @author hzs
 *
 */
public abstract class Conn {

	volatile private SelectionKey selKey;	
	private ByteBuffer readbuff = ByteBuffer.allocate(config.NetConfig.ALLOC_RECV_BUFF_SIZE);
	private ByteBuffer writebuff = null;

	public void attachKey(SelectionKey k) throws SocketException {
		selKey = k;		
		Socket socket = ((SocketChannel) k.channel()).socket();
		socket.setReceiveBufferSize(config.NetConfig.NIO_RECV_BUFF_SIZE);
		socket.setSendBufferSize(config.NetConfig.NIO_SEND_BUFF_SIZE);
		socket.setKeepAlive(true);
	}

	public void send(byte[] data) {
		writebuff = ByteBuffer.allocate(data.length);
		writebuff.flip();
		writebuff.clear();
		writebuff.put(data);

		enableWrite();
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
		return writebuff;
	}

	public ByteBuffer getReadBuffer() {
		return readbuff;
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
}
