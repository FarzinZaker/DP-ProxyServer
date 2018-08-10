package ceraslabs.hogna.cli;

import java.lang.reflect.Field;
import java.util.HashMap;

import ceraslabs.hogna.cli.CliAppParam;

public class CommandLineHandler
{
	public static void CommandLineParse(Object settings, String[] args)
	{
		HashMap<String, String> arguments = new HashMap<String, String>();

		for (int i = 0; i < args.length; ++i)
		{
			if (args[i].startsWith("-") == false)
			{
				// this is not an accepted argument, ignore
				continue;
			}

			// search for "=".
			int idx = args[i].indexOf('=');
			if (idx == -1)
			{
				// check if this argument has a value
				if (i + 1 < args.length && args[i+1].startsWith("-") == false)
				{
					// assume the argument is in the form "-key value".
					// the next element in the array is the value
					arguments.put(args[i].substring(1), args[i+1]);
					// skip the next argument
					++i;
				}
				else
				{
					// assume the argument does not have a value
					arguments.put(args[i].substring(1), null);
				}
			}
			else
			{
				// assume the argument is in the form "key=value"
				// split this argument
				arguments.put(args[i].substring(1, idx), args[i].substring(idx + 1));
			}
		}
		
		Class<?> classSettings = settings.getClass();
		Field[] fields = classSettings.getDeclaredFields();
		for (Field field : fields)
		{
			CliAppParam progParameter = field.getAnnotation(CliAppParam.class);
			if (progParameter != null)
			{
				if (arguments.containsKey(progParameter.Name()))
				{
					Object paramValue = arguments.get(progParameter.Name());
					try
					{
						if (paramValue == null)
						{
							// assume that this is a boolean parameter, set to true
							if (field.getType().equals(boolean.class))
							{
									field.set(settings, true);
							}
						}
						else
						{
							if (field.getType().equals(int.class))
							{
								field.setInt(settings, Integer.parseInt(paramValue.toString()));
										
							}
							else
							{
								field.set(settings, paramValue);
							}
						}
					}
					catch (Exception e) {}
				}
			}
		}
	}
}
