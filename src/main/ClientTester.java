package main;

import helper.CmdInput;
import helper.Dosth;
import net.Connector;
import net.NioClient;
import snd.CProtocol;

public class ClientTester implements Dosth {
	
	private NioClient nc = new NioClient("HZSClient");
	
	private Connector connector = null;
	
	public void waitInput()
	{
		new CmdInput(this).input();
	}
	
	public void connect()
	{
		nc.setRunning();
		nc.open();		
		connector = nc.addConnector("127.0.0.1", 10075);
		new Thread(nc).start();
	}
	
	public void disconnect()
	{
		nc.stop();
		connector = null;
	}

	@Override
	public void dosth(String str) {
		if(str.equals(":/lk"))
		{
			this.connect();
			return;
		}
		if(str.equals(":/ulk"))
		{
			this.disconnect();
			return;
		}
		if(connector == null)
			return;

		CProtocol cp = new CProtocol(str);
		connector.send(cp);
	}
	
	public static void main(String[] args) {
		new ClientTester().waitInput();
	}

}
