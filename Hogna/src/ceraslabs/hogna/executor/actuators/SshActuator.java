package ceraslabs.hogna.executor.actuators;

import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ceraslabs.hogna.executor.SshConnectionSettings;
import ceraslabs.hogna.executor.SshResult;
import ceraslabs.hogna.executor.commands.CommandResult;
import ceraslabs.hogna.executor.commands.SshShellCommand;
import ceraslabs.hogna.executor.commands.SshUploadCommand;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.common.IOUtils;
import Framework.Diagnostics.Trace;

@XmlRootElement(name = "SshActuator", namespace = "ceraslabs.hogna.executor.actuators.SshActuator")
public class SshActuator implements IActuator
{
	@XmlElement(name = "conRetries", namespace="ceraslabs.hogna.executor.actuators.SshActuator")
	private int m_conRetries = 5;
	@XmlElement(name = "conWaitRetry", namespace="ceraslabs.hogna.executor.actuators.SshActuator")
	private int m_conWaitRetry = 5000;
	@XmlElement(name = "credUser", namespace="ceraslabs.hogna.executor.actuators.SshActuator")
	private String m_credLoginName = "ubuntu";
	@XmlElement(name = "credPass", namespace="ceraslabs.hogna.executor.actuators.SshActuator")
	private String m_credLoginPass = null;
	@XmlElement(name = "credPrivateKeyFile", namespace="ceraslabs.hogna.executor.actuators.SshActuator")
	private String m_credLoginKeyFile = null;

	public CommandResult ExecuteSshScript(SshShellCommand cmd)
	{
		SshConnectionSettings conSettings = new SshConnectionSettings();
		conSettings.instanceIp            = cmd.m_strHostIp;
		conSettings.instanceLoginKeyFile  = cmd.m_strPrivKeyFile;
		conSettings.instanceLoginName     = cmd.m_strLoginName;
		conSettings.instanceLoginPassword = cmd.m_strPassword;
		
		return this.ExecuteSshScript(conSettings, cmd.GetScript());
	}
	
	public SshResult ExecuteSshScript(SshConnectionSettings connectionSettings, String sScript)
	{
		SshResult result = new SshResult();
		
		SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier( new HostKeyVerifierDefault() );

		boolean retry = true;
		for (int i = 0; i < this.m_conRetries && retry; ++i)
		{
			retry = false;
			try
			{
		        ssh.connect(connectionSettings.instanceIp);
		        if (connectionSettings.instanceLoginKeyFile != null)
		        	ssh.authPublickey(connectionSettings.instanceLoginName, connectionSettings.instanceLoginKeyFile);
		        else
		        	ssh.authPassword(connectionSettings.instanceLoginName, connectionSettings.instanceLoginPassword);
	
	            final Session session = ssh.startSession();
	            try
	            {
	                final Command cmd = session.exec(sScript);
	                //System.out.println(net.schmizz.sshj.common.IOUtils.readFully(cmd.getInputStream()).toString());
	                cmd.join(60, TimeUnit.SECONDS);
	                result.SetResultCode(cmd.getExitStatus());
	                result.SetOutput(IOUtils.readFully(cmd.getInputStream()).toString());
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
					Thread.sleep(this.m_conWaitRetry);
				}
				catch (Exception e) { }
				retry = true;
		        result.SetResultCode(-1);
			}
			catch(Exception ex)
			{
				Trace.WriteException(ex);
		        result.SetResultCode(-1);
			}
			finally
			{
				//try { ssh.disconnect(); } catch (Exception e) { Trace.WriteException(e); }
			}
		}

		return result;
	}

	public CommandResult UploadFile(SshUploadCommand cmd)
	{
		SshConnectionSettings conSettings = new SshConnectionSettings();
		conSettings.instanceIp            = cmd.m_strHostIp;
		conSettings.instanceLoginKeyFile  = cmd.m_strPrivKeyFile;
		conSettings.instanceLoginName     = cmd.m_strLoginName;
		conSettings.instanceLoginPassword = cmd.m_strPassword;
		
		return this.UploadFile(conSettings, cmd.m_strLocalFile, cmd.m_strRemoteFile);
	}
	
	public SshResult UploadFile(SshConnectionSettings connectionSettings, String sLocalFile, String sRemoteFile)
	{
		SshResult result = new SshResult();
		
		SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier( new HostKeyVerifierDefault() );

		boolean retry = true;
		for (int i = 0; i < this.m_conRetries && retry; ++i)
		{
			retry = false;
			try
			{
		        ssh.connect(connectionSettings.instanceIp);
		        if (connectionSettings.instanceLoginKeyFile != null)
		        	ssh.authPublickey(connectionSettings.instanceLoginName, connectionSettings.instanceLoginKeyFile);
		        else
		        	ssh.authPassword(connectionSettings.instanceLoginName, connectionSettings.instanceLoginPassword);

		        ssh.newSCPFileTransfer().upload(sLocalFile, sRemoteFile);

		        result.SetResultCode(0);
			}
			catch (java.net.ConnectException ex)
			{
				try
				{
					Thread.sleep(this.m_conWaitRetry);
				}
				catch (Exception e) { }
				retry = true;
		        result.SetResultCode(-1);
			}
			catch(Exception ex)
			{
				Trace.WriteException(ex);
		        result.SetResultCode(-1);
			}
			finally
			{
				//try { ssh.disconnect(); } catch (Exception e) { Trace.WriteException(e); }
			}
		}
		
		return result;
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


	@Override
	public CommandResult Execute(ceraslabs.hogna.executor.commands.Command command)
	{
		// this class knows how to handle the following commands:
		// ssh.upload-file -- SshUploadCommand
		// ssh.execute-shell-script -- SshShellCommand
		if (command instanceof SshShellCommand)
		{
			return this.ExecuteSshScript((SshShellCommand)command);
		}
		else if (command instanceof SshUploadCommand)
		{
			return this.UploadFile((SshUploadCommand)command);
		}

		return null;
	}
}
