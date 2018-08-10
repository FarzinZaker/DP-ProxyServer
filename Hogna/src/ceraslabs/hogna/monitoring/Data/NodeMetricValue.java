package ceraslabs.hogna.monitoring.Data;

public class NodeMetricValue
{
	double m_value;
	String m_metricName;
	String m_nodeId;
	long m_timestamp;
	
	public double GetMetricValue() { return this.m_value; }
	public String GetNodeId() { return this.m_nodeId; }
	public String GetMetricName() { return this.m_metricName; }
	public long GetTimestamp() {return this.m_timestamp; }
}
