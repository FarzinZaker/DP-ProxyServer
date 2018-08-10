package ceraslabs.hogna.monitoring;

import Framework.Cloud.Topology.Node;

public class CorproMonitorBuilder implements IMonitorBuilder
{
	String m_strName;
	String m_strDescription;
	String m_strNetwork;
	String m_strPort;

	int m_timeout;
	int m_retires;
	
	public Monitor forInstance(String sHost, String sCluster)
	{
		CorproMonitor theMonitor = new CorproMonitor();
		
		theMonitor.m_strName = this.m_strName;
		theMonitor.m_strDescription = this.m_strDescription;
		theMonitor.m_strHost = sHost;
		theMonitor.m_strPort = this.m_strPort;
		theMonitor.m_strCluster = sCluster;
		
		return theMonitor;
	}

	@Override
	public Monitor forInstance(Node theInstance)
	{
		CorproMonitor theMonitor = new CorproMonitor();
		
		theMonitor.m_strName = this.m_strName;
		theMonitor.m_strDescription = this.m_strDescription;
		theMonitor.m_strHost = theInstance.GetIpAddress(this.m_strNetwork);
		theMonitor.m_strPort = this.m_strPort;
		theMonitor.m_strCluster = theInstance.GetCluster().GetId();
		
		return theMonitor;
	}

	@Override
	public String GetName()
	{
		return this.m_strName;
	}
}
