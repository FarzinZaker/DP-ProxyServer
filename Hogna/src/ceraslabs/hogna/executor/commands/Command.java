package ceraslabs.hogna.executor.commands;

/**
 * This is the abstract class for all the commands that should be executed by the actuator.
 * 
 * @author Cornel
 *
 */
public abstract class Command
{
	private final String m_strType;
	
	protected Command(String strType)
	{
		this.m_strType = strType;
	}
	
	public String GetType()
	{
		return this.m_strType;
	}
}
