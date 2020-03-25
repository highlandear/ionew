package helper;

import java.util.Scanner;

public class CmdInput
{	
	private Dosth dosth;
	
	public CmdInput(Dosth d)
	{
		this.dosth = d;
	}
	
	public void input()
	{
		Scanner scan = new Scanner(System.in);
		scan.useDelimiter("\n");
		while (true) 
		{
			System.out.print("input:");
			String str = scan.next();
			if (null == str)
				break;

			str = str.replaceAll("\r", "");
			if (str.equals("quit") || str.equals("exit"))
			{
				System.out.print("exit!");
				break;
			}
			System.out.println("[" + str + "]");
			dosth.dosth(str);
		}
		scan.close();
	}	
}
