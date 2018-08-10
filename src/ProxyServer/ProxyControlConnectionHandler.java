package ProxyServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import ProxyServer.Monitoring.DataSample;
import ProxyServer.Monitoring.MonitoredDatastore;

public class ProxyControlConnectionHandler implements Runnable
{
	private Socket socket;
	private IRequestFilter theRequestFilter = null;
	 
	public ProxyControlConnectionHandler(Socket socket, IRequestFilter filter)
	{
		this.socket = socket;
		this.theRequestFilter = filter;
		Thread t = new Thread(this);
		t.start();
	}
	
	private Object CommandGetMonitoredData(String sMessage)
	{
		// get the last sample
		MonitoredDatastore theStore = MonitoredDatastore.GetDatastore();
		DataSample theSample = theStore.GetLastSample();

		return theSample;
	}
	
	private Object CommandGetMonitoredDataWindow(String sMessage)
	{
		Object messageResult = null;
		int windowSize = 0;
		try
		{
			windowSize = Integer.parseInt(sMessage.substring(16).trim());

			MonitoredDatastore theStore = MonitoredDatastore.GetDatastore();
			DataSample[] theSamples = theStore.GetWindowSamples(windowSize);
			messageResult = theSamples;
		}
		catch (Exception ex)
		{
			messageResult = "ERROR: Incorrect window size.";
		}
		return messageResult;
	}
	
	private Object CommandAddServiceClass(String sMessage)
	{
		
		return "ERROR: Not Implemented";
	}

	private Object CommandRemoveServiceClass(String sMessage)
	{
		
		return "ERROR: Not Implemented";
	}
	private Object CommandAddFilterRule(String sMessage)
	{
		if (this.theRequestFilter != null)
		{
			String pattern = sMessage.substring(16).trim();
			this.theRequestFilter.addRule(new Rule(pattern, Actions.MONITOR));
			return "200 OK";
		}
		return "201 No Filter";
	}
	private Object CommandRemoveFilterRule(String sMessage)
	{
		if (this.theRequestFilter != null)
		{
			String pattern = sMessage.substring(19).trim();
			this.theRequestFilter.removeRule(new Rule(pattern, Actions.PREMIT));
			return "200 OK";
		}
		return "201 No Filter";
	}

	public void run()
	{
		try
		{
			//
			// Read a message sent by client application
			//
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			String message = (String) ois.readObject();

			//
			// Send a response information to the client application
			//
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			
			if (message.equals("GET DATA"))
			{
				Object messageResponse = this.CommandGetMonitoredData(message);
				oos.writeObject(messageResponse);
			}
			else if (message.startsWith("GET DATA WINDOW:"))
			{
				Object messageResponse = this.CommandGetMonitoredDataWindow(message);
				oos.writeObject(messageResponse);
			}
			else if (message.startsWith("ADD SERVICE CLASS:"))
			{
				Object messageResponse = this.CommandAddServiceClass(message);
				oos.writeObject(messageResponse);
			}
			else if (message.startsWith("REMOVE SERVICE CLASS:"))
			{
				Object messageResponse = this.CommandRemoveServiceClass(message);
				oos.writeObject(messageResponse);
			}
			else if (message.startsWith("ADD FILTER RULE:"))
			{
				Object messageResponse = this.CommandAddFilterRule(message);
				oos.writeObject(messageResponse);
			}
			else if (message.startsWith("REMOVE FILTER RULE:"))
			{
				Object messageResponse = this.CommandRemoveFilterRule(message);
				oos.writeObject(messageResponse);
			}
			else
			{
				oos.writeObject("ERROR: Unknown request.");
			}
			ois.close();
			oos.close();
			socket.close();

			//System.out.println("Server: Waiting for client message...");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}