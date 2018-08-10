package ProxyServer.RequestObserver;

import java.util.ArrayList;
import java.util.HashMap;
import ProxyServer.User;

/**
 * Stores the metrics for a Service Class.
 * 
 * @author Cornel
 *
 */
public class ServiceClassMetrics
{
	/**
	 * The name of the Service Class
	 */
	String name = null;
	
	/**
	 * The number of requests in the service class that were filtered
	 * (not forwarded by the proxy). 
	 */
	int reqFiltered = 0;
	
	/**
	 * The number of requests in the service class that were not filtered
	 * (forwarded by the proxy).
	 */
	int reqUnfiltered = 0;
	
	/**
	 * A list with the response time for each request in the class service
	 * (clculated when the request got its reply).
	 */
	ArrayList<Integer> responseTime = new ArrayList<Integer>();
	HashMap<String, User> users=new HashMap<String, User>() ;
	
	/**
	 * Creates a new instance to store metrics for the service class
	 * with the name <code>name</code>
	 */

	ServiceClassMetrics(String name)
	{
		this.name = name;
	}
	
	public void copyObject(ServiceClassMetrics scm){
		this.responseTime.clear();
		this.users.clear();
		
		this.name = scm.name;
		this.reqFiltered = scm.reqFiltered;
		this.reqUnfiltered = scm.reqUnfiltered;
		for (Integer integer : scm.responseTime) {
			Integer tempInt = new Integer(integer);
			this.responseTime.add(tempInt);
		}
		
		for(String key: scm.users.keySet()){
			User usr = new User();
			usr.copyObject(scm.users.get(key));
			this.users.put(key, usr);
		}
	    
	}
}
