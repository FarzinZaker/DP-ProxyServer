package Framework.Diagnostics;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Trace
{
	public enum TraceLevel
	{
		/*
		 * Just some informations about what's happening in the code. Should appear during debug only.
		 */
		INFO,
		DEBUG,
		WARNING,
		ERROR,
	}
	
	private class CustomConsoleHandler extends ConsoleHandler
	{
		public CustomConsoleHandler()
		{
			super();
			this.setOutputStream(System.out);
		}
	}
	
	private static Logger theLogger = null;

	private static Logger GetLogger()
	{
		if (null == Trace.theLogger)
		{
			// the logger is not initialized yet. Do it now.
			Trace.theLogger = Logger.getLogger("Framework");
			try
			{
				Trace.theLogger.setUseParentHandlers(false);

				// console logger
				Handler logHdlr = new Trace().new CustomConsoleHandler();
				logHdlr.setEncoding("UTF-8");
				logHdlr.setFormatter(new Formatter() {
				      public String format(LogRecord record)
				      {
				        return record.getMessage();
				      }
				    });
				Trace.theLogger.addHandler(logHdlr);

				// file logger
				boolean canAccessFileLogger = false;
				java.io.File file = new java.io.File("./output/framework.log");
				if ((canAccessFileLogger = file.exists()) == false)
				{
					// create file
					canAccessFileLogger = file.getParentFile().mkdirs();
				}

				if (canAccessFileLogger)
				{
					Handler logHdlrFile = new FileHandler("./output/framework.log", true);
					logHdlrFile.setEncoding("UTF-8");
					logHdlrFile.setFormatter(new Formatter() {
					      public String format(LogRecord record)
					      {
					        return record.getMessage();
					      }
					    });
					Trace.theLogger.addHandler(logHdlrFile);
				}				
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return Trace.theLogger;
	}
	
	public static void Assert(boolean condition, String msg, Object... args)
	{
		if (condition == false)
		{
			Trace.WriteLine(TraceLevel.ERROR, msg, args);
			System.exit(1);
		}
	}
	
	public static void WriteLine(boolean condition, TraceLevel lvl, String msg, Object... args)
	{
		if (condition)
		{
			Trace.WriteLine(lvl, msg, args);
		}
	}
	
	public static void WriteLine(boolean condition, String msg, Object... args)
	{
		if (condition)
		{
			Trace.WriteLine(msg, args);
		}
	}

	public static void Write(boolean condition, String msg, Object... args)
	{
		if (condition)
		{
			String sLogEntry = String.format(msg, args);
			
			Trace.GetLogger().log(Level.INFO, sLogEntry);
		}
	}
	
	public static void WriteLine(TraceLevel lvl, String msg, Object... args)
	{
		String sFormattedMessage = String.format(msg, args);
		String sLogEntry = String.format("<%d> [%s] [Thread-%d] (%s:%d) %s\n",
										 System.currentTimeMillis(),
										 lvl.toString(),
										 Thread.currentThread().getId(),
										 Thread.currentThread().getStackTrace()[2].getFileName(),
										 Thread.currentThread().getStackTrace()[2].getLineNumber(),
										 sFormattedMessage);
		Trace.GetLogger().log(Level.INFO, sLogEntry);
	}
	
	public static void WriteLine(String msg, Object... args)
	{
		String sLogEntry = String.format(msg, args);
		Trace.GetLogger().log(Level.INFO, sLogEntry + "\n");
	}
	
	public static void WriteNotImplemented()
	{
		String sLogEntry = String.format("<%d> [%s] [Thread-%d] (%s:%d) %s(...): Not implemented!\n",
										 System.currentTimeMillis(),
										 TraceLevel.DEBUG,
										 Thread.currentThread().getId(),
										 Thread.currentThread().getStackTrace()[2].getFileName(),
										 Thread.currentThread().getStackTrace()[2].getLineNumber(),
										 Thread.currentThread().getStackTrace()[2].getMethodName());
		Trace.GetLogger().log(Level.INFO, sLogEntry);
	}

	/**
	 * Write a message to the log.
	 * 
	 * @param 	msg     the message to be written. Can use {@link printf} format flags.
	 * @param	args    A vector of arguments. Will be inserted in the <code>msg</code>
	 *                  according to the format flags.
	 */
	public static void Write(String msg, Object... args)
	{
		String sLogEntry = String.format(msg, args);
		
		Trace.GetLogger().log(Level.INFO, sLogEntry);
	}
	
	public static void WriteException(Exception ex)
	{
		//ex.printStackTrace();
		String sLogEntry = String.format("<%d> [%s] [Thread-%d] (%s:%d): %s  -->  %s\n",
										 System.currentTimeMillis(),
										 TraceLevel.ERROR,
								         Thread.currentThread().getId(),
								         Thread.currentThread().getStackTrace()[2].getFileName(),
								         Thread.currentThread().getStackTrace()[2].getLineNumber(),
								         ex.getClass().getName(),
								         ex.getLocalizedMessage());

		for (StackTraceElement elem : ex.getStackTrace())
		{
			sLogEntry += String.format("\t\t\t%s\n", elem.toString());
		}
		
		Trace.GetLogger().log(Level.INFO, sLogEntry);
	}
}
