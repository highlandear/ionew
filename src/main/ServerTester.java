package main;

import helper.CmdInput;
import helper.Dosth;
import net.NioServer;

public class ServerTester implements Dosth {

	private CmdInput cmd = new CmdInput(this);

	private NioServer ns = new NioServer("AWBserver", 10075);;

	public void input() {
		cmd.input();
	}

	public void listen() {
		ns.open();
		ns.addListener(10075);
		ns.setRunning();
		new Thread(ns).start();
	}

	public void stop() {
		ns.stop();
	}

	@Override
	public void dosth(String str) {
		this.ns.send(str.getBytes());
	}

	public static void main(String[] args) {

		ServerTester s = new ServerTester();

		s.listen();

		s.input();

		s.stop();

	}

}
