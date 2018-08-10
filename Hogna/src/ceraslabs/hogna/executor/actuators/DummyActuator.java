package ceraslabs.hogna.executor.actuators;

import ceraslabs.hogna.executor.commands.CloudBuildTopologyCommand;
import ceraslabs.hogna.executor.commands.Command;
import ceraslabs.hogna.executor.commands.CommandResult;
import ceraslabs.hogna.executor.commands.SshShellCommand;
import ceraslabs.hogna.executor.commands.SshUploadCommand;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class DummyActuator implements IActuator
{
	@Override
	public CommandResult Execute(Command command)
	{
		// this actuator knows how to do the following operations
		// - Build Topology
		// - Add nodes to the topology
		// - Remove nodes from the topology 
		
		if (command instanceof SshShellCommand)
		{
			Trace.WriteLine(TraceLevel.DEBUG, "Executing a shell command on host [%s].", ((SshShellCommand)command).m_strHostIp);
		}
		else if (command instanceof SshUploadCommand)
		{
			Trace.WriteLine(TraceLevel.DEBUG, "Uploading a file to host [%s].", ((SshUploadCommand)command).m_strHostIp);
		}
		else if (command instanceof CloudBuildTopologyCommand)
		{
			Trace.WriteLine(TraceLevel.DEBUG, "Building a topology.");
		}
		else
		{
			Trace.WriteLine(TraceLevel.ERROR, "Unknown command. Should never happen.");
		}
		
		return null;
	}
}
