package net;

/**
 * The client that Server can feel
 * @author hzs
 *
 */
public class Connectee extends Conn {
	private Listener listener = null;

	public Connectee(Listener lis) {
		this.listener = lis;
	}

	public Listener getLisener() {
		return listener;
	}
	
	@Override
	public String toString() {
		return "an connectee";
	}
}
