package ceraslabs.hogna.monitoring;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import ceraslabs.hogna.data.MetricValue;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class JmxMonitor extends Monitor
{
	String m_strObjectName;
	String m_strObjectAttribute;

	String m_strHost;
	String m_strPort;
	String m_strCluster;

	public JmxMonitor withHost(String strHost)
	{
		this.m_strHost = strHost;
		return this;
	}
	public JmxMonitor withPort(String strPort)
	{
		this.m_strPort = strPort;
		return this;
	}
	public JmxMonitor withCluster(String strCluster)
	{
		this.m_strCluster = strCluster;
		return this;
	}
	public JmxMonitor withObjectName(String strObjectName)
	{
		this.m_strObjectName = strObjectName;
		return this;
	}
	public JmxMonitor withObjectAttribute(String strObjectAttribute)
	{
		this.m_strObjectAttribute = strObjectAttribute;
		return this;
	}

	public String GetObjectName()                     { return this.m_strObjectName; }
	public void   SetObjectName(String strObjectName) { this.m_strObjectName = strObjectName; }
	
	public String GetObjectAttribute()                        { return this.m_strObjectAttribute; }
	public void SetObjectAttribute(String strObjectAttribute) { this.m_strObjectAttribute = strObjectAttribute; }
	
	@Override
	public double GetValue()
	{
		String sServiceUrl = String.format("service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi", this.m_strHost, this.m_strPort);
		JMXConnector serviceConnector = null;

		try
		{
			JMXServiceURL serviceUrl = new JMXServiceURL(sServiceUrl);
			serviceConnector = JMXConnectorFactory.connect(serviceUrl);
			MBeanServerConnection beanServer = serviceConnector.getMBeanServerConnection();

			ObjectName objectName = new ObjectName(this.m_strObjectName);
			Object value = beanServer.getAttribute(objectName, this.m_strObjectAttribute);
			return Double.parseDouble(value.toString());
		}
		catch (Exception ex)
		{
			Trace.WriteLine(TraceLevel.ERROR, "Monitor [%s]: Unknown error.\n\tError message: [%s].", this.GetName(), ex.getLocalizedMessage());
		}
		finally
		{
			if (null != serviceConnector)
			{
				try { serviceConnector.close(); }
				catch (Exception ex)
				{
					Trace.WriteLine(TraceLevel.ERROR, "Monitor [%s]: Error while closing the connection to server.\n\tError message: [%s].", this.GetName(), ex.getLocalizedMessage());
				}
			}
		}

		return Double.NaN;
	}

	@Override
	public MetricValue[] GetValues()
	{
		MetricValue value = new MetricValue(this.m_strCluster + "/" + this.m_strHost, this.GetName(), this.GetValue());
		return new MetricValue[] { value };
	}
}
