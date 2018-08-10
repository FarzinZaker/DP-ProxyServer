package ProxyServer;

import java.net.URI;
import java.util.UUID;



public interface  IRequestFilter
{
	public boolean doFilter(URI uri, UUID uuid, String IP) throws Exception;
	public void onReply(UUID uuid, String IP) throws Exception;
	public void addRule(Rule rule);
	public void removeRule(Rule rule);
}
