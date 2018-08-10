package ceraslabs.hogna.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ceraslabs.hogna.IExecutor;
import ceraslabs.hogna.configuration.ConfigurationManager;
import ceraslabs.hogna.configuration.ExecutorConfigurationSection;
import ceraslabs.hogna.configuration.ExecutorConfigurationSection.ActuatorConfig;
import ceraslabs.hogna.executor.actuators.DummyActuator;
import ceraslabs.hogna.executor.actuators.IActuator;
import ceraslabs.hogna.executor.commands.Command;
import ceraslabs.hogna.executor.commands.CommandResult;
import ceraslabs.hogna.executor.commands.ICommandCompleteCallback;
import ceraslabs.hogna.executor.commands.SshShellCommand;
import ceraslabs.hogna.executor.commands.SshUploadCommand;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

/*
 * Configuration Section:
 * 
 * <executor>
 *   <actuators>
 *     <actuator type="com.ceraslabs.hogna.executor.SshActuator" commands="ssh.set-tomcat6-threads" />
 *     <actuator type="com.ceraslabs.hogna.executor.DummyActuator" commands="ec2.build-topology, ec2.add-instances, ec2.remove-instances">
 *       <DummyActuator>
 *         ...
 *       </DummyActuator>
 *     <actuator>
 *   </actuators>
 * </executor>
 * 
 * 
 * Errors:
 * • Register an Actuator, but the class cannot be found
 * • Register an Actuator, but the class cannot be instantiated
 * • For a command type, no actuator is registered.
 */

public class Executor implements IExecutor
{
	private Map<String, IActuator> m_mapActuators = new HashMap<>();
	private List<ICommandCompleteCallback> m_lstCallbackCommandComplete = new ArrayList<>();
	private Map<String, List<ICommandCompleteCallback>> m_mapCallbackCommandComplete = new HashMap<>();

	
	public Executor()
	{
		// get executors from configuration file
		ExecutorConfigurationSection secExecutor = (ExecutorConfigurationSection)ConfigurationManager.GetSection("executor");
		if (secExecutor != null)
		{
			List<ActuatorConfig> lstActuatorConfig = secExecutor.GetActuatorsConfig();

			for (ActuatorConfig actuatorCfg : lstActuatorConfig)
			{
				for (String sCmd : actuatorCfg.sActuatorCommands )
				{
					this.RegisterActuator(sCmd, actuatorCfg.theActuator);
				}
			}
		}
	}
	
	public IActuator RegisterActuator(String sCommandType, IActuator actuator)
	{
		IActuator registeredActuator = this.m_mapActuators.put(sCommandType, actuator);
		if (registeredActuator == null)
		{
			Trace.WriteLine(TraceLevel.INFO, "Command [%s]: registered actuator [%s].", sCommandType, actuator.getClass().getCanonicalName());
		}
		else
		{
			Trace.WriteLine(TraceLevel.INFO, "Command [%s]: replaced actuator [%s] with [%s].", sCommandType,
					                                                                            registeredActuator.getClass().getCanonicalName(),
					                                                                            actuator.getClass().getCanonicalName());
		}
		return actuator;
	}
	
