package ProxyServer;

import java.net.URI;
import java.util.UUID;

public enum Actions
{
	BLOCK
	{
		boolean perfom(IServiceClassRequestObserver observer, URI request,UUID uuid, String IP)
		{
			return false;
		}
	},

	PREMIT
	{
		boolean perfom(IServiceClassRequestObserver observer, URI request,UUID uuid, String IP)
		{
			observer.onRequest(request,uuid,IP);
			return true;
		}
	},

	MONITOR
	{
		boolean perfom(IServiceClassRequestObserver observer, URI request,UUID uuid, String IP)
		{
			observer.onMonitorRequest(request);
			return false;
		}
	};

	// Do action op represented by this constant
	 abstract boolean perfom(IServiceClassRequestObserver observer, URI request,UUID uuid, String IP);

}
