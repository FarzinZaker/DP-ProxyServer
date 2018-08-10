package ceraslabs.hogna.monitoring;

import ceraslabs.hogna.data.MetricValue;

public abstract class Monitor
{
	protected String m_strName;
	protected String m_strDescription;
//	protected String m_cluster;
//	private String m_host;
//	private String m_port;

	public String  GetName()            { return this.m_strName; }
	public void    SetName(String name) { this.m_strName = name; }

	public String  GetDescription()                   { return this.m_strDescription; }
	public void    SetDescription(String description) { this.m_strDescription = description; }

//	public String GetCluster()               { return this.m_cluster; }
//	public void   SetCluster(String cluster) { this.m_cluster = cluster; }

//	public String GetHost()             { return this.m_host; }
//	public void   SetHost(String sHost) { this.m_host = sHost; }

//	public String GetPort()             { return this.m_port; }
//	public void   SetPort(String sPort) { this.m_port = sPort; }
	
//	public void SetTarget(Node theTarget)
//	{
//		this.m_cluster = theTarget.GetCluster().GetId();
//		this.m_host = theTarget.GetIpAddress("public");
//	}
	
	public Monitor withName(String sName)
	{
		this.SetName(sName);
		return this;
	}
	
	public Monitor withDescription(String sDescription)
	{
		this.SetDescription(sDescription);
		return this;
	}
	
//	public Monitor withHost(String sHost)
//	{
//		this.SetHost(sHost);
//		return this;
//	}
	
//	public Monitor withPort(String sPort)
//	{
//		this.SetPort(sPort);
//		return this;
//	}
	
//	public Monitor withCluster(String sCluster)
//	{
//		this.SetCluster(sCluster);
//		return this;
//	}
	
//	protected void UptdateClone(Monitor clone)
//	{
//		clone.m_host = this.m_host;
//		clone.m_port = this.m_port;
//		clone.m_name = this.m_name;
//		clone.m_description = this.m_description;
//	}

//	public abstract Monitor Clone();

	public abstract double GetValue();
	
	public abstract MetricValue[] GetValues();
}
