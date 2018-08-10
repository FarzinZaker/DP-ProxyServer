package ceraslabs.hogna.workload;

public abstract class Parameter
{
	protected String m_sName = "";

	public String GetName()
	{
		return this.m_sName;
	}

	public abstract Object GetRandomValue();
}
