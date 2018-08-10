import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;


public class ControlServerClient
{

	private static void SendCommand(String sCommand) throws Exception
	{
		InetAddress host = InetAddress.getByName("54.242.157.133");
		Socket socket = new Socket(host.getHostName(), 9300);

		//
		// Send a message to the server application
		//
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		oos.writeObject(sCommand);

		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		Object message = ois.readObject();
		System.out.println(message);
		
		oos.close();
		ois.close();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
//		SendCommand("ADD SERVICE CLASS: pi 0  \t  .*pi");
//		SendCommand("REMOVE SERVICE CLASS: pi 0  \t  .*pi");
		int i = 0;
		while (true)
		{
			++i;
			Thread.sleep(1000);
			SendCommand("GET DATA");
			
			if (i % 10 == 0)
			{
				SendCommand("ADD FILTER RULE: .*pi\\?digits=12&iterations=(9|10)\\d{4}");
			}
			else if (i % 5 == 0)
			{
				SendCommand("REMOVE FILTER RULE: .*pi\\?digits=12&iterations=(9|10)\\d{4}");
			}
		}
//		SendCommand("GET DATA WINDOW: 3600");
	}
}
