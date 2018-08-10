package ceraslabs.hogna.data;

public class MetricValue
{
	public String sResource;
	public String sMetricName;
	public double value;
	
	public MetricValue(String sResource, String sMetricName, double value)
	{
		this.sResource = sResource;
		this.sMetricName = sMetricName;
		this.value = value;
	}
}
