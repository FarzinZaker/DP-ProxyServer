package ProxyServer;

import java.net.URI;
import java.security.InvalidParameterException;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

public class RequestFilter implements  IRequestFilter
{
	private IServiceClassRequestObserver theObserver;
	private Map<String,Rule> rulesMap;

	public RequestFilter(IServiceClassRequestObserver observer)
	{
		setObserver(observer);
		rulesMap = new Hashtable<String,Rule>();
	}

	private Actions analyze(URI request)
	{
		for (Rule rule : rulesMap.values())
		{
			if (rule.getPattern().matcher(request.toString()).matches())
			{
				return rule.getAction();
			}
		}
		return Actions.PREMIT;
	}


	/**
	 * @param engine
	 *            the engine to set
	 */
	private void setObserver(IServiceClassRequestObserver observer)
	{
		if (observer == null)
			throw new InvalidParameterException(
					"IServiceClassRequestObserver must be not null");

		this.theObserver = observer;
	}

	@Override
	public void addRule(Rule rule)
	{
		String key = rule.getPattern().pattern();
		rulesMap.put(key,rule);
	}

	@Override
	public void removeRule(Rule rule)
	{
		String key = rule.getPattern().pattern();
		rulesMap.remove(key);
	}

	@Override
	public synchronized boolean doFilter(URI request, UUID uuid, String IP) throws Exception
	{	
		if (analyze(request).perfom(theObserver, request, uuid, IP))
		{	
			return true;
		}
		return false;
	}

	@Override
	public synchronized void onReply(UUID uuid,String IP)
			throws Exception
	{
		theObserver.onReply(uuid,IP);
	}
}