	@Override
	public IActuator RegisterActuator(String sCommandType, String sActuator)
	{
		try
		{
			IActuator actuator = (IActuator) Class.forName(sActuator).newInstance();
			return this.RegisterActuator(sCommandType, actuator);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			// class cannot be instantiated automatically
			e.printStackTrace();
		}
		catch (ClassCastException e)
		{
			// class does not implement IActuator interface
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void AddCommandCompleteCallback(String cmdType, ICommandCompleteCallback cmdComplete)
	{
		List<ICommandCompleteCallback> lstCallbacks = this.m_mapCallbackCommandComplete.get(cmdType);
		if (lstCallbacks == null)
		{
			lstCallbacks = new ArrayList<ICommandCompleteCallback>();
			this.m_mapCallbackCommandComplete.put(cmdType, lstCallbacks);
		}
		lstCallbacks.add(cmdComplete);
	}
	
	@Override
	public void AddCommandCompleteCallback(ICommandCompleteCallback cmdComplete)
	{
		this.m_lstCallbackCommandComplete.add(cmdComplete);
	}
	
	@Override
	public void RemoveCommandCompleteCallback(String cmdType, ICommandCompleteCallback cmdComplete)
	{
		List<ICommandCompleteCallback> lstCallbacks = this.m_mapCallbackCommandComplete.get(cmdType);
		if (lstCallbacks != null)
		{
			lstCallbacks.remove(cmdComplete);
		}
	}
	
	public void RemoveCommandCompleteCallback(ICommandCompleteCallback cmdComplete)
	{
		this.m_lstCallbackCommandComplete.remove(cmdComplete);
	}
	
	protected void OnCommandComplete(Command cmd, CommandResult cmdResult)
	{
		List<ICommandCompleteCallback> lstCallbacks = this.m_mapCallbackCommandComplete.get(cmd.GetType());
		if (lstCallbacks != null)
		{
			for (ICommandCompleteCallback cmdComplete : lstCallbacks)
			{
				cmdComplete.CommandComplete(cmd, cmdResult);
			}
		}
		
		for (ICommandCompleteCallback cmdComplete : this.m_lstCallbackCommandComplete)
		{
			cmdComplete.CommandComplete(cmd, cmdResult);
		}
	}

	@Override
	public void Execute(Command cmd)
	{
		// 1. find the actuator
		// 2. Call IActuator.Execute(...) for the command
		IActuator theActuator = this.m_mapActuators.get(cmd.GetType());
		if (theActuator != null)
		{
			CommandResult cmdResult = theActuator.Execute(cmd);
			this.OnCommandComplete(cmd, cmdResult);
		}
		else
		{
			Trace.WriteLine(TraceLevel.ERROR, "No actuator found for operations of type [%s].", cmd.GetType());
		}
	}

	@Override
	public void Execute(List<Command> lstCommands)
	{
		for (Command command : lstCommands)
		{
			this.Execute(command);
		}
	}

	/**
	 * Pretty prints the Executioner.
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		int lineSize = 0;
		ArrayList<String> lstLines = new ArrayList<>();
		
		Map<IActuator, ArrayList<String>> mapActuatorToCommand = new HashMap<>(); 
		for (Entry<String, IActuator> entry : this.m_mapActuators.entrySet())
		{
			if (mapActuatorToCommand.containsKey(entry.getValue()) == false)
				mapActuatorToCommand.put(entry.getValue(), new ArrayList<String>());
			mapActuatorToCommand.get(entry.getValue()).add(entry.getKey());
		}
		
		String strLine = "";
		for (Entry<IActuator, ArrayList<String>> entry : mapActuatorToCommand.entrySet())
		{
			strLine = String.format("[%10d] %s", entry.getKey().hashCode(), entry.getKey().getClass().getName());
			lstLines.add(strLine);
			lineSize = lineSize > strLine.length() ? lineSize : strLine.length();
			// search for other
			for (int i = 0; i < entry.getValue().size() - 1; ++i)
			{
				strLine = String.format("%14s ├─ • %s", " ", entry.getValue().get(i));
				lstLines.add(strLine);
				lineSize = lineSize > strLine.length() ? lineSize : strLine.length();
			}
			strLine = String.format("%14s └─ • %s", " ", entry.getValue().get(entry.getValue().size() - 1));
			lstLines.add(strLine);
			lineSize = lineSize > strLine.length() ? lineSize : strLine.length();
		}
		
		String strLead = "│    Executor    │";
		sb.append(String.format("┌────────────────┬─%" + (lineSize + 1) + "s┐\n", " ").replaceAll(" ", "─"));
		for (int i = 0; i < lstLines.size(); ++i)
		{
			strLine = lstLines.get(i);
			sb.append(String.format("%s %s%" + (lineSize - strLine.length() + 1) + "s│", strLead, strLine, " "));
			sb.append("\n");
			if (i == 0)
			{
				strLead = String.format("│ [ %10d ] │", this.hashCode());
			}
			else if (i == 1)
			{
				strLead = String.format("│ %14s │", " ");
			}
		}
		sb.append(String.format("└────────────────┴─%" + (lineSize + 1) + "s┘\n", " ").replaceAll(" ", "─"));

		return sb.toString();
	}

	class CommandCompleteCallback implements ICommandCompleteCallback
	{
		@Override
		public void CommandComplete(Command cmd, CommandResult cmdResult)
		{
			if (cmdResult == null)
			{
				Trace.WriteLine(TraceLevel.DEBUG, "Command [%s] complete. Result code [?].", cmd.GetType());
			}
			else
			{
				Trace.WriteLine(TraceLevel.DEBUG, "Command [%s] complete. Result code [%d].", cmd.GetType(), cmdResult.GetResultCode());
			}
		}
	}
	
	public static void main (String... args) throws Exception
	{
		ConfigurationManager.Configure("./input.Samples/application.config");

		Executor theExecutor = new Executor();
		theExecutor.AddCommandCompleteCallback(theExecutor.new CommandCompleteCallback());
		theExecutor.RegisterActuator("ssh.upload-file", DummyActuator.class.getName());
		theExecutor.RegisterActuator("ssh.execute-shell-script", DummyActuator.class.getName());
		
		System.out.print(theExecutor.toString());

		// create some commands
		List<Command> lstCommands = new ArrayList<>();
		Command cmd = null;


		cmd = new SshShellCommand("ssh.execute-shell-script");
		lstCommands.add(cmd);
		
		cmd = new SshShellCommand("ssh.tomcat6.set-threads");
		lstCommands.add(cmd);
		
		cmd = new SshUploadCommand("ssh.upload-file");
		lstCommands.add(cmd);
		
		theExecutor.Execute(lstCommands);
	}
}
