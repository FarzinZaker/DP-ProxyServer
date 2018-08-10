package ceraslabs.hogna.workload;

import java.util.Random;

public class ParameterInt extends Parameter
{
	int m_min;
	int m_max;

	public ParameterInt(String name, int min, int max)
	{
		this.m_sName = name;
		this.m_min = min;
		this.m_max = max;
	}

	public String GetRandomValue()
	{
		Random rand = new Random();
		return Integer.toString(rand.nextInt(this.m_max - this.m_min) + this.m_min);
	}
}

