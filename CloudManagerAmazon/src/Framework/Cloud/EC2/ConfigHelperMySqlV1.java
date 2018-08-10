package Framework.Cloud.EC2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import Framework.Cloud.IConfigHelper;
import Framework.Cloud.Topology.Node;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class ConfigHelperMySqlV1 implements IConfigHelper
{   public static String DBPublicIP=null, DBPrivateIP=null;
	@Override
	public void Configure(Node node)
	{
		DBPublicIP=node.GetIpAddress("public");	
		DBPrivateIP=node.GetIpAddress("private");
		Trace.WriteLine(TraceLevel.INFO, "Start configuring the ec2Instance [%s] as MySql database ...", node.GetIpAddress("public"));
		String configScript = 
				"sudo service mysql start;\n" +
				"sudo service snmpd start;\n" +
				//"sudo apt-get install openvswitch-switch openvswitch-common;\n"+		
				"exit 0;";
		int exitCode = SshClient.ExecuteCommand(node.GetIpAddress("public"), configScript);
		Trace.WriteLine(TraceLevel.INFO, " Done configuring the ec2Instance [%s] as MySql database --> [%d]", node.GetIpAddress("public"), exitCode);
		//ConfigureVxlan(0);
	
	}

	@Override
	public void ConfigureVxlan(int index)
	{
		String name="amazon_DB";
		//current number of virtual interfaces
		int currentNoNic=0;
		try {
			String command = "python3 /Users/Nasim/Dropbox/PycharmProjects/setup-on-Amazon/update_with_amazon.py "
					+ name + " " + currentNoNic +  " " + DBPublicIP+ " " + DBPrivateIP;
			Process p1 = Runtime.getRuntime().exec(command);
			p1.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p1.getInputStream()));
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
			}

		}
		catch (IOException e1) {
		} catch (InterruptedException e2) {
		}
	}
	
	
	@Override
	public void AddDependency(Node depFrom, List<Node> depTo)
	{
		Trace.WriteLine(TraceLevel.WARNING, "The ec2Instance [%s] is a MySql database. Shouldn't be any dependency to configure.", depFrom.GetIpAddress("public"));
		// MySql has no dependency to configure
	}

	@Override
	public void RemoveDependency(Node depFrom, List<Node> depTo)
	{
		Trace.WriteLine(TraceLevel.WARNING, "The ec2Instance [%s] is a MySql database. Shouldn't be any dependency to remove.", depFrom.GetIpAddress("public"));
	}
}
