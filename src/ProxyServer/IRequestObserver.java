package ProxyServer;

import java.net.URI;
import java.util.UUID;

/**
 * Interface that has to be implemented by classes that want to be notified
 * when the proxy receives requests and send back the reply.
 * 
 * @author Cornel
 *
 */
public interface IRequestObserver
{
	/**
	 * A notification that a new request has arrived at the proxy.
	 * The request will be forwarded.
	 * 
	 * @param requestUri
	 * 				The resource that has been requested.
	 * @param uuid
	 * 				An identifier associated with this request.
	 * 				Later, this identifier will be used to notify the observer
	 * 				that the request is finished processing.
	 */
	public void onRequest(URI requestUri, UUID uuid, String IP);
	
	/**
	 * A notification that a new request has arrived to the proxy.
	 * The request will <b>not</b> be forwarded.
	 * 
	 * @param requestUri
	 * 				The resource that has been requested.
	 */
	public void onMonitorRequest(URI requestUri);

	/**
	 * A notification that a request is finished processing.
	 * 
	 * @param uuid
	 * 				The identifier of the request that is finished processing.
	 */
	public void onReply(UUID uuid, String IP);
}
