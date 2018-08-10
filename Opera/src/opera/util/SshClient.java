package opera.util;

import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

public class SshClient
{
	private static SshClient theClient = new SshClient();
	
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
		        //ssh.authPublickey("ubuntu", "d:\\Downloads\\Documents\\Amazon\\access\\CornelKeyPairs.pem");
		        ssh.authPublickey("ubuntu", "/Users/Nasim/Workspace/AdaptiveControlWithBW/config/AwsPrivate.key");
	
	            final Session session = ssh.startSession();
	            try
	            {
	                final Command cmd = session.exec(command);
	                //System.out.println(net.schmizz.sshj.common.IOUtils.readFully(cmd.getInputStream()).toString());
	                cmd.join(60, TimeUnit.SECONDS);
	                exitStatus = cmd.getExitStatus();
	            }
	            finally
	            {
	                session.close();
	            }
			}
			catch (java.net.ConnectException ex)
			{
				try
				{
					Thread.sleep(5000);
				}
				catch (Exception e) { }
				retry = true;
			}
			catch(Exception ex)
			{
				System.out.println(ex);
			}
			finally
			{
				//try { ssh.disconnect(); } catch (Exception e) { Trace.WriteException(e); }
			}
		}

		return exitStatus;
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
			//Trace.WriteLine(TraceLevel.INFO, "[%s] [%d] [%s]", hostname, port, key.toString());
			// do not verify anything. accept any host
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
		        //ssh.authPublickey("ubuntu", "d:\\Downloads\\Documents\\Amazon\\access\\CornelKeyPairs.pem");
		        ssh.authPublickey("ubuntu", "./config/AwsPrivate.key");
		        
		        ssh.newSCPFileTransfer().upload(pathFile, ".");
			}
			catch (java.net.ConnectException ex)
			{
				try
				{
					Thread.sleep(5000);
				}
				catch (Exception e) { }
				retry = true;
			}
			catch(Exception ex)
			{
				System.out.println(ex);
			}
			finally
			{
				//try { ssh.disconnect(); } catch (Exception e) { Trace.WriteException(e); }
			}
		}
	}
	
	public static void main(String ... args)
	{
		SshClient.UploadFile("54.159.98.34", "d:/Downloads/... bulk/Work/Documents/Hogna/hogna.tex");
	}
}

