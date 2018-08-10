package ceraslabs.hogna.executor.commands;

public class SshUploadCommand extends Command
{
	public String m_strHostIp = "127.0.0.1";
	public String m_strLocalFile = "";
	public String m_strRemoteFile = "";
	public String m_strLoginName = "ubuntu";
	public String m_strPassword = "ubuntu";
	public String m_strPrivKeyFile = null;
	
	public SshUploadCommand(String strType)
	{
		super(strType);
	}

/*	@Override
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
			
			theSshActuator.UploadFile(conSettings, this.m_strLocalFile, this.m_strRemoteFile);
		}
		catch (ClassCastException e)
		{
			Trace.WriteLine(TraceLevel.ERROR, "The actuator [%s] does not implement interface ISshActuator!", theActuator.getClass().getName());
		}
	}
*/
}
