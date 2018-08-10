package ProxyServer.RequestObserver;

import java.net.URI;
import java.util.UUID;

/**
 * Represents single request that came to the proxy.
 * 
 * @author Cornel
 *
 */
public class ProxyRequest
{
	/**
	 * An identifier for this request.
	 */
	UUID uuid;
	
	/**
	 * The URI that has been requested.
	 */
	URI uri;
	
	/**
	 * A timestamp representing when the request came.
	 */
	long timestamp;
	String IPaddress;
}
