package ceraslabs.hogna.executor.commands;

public interface ICommandCompleteCallback
{
    public void CommandComplete(Command cmd, CommandResult cmdResult);
}
