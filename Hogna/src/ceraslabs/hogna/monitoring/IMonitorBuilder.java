package ceraslabs.hogna.monitoring;

import Framework.Cloud.Topology.Node;

public interface IMonitorBuilder
{
	public Monitor forInstance(Node theInstance);
	public String GetName();
}
