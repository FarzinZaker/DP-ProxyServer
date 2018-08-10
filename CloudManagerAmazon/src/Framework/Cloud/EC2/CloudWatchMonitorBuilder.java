package Framework.Cloud.EC2;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import Framework.Cloud.Topology.Node;
import ceraslabs.hogna.monitoring.IMonitorBuilder;
import ceraslabs.hogna.monitoring.Monitor;

/**
 * <pre>{@code
 * <monitor name="cloud-watch.cpu-utilization" type="cloud-watch" xmlns:cw="ceraslabs.hogna.monitoring.CloudWatchMonitorBuilder">
 *   <description>
 *     ...
 *   </description>
 *   <cw:metric-name>CPUUtilization</cw:metric-name>
 *   <cw:cred-file>./config/AwsCredentials.properties</cw:cred-file>
 * </monitor>
 * }</pre>
 * 
 * @author Cornel
 *
 */
@XmlRootElement(name="monitor")
public class CloudWatchMonitorBuilder implements IMonitorBuilder
{
	@XmlAttribute(name="name")
	private String m_strName;
	@XmlElement(name="description")
	private String m_strDescription;
	@XmlElement(name="metric-name", namespace="ceraslabs.hogna.monitoring.CloudWatchMonitorBuilder")
	private String m_strMetricName;
	@XmlElement(name="cred-file", namespace="ceraslabs.hogna.monitoring.CloudWatchMonitorBuilder")
	private String m_strCredentialsFile;

	@Override
	public Monitor forInstance(Node theInstance)
	{
		CloudWatchMonitor theMonitor = new CloudWatchMonitor();
		
		theMonitor.withInstanceId(theInstance.GetId())
		          .withCluster(theInstance.GetCluster().GetId())
		          .withMetricName(this.m_strMetricName)
		          .withCredentialFile(this.m_strCredentialsFile)
		          .withName(this.m_strName)
		          .withDescription(this.m_strDescription);

		return theMonitor;
	}

	@Override
	public String GetName()
	{
		return this.m_strName;
	}
}
