package ceraslabs.hogna.executor;

import ceraslabs.hogna.executor.commands.CommandResult;

public class SshResult extends CommandResult
{
	private String m_sOutput = "";
	
	public String GetOutput() { return this.m_sOutput; }
	public void SetOutput(String value) { this.m_sOutput = value; }
}
