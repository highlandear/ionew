package snd;

import java.nio.ByteBuffer;

public interface Protocol {
	
	public ByteBuffer buf();
	
	public void process();
}
