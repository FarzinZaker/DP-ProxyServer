package Framework.Cloud.EC2;

import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import Framework.Diagnostics.Trace;

public class SshClient
{
	private static SshClient theClient = new SshClient();
	String keyPath="/Users/Nasim/Workspace/AdaptiveControlWithBW/config/AwsPrivate.key";
	
	private int ExecuteCommandPrivate(String ip, String command)
	{
		int exitStatus = -1;
		SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier( new HostKeyVerifierDefault() );

		int retries = 5;
		boolean retry = true;
		for (int i = 0; i < retries && retry; ++i)
		{
			retry = false;
			try
			{
		        ssh.connect(ip);
		        ssh.authPublickey("ubuntu", keyPath);

				try (Session session = ssh.startSession()) {
					final Command cmd = session.exec(command);
					cmd.join(60, TimeUnit.SECONDS);
					exitStatus = cmd.getExitStatus();
				}
			}
			catch (java.net.ConnectException ex)
			{
				try
				{
					Thread.sleep(5000);
				}
				catch (Exception ignored) { }
				retry = true;
			}
			catch(Exception ex)
			{
				Trace.WriteException(ex);
			}
		}

		return exitStatus;
	}

	
	//to have output of the command
	private String ExecuteCommandwithOutputPrivate(String ip, String command)
	{
		
		String output="";
		int exitStatus = -1;
		SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier( new HostKeyVerifierDefault() );

		int retries = 5;
		boolean retry = true;
		for (int i = 0; i < retries && retry; ++i)
		{
			retry = false;
			try
			{
		        ssh.connect(ip);
		        ssh.authPublickey("ubuntu", keyPath);

				try (Session session = ssh.startSession()) {
					final Command cmd = session.exec(command);
					output = net.schmizz.sshj.common.IOUtils.readFully(cmd.getInputStream()).toString();
					cmd.join(60, TimeUnit.SECONDS);
					exitStatus = cmd.getExitStatus();
				}
			}
			catch (java.net.ConnectException ex)
			{
				try
				{
					Thread.sleep(5000);
				}
				catch (Exception ignored) { }
				retry = true;
			}
			catch(Exception ex)
			{
				Trace.WriteException(ex);
			}
		}

		return output;
	}

	public static String ExecuteCommandWithOutput(String ip, String command)
	{
		return theClient.ExecuteCommandwithOutputPrivate(ip, command);
	}
	
	public static int ExecuteCommand(String ip, String command)
	{
		return theClient.ExecuteCommandPrivate(ip, command);
	}
	
	private class HostKeyVerifierDefault implements HostKeyVerifier
	{
		@Override
		public boolean verify(String hostname, int port, PublicKey key)
		{
			return true;
		}
	}
	
	public static void UploadFile(String ip, String pathFile)
	{
		theClient.UploadFileInternal(ip, pathFile);
	}
	
	private void UploadFileInternal(String ip, String pathFile)
	{
		SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier( new HostKeyVerifierDefault() );
		
		int retries = 5;
		boolean retry = true;
		for (int i = 0; i < retries && retry; ++i)
		{
			retry = false;
			try
			{
		        ssh.connect(ip);
		        ssh.authPublickey("ubuntu", keyPath);
		        
		        ssh.newSCPFileTransfer().upload(pathFile, ".");
			}
			catch (java.net.ConnectException ex)
			{
				try
				{
					Thread.sleep(5000);
				}
				catch (Exception ignored) { }
				retry = true;
			}
			catch(Exception ex)
			{
				Trace.WriteException(ex);
			}
		}
	}
}
