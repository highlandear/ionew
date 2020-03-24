package main;

import helper.CmdInput;
import helper.Dosth;
import net.Connector;
import net.NioClient;
import snd.TestProtocol;

public class ClientTester implements Dosth {
	
	private CmdInput cmd = new CmdInput(this);
	
	private NioClient nc = new NioClient("HZSClient");
	
	private Connector connector = null;
	
	public void input()
	{
		cmd.input();
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
		
		connector.send(str.getBytes());
	}
	
	public static void main(String[] args) {
		ClientTester c = new ClientTester();	
		
	//	c.connect();
		
		c.input();
		
	//	c.disconnect();
		
	}

}
