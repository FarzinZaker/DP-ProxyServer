package ceraslabs.hogna.executor.actuators;

import ceraslabs.hogna.executor.commands.Command;
import ceraslabs.hogna.executor.commands.CommandResult;

public interface IActuator
{
	// type
	// String GetType();
	
	CommandResult Execute(Command command);



	// To Remove
	//void AddClusterNodes(CommandArgs commandArgs);
	//void RemoveClusterNodes(CommandArgs commandArgs);
	
//	void FilterRequests(CommandArgs commandArgs);
//	void UnfilterRequests(CommandArgs commandArgs);
	
	//void BuildTopology(Topology topology);
	
	//int ExecuteSshScript(String sHostIp, String sScript);
}
