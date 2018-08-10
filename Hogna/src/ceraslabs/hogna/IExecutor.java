package ceraslabs.hogna;

import java.util.List;

import ceraslabs.hogna.executor.actuators.IActuator;
import ceraslabs.hogna.executor.commands.Command;
import ceraslabs.hogna.executor.commands.ICommandCompleteCallback;

public interface IExecutor
{
	void AddCommandCompleteCallback(ICommandCompleteCallback cmdComplete);
	public void AddCommandCompleteCallback(String cmdType, ICommandCompleteCallback cmdComplete);
	public void RemoveCommandCompleteCallback(String cmdType, ICommandCompleteCallback cmdComplete);
	public IActuator RegisterActuator(String sCommandType, String sActuator);
	public void Execute(Command command);
	public void Execute(List<Command> lstCommands);
}
