package snd;

import net.Conn;

public abstract class Protocol implements Runnable {
	private net.Conn peer;

	public abstract RawData toRawData();

	public abstract void process();

	public Conn getPeer() {
		return peer;
	}

	void setPeer(Conn p) {
		this.peer = p;
	}

	@Override
	public void run() {
		this.process();
	}

}
