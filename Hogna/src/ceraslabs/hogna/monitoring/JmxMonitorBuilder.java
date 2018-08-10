package ceraslabs.hogna.monitoring;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import Framework.Cloud.Topology.Node;

/**
 * <pre>{@code
 * <monitor name="BusyThreads" type="jmx" xmlns:jmx="ceraslabs.hogna.monitoring.JmxMonitorBuilder">
 *   <description>
 *     ...
 *   </description>
 *   <jmx:con-network>yorku-net<jmx:con-network/>
 *   <jmx:con-port>1092</jmx:con-port>
 *   <jmx:object-name>Catalina:type=ThreadPool,name=http-80</jmx:object-name>
 *   <jmx:object-attribute>currentThreadsBusy</jmx:object-attribute>
 * </monitor>
 * }</pre>
 * 
 * @author Cornel
 *
 */
@XmlRootElement(name="monitor")
public class JmxMonitorBuilder implements IMonitorBuilder
{
	@XmlAttribute(name="name")
	private String m_strName;
	@XmlElement(name="description")
	private String m_strDescription;
	@XmlElement(name="con-network", namespace="ceraslabs.hogna.monitoring.JmxMonitorBuilder")
	private String m_strNetwork;
	@XmlElement(name="con-port", namespace="ceraslabs.hogna.monitoring.JmxMonitorBuilder")
	private String m_strPort;
	@XmlElement(name="object-name", namespace="ceraslabs.hogna.monitoring.JmxMonitorBuilder")
	private String m_strObjectName;
	@XmlElement(name="object-attribute", namespace="ceraslabs.hogna.monitoring.JmxMonitorBuilder")
	private String m_strObjectAttribute;

	@Override
	public Monitor forInstance(Node theInstance)
	{
		JmxMonitor theMonitor = new JmxMonitor();
		
		theMonitor.withHost(theInstance.GetIpAddress(this.m_strNetwork))
		          .withCluster(theInstance.GetCluster().GetId())
		          .withPort(this.m_strPort)
		          .withObjectName(this.m_strObjectName)
		          .withObjectAttribute(this.m_strObjectAttribute)
		          .withName(this.m_strName)
		          .withDescription(this.m_strDescription);

		return theMonitor;
	}

	@Override
	public String GetName()
	{
		return this.m_strName;
	}
	
	public JmxMonitorBuilder withName(String strName)
	{
		this.m_strName = strName;
		return this;
	}
	
	public JmxMonitorBuilder withDescription(String strDescription)
	{
		this.m_strDescription = strDescription;
		return this;
	}

	public JmxMonitorBuilder withNetwork(String strNetwork)
	{
		this.m_strNetwork = strNetwork;
		return this;
	}
	public JmxMonitorBuilder withPort(String strPort)
	{
		this.m_strPort = strPort;
		return this;
	}
	public JmxMonitorBuilder withObjectName(String strObjectName)
	{
		this.m_strObjectName = strObjectName;
		return this;
	}
	public JmxMonitorBuilder withObjectAttribute(String strObjectAttribute)
	{
		this.m_strObjectAttribute = strObjectAttribute;
		return this;
	}
}
