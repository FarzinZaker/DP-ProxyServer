package opera.KalmanFilter;

public class ModelParameter extends Variable
{
	String xPath;

	public ModelParameter()
	{
		super();
	}
	
	public ModelParameter(ModelParameter other)
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
