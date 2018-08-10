package ceraslabs.hogna.configuration;

import org.w3c.dom.Element;

import Framework.Cloud.Topology.Topology;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class TopologyConfigurationSection extends ConfigurationSection
{
	Topology m_topology = null;
	@Override
	protected void ParseSection(Element elemSection)
	{
		Trace.WriteLine(TraceLevel.DEBUG, "Parsing section [%s].", elemSection.getNodeName());
		
		this.m_topology = Topology.CreateInstance(elemSection);
		this.m_topology.BuildTemplateNodes();
	}

	public Topology GetTopology()
	{
		return this.m_topology;
	}
}
