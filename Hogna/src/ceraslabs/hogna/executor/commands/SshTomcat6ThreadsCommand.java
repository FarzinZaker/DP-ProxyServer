package ceraslabs.hogna.executor.commands;

public class SshTomcat6ThreadsCommand extends SshShellCommand
{
	public String m_sCountThreads = "100";
	
	public SshTomcat6ThreadsCommand(String strType)
	{
		super(strType);
	}
	
	@Override
	public String GetScript()
	{
		String sScript = "sudo sed -e 's/maxThreads=\"[0-9]\\+\"/maxThreads=\""+ this.m_sCountThreads +"\"/' /etc/tomcat6/server.xml > tmp;\n" +
		 "sudo mv ./tmp /etc/tomcat6/server.xml;\n" +
		 "exit 0;\n";
		return sScript;
	}
}
