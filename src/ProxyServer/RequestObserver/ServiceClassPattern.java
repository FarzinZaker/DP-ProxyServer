package ProxyServer.RequestObserver;

import java.util.regex.Pattern;

/**
 * Stores a pattern to identify a Service Class.
 * 
 * @author Cornel
 *
 */
public class ServiceClassPattern
{
	/**
	 * The name of the Service Class
	 */
	String name = null;

	/**
	 * The pattern that identify a request in this Service Class.
	 */
	Pattern pattern = null;

	/**
	 * Creates a new Service Class pattern. 
	 * 
	 * @param name
	 * 				The name of the service class
	 * @param pattern
	 * 				A regular expression that identifies the service class.
	 */
	public ServiceClassPattern(String name, String pattern)
	{
		this.name = name;
		this.pattern = Pattern.compile(pattern);
	}

	/**
	 * Set the pattern for the Service Class. If the service class
	 * already has a pattern set, the pattern will be overriden.
	 * 
	 * @param pattern
	 * 				The new pattern for the service class.
	 */
	public void SetPattern(String pattern)
	{
		this.pattern = Pattern.compile(pattern);
	}

	/**
	 * Set the pattern for the Service Class. If the service class
	 * already has a pattern set, the pattern will be overriden.
	 * 
	 * @param pattern
	 * 				The new pattern for the service class.
	 */
	public void SetPattern(Pattern pattern)
	{
		this.pattern = pattern;
	}
}
