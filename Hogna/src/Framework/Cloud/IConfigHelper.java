package Framework.Cloud;

import java.util.List;

import Framework.Cloud.Topology.Node;

public interface IConfigHelper
{
	/**
	 * This method is executed after the instance has started,
	 * and a service needs to be configured.
	 * 
	 * This method should take care of installing any necessary software
	 * on the instance.
	 * 
	 * @param node
	 */
	void Configure(Node node);
	void ConfigureVxlan(int index);
	
	/**
	 * Add a dependency from the service running on <code>depFrom</code> to
	 * another service running on <code>depTo</code>.
	 * 
	 * @param depFrom
	 * @param depTo
	 */
	void AddDependency(Node depFrom, List<Node> depTo);
	void RemoveDependency(Node depFrom, List<Node> depTo);
}
