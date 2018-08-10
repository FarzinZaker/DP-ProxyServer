package ceraslabs.hogna.workload;

import java.util.ArrayList;
import java.util.Random;

public abstract class Scenario
{
	protected String m_sName = "[default]";

	/**
	 * The number of milliseconds an user spends, on average, between two consecutive executions of the scenario.
	 */
	protected int m_thinkTime = 0;
	protected ArrayList<Parameter> m_parameters = new ArrayList<Parameter>();
	
	public void SetName(String sName) { this.m_sName = sName; }
	public String GetName() { return this.m_sName; }
	
	public void SetThinkTimeMean(int thinkTime)
	{
		// Assert(thinkTime > 0)
		this.m_thinkTime = thinkTime;
	}
	
	public int GetThinkTimeMean()
	{
		return this.m_thinkTime;
	}
	
	/**
	 * Computes a random value with normal distribution for think time, based on the average think time set.
	 * The value returned is at most 1/3 * average from the average.
	 * @return
	 */
	public int GetThinkTime()
	{
		//thinkTime = 0 - this.m_thinkTime * Math.log(1 - (new Random()).nextDouble()));
		//return (int)(2.0 / 3.0 * this.m_thinkTime * (1 + (new Random()).nextDouble()));
		//return (int)(new Random().nextDouble()* this.m_thinkTime);
		return (int)(this.m_thinkTime + (Math.random() - 0.3) * this.m_thinkTime);// +- 50%
	}
	
	public void AddParameter(Parameter param)
	{
		if (param != null)
			this.m_parameters.add(param);
	}
}
