package Framework.Cloud.EC2;

import java.util.List;

import Framework.Cloud.IConfigHelper;
import Framework.Cloud.Topology.Node;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class ConfigHelperMySql implements IConfigHelper
{
	@Override
	public void Configure(Node node)
	{
		Trace.WriteLine(TraceLevel.INFO, "Start configuring the ec2Instance [%s] as MySql database ...", node.GetIpAddress("public"));
		String configScript = "hostname; pwd; ./scripts/mySqlConfig.sh; sudo service mysql start; hostname; date; exit 0;";
		//String configScript = "hostname; pwd; ./scripts/install.sh mysql; sudo /etc/init.d/mysql restart; hostname; date; exit 0;";
		int exitCode = SshClient.ExecuteCommand(node.GetIpAddress("public"), configScript);
		Trace.WriteLine(TraceLevel.INFO, " Done configuring the ec2Instance [%s] as MySql database --> [%d]", node.GetIpAddress("public"), exitCode);
	}
	public void ConfigureVxlan(int index)
	{
		
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
