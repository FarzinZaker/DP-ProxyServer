package ceraslabs.hogna.executor.commands;

import Framework.Cloud.Topology.Topology;


public class CloudScaleCommand extends Command
{
	public Topology m_topology = null;
	public String m_strClusterId = null;
	public int m_cntInstancesDelta = 1;

	public CloudScaleCommand(String strType)
	{
		super(strType);
	}
}
