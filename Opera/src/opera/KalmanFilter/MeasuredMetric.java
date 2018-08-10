package opera.KalmanFilter;


public class MeasuredMetric extends Variable
{
	public MeasuredMetric()
	{
		super();
	}

	public MeasuredMetric(MeasuredMetric other)
	{
		super(other);
	}

	public String toString()
	{
		return String.format("%s : %9.4f", this.name, this.value);
	}
}
