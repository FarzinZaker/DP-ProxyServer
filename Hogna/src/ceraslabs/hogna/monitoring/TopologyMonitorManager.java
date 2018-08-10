package ceraslabs.hogna.monitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ceraslabs.hogna.configuration.ConfigurationManager;
import ceraslabs.hogna.configuration.MonitorConfigurationSection;
import Framework.Cloud.Topology.Node;
import Framework.Cloud.Topology.Topology;

// Monitors a topology
// this class assumes that the configuration file has been loaded and the configuration file has a "monitoring" section.

public class TopologyMonitorManager  extends MonitorManager
{
	Map<String, IMonitorBuilder> m_mapMonitorBuilders = null;
	
	public TopologyMonitorManager()
	{
		this.m_mapMonitorBuilders = new HashMap<>();
		MonitorConfigurationSection secMonitors = (MonitorConfigurationSection)ConfigurationManager.GetSection("monitoring");
		IMonitorBuilder[] monBuilders = secMonitors.GetMonitorBuilders();
		for (IMonitorBuilder monBuilder : monBuilders)
		{
			this.m_mapMonitorBuilders.put(monBuilder.GetName(), monBuilder);
		}
	}
	
	public void UpdateMonitorList(Topology theTopology)
	{
		ArrayList<Monitor> lstMonitors = new ArrayList<>();
		
		for(Node node : theTopology.GetAllNodes())
		{
			String[] sMonitors = node.GetMonitorNames();
			if (sMonitors == null)
			{
				continue;
			}

			for (String sMonitor : sMonitors)
			{
				IMonitorBuilder builder = this.m_mapMonitorBuilders.get(sMonitor);
				if (builder != null)
				{
					Monitor theMonitor = builder.forInstance(node);
					lstMonitors.add(theMonitor);
				}
			}
		}
		Monitor[] theMonitors = lstMonitors.toArray(new Monitor[lstMonitors.size()]);
		this.SetMonitors(theMonitors);
	}
}
