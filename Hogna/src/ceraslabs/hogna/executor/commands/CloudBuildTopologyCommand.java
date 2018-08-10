package ceraslabs.hogna.executor.commands;

import Framework.Cloud.Topology.Topology;

public class CloudBuildTopologyCommand extends Command
{
	public Topology m_topology = null;

	public CloudBuildTopologyCommand(String strType)
	{
		super(strType);
	}

}
