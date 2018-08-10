package ceraslabs.hogna.monitoring;

import Framework.Cloud.Topology.Node;

public class SnmpMonitorBuilder implements IMonitorBuilder
{
	int m_timeout;
	int m_retries;
	
	String m_strOid;
	String m_strCommunity;
	String m_strNetwork;
	
	String m_strName;
	String m_strPort;
	String m_strDescription;

	@Override
	public Monitor forInstance(Node theInstance)
	{
		SnmpMonitor newMonitor = new SnmpMonitor();

		newMonitor.m_strName        = this.m_strName;
		newMonitor.m_strDescription = this.m_strDescription;

		newMonitor.m_strCluster   = theInstance.GetCluster().GetId();
		newMonitor.m_timeout      = this.m_timeout;
		newMonitor.m_retries      = this.m_retries;
		newMonitor.m_strCommunity = this.m_strCommunity;
		newMonitor.m_strOid       = this.m_strOid;
		newMonitor.m_strHost      = theInstance.GetIpAddress(this.m_strNetwork);
		newMonitor.m_strPort      = this.m_strPort;

		return newMonitor;
	}

	@Override
	public String GetName()
	{
		return m_strName;
	}
}
