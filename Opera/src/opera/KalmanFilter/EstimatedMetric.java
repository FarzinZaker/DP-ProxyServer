package opera.KalmanFilter;

public class EstimatedMetric extends Variable
{
	String xPath;

	public EstimatedMetric()
	{
		super();
	}
	
	public EstimatedMetric(EstimatedMetric other)
	{
		super(other);
		this.xPath = other.xPath;
	}

	public String getXPath()
	{
		return this.xPath;
	}
	public void setXPath(String xPath)
	{
		this.xPath = xPath;
	}
}
