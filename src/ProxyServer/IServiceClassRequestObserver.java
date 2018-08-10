package ProxyServer;

/**
 * An observer for requests at the proxy that classifies the requests in
 * classes of service. The classes of service can be dinamicly added and removed.
 * 
 * @author Cornel
 *
 */
public interface IServiceClassRequestObserver extends IRequestObserver
{
	/**
	 * Add a new service class to the observer.
	 * 
	 * @param name
	 * 				The name of the new Service Class.
	 * @param pattern
	 * 				The pattern to identify the requests that belong
	 * 				to this service class.
	 */
	public void AddServiceClass(String name, String pattern);

	/**
	 * Remove a service class from the observer. If the Service Class
	 * does not exist, this method should do nothing.
	 * 
	 * @param name
	 * 				The name of the service class to remove.
	 */
	public void RemoveServiceClass(String name);
}
