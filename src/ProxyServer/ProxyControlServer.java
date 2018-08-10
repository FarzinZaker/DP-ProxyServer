package ProxyServer;

import java.net.ServerSocket;
import java.net.Socket;


public class ProxyControlServer extends Thread
{
	private IRequestFilter theRequestFilter = null;
	private ServerSocket server;
	private int m_port = 7777;
	 
	public ProxyControlServer(int port)
	{
		this.setName("Thread - Control Server");

		
		this.m_port = port;
		try
		{
			server = new ServerSocket(m_port);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		//
		// The server do a loop here to accept all connection initiated by the
		// client application.
		//
		while (true)
		{
			try
			{
				Socket socket = server.accept();
				new ProxyControlConnectionHandler(socket, this.theRequestFilter);
			}
			catch (Exception e) {e.printStackTrace(); }
		}
	}
	
	public void SetRequestFilter(IRequestFilter filter)
	{
		this.theRequestFilter = filter;
	}
}
