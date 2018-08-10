package ceraslabs.hogna.executor.commands;

public class SshShellCommand extends Command
{
	public String m_strHostIp = "127.0.0.1";
	public String m_strScript = "exit 0;";
	public String m_strLoginName = "ubuntu";
	public String m_strPassword  = "ubuntu";
	public String m_strPrivKeyFile = null;
	
	public SshShellCommand(String strType)
	{
		super(strType);
	}
	
	public String GetScript() { return this.m_strScript; }

/*
	@Override
	public void Execute(IActuator theActuator)
	{
		try
		{
			ISshActuator theSshActuator = (ISshActuator)theActuator;
			
			SshConnectionSettings conSettings = new SshConnectionSettings();
			conSettings.instanceIp            = this.m_strHostIp;
			conSettings.instanceLoginKeyFile  = this.m_strPrivKeyFile;
			conSettings.instanceLoginName     = this.m_strLoginName;
			conSettings.instanceLoginPassword = this.m_strPassword;
			
			theSshActuator.ExecuteSshScript(conSettings, this.m_strScript);
		}
		catch (ClassCastException e)
		{
			Trace.WriteLine(TraceLevel.ERROR, "The actuator [%s] does not implement interface ISshActuator!", theActuator.getClass().getName());
		}
	}
*/
}
