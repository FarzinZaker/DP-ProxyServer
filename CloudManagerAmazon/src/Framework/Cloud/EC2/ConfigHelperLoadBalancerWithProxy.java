package Framework.Cloud.EC2;

import java.util.List;

import Framework.Cloud.IConfigHelper;
import Framework.Cloud.Topology.Node;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

/**
 * This class works with ami that is built for 32- and 64-bit systems.
 * Assumes that apache is installed and configured as a load balancer;
 * the members of the cluster are added/removed dynamically.
 * @author Cornel
 *
 */
public class ConfigHelperLoadBalancerWithProxy implements IConfigHelper
{
	@Override
	public void Configure(Node node)
	{
		Trace.WriteLine(TraceLevel.INFO, "Start configuring the ec2Instance [%s] as Web Load Balancer ...", node.GetIpAddress("public"));
		
		SshClient.UploadFile(node.GetIpAddress("public"), "./resources/ProxyServer.jar");

		String configScript = "sudo service apache2 start;\n" +
		//"wget http://ceras.eso.yorku.ca/etavirp/proxy/ProxyServer.jar > proxy.log;\n" +
		"sudo nohup java -jar ./ProxyServer.jar -remote-address=\"127.0.0.1\" -remote-port=\"9100\" -control-port=\"9300\" > proxy.log &" +
		"exit 0;\n";

		int exitCode = SshClient.ExecuteCommand(node.GetIpAddress("public"), configScript);
		Trace.WriteLine(TraceLevel.INFO, " Done configuring the ec2Instance [%s] as Web Load Balancer --> [%d].", node.GetIpAddress("public"), exitCode);
	}
	public void ConfigureVxlan(int index)
	{
		
	}

	@Override
	public void AddDependency(Node depFrom, List<Node> depTo)
	{
		Trace.WriteLine(TraceLevel.INFO, "Start creating dependencies for the Web Load Balancer [%s] ...", depFrom.GetIpAddress("public"));

		String configSection = "";
		for (Node node : depTo)
		{
			configSection += "\\tBalancerMember http:\\/\\/" + node.GetIpAddress("private") + ":9200\\n";
		}
		String configScript = 	"sudo sed -e 's/<\\/Proxy>/" + configSection + "<\\/Proxy>/' /etc/apache2/conf.d/proxy-balancer.conf > tmp;\n" +
							    "sudo mv ./tmp /etc/apache2/conf.d/proxy-balancer.conf;\n" +
//							    "sudo apache2ctl graceful;\n" +
							    "sudo /etc/init.d/apache2 restart;\n" +
							    "exit 0;\n";
		int exitCode = SshClient.ExecuteCommand(depFrom.GetIpAddress("public"), configScript);
		Trace.WriteLine(TraceLevel.INFO, " Done creating dependencies for the Web Load Balancer [%s] --> [%d].", depFrom.GetIpAddress("public"), exitCode);
	}

	@Override
	public void RemoveDependency(Node depFrom, List<Node> depTo)
	{
		Trace.WriteLine(TraceLevel.INFO, "Start removing dependencies for the Web Load Balancer [%s] ...", depFrom.GetIpAddress("public"));

		String configSection = "";
		for (Node node : depTo)
		{
			configSection += "sed '/" + node.GetIpAddress("private") + "/d' proxy-balancer.conf > proxy-balancer_temp.conf;\n";
			configSection += "mv proxy-balancer_temp.conf proxy-balancer.conf;\n";
		}
		String configScript =	"sudo cp /etc/apache2/conf.d/proxy-balancer.conf proxy-balancer.conf;\n" +
								configSection +
								"sudo mv proxy-balancer.conf /etc/apache2/conf.d/proxy-balancer.conf;\n" +
								"sudo apache2ctl graceful;\n" +
//								"sudo /etc/init.d/apache2 restart;\n" +
							    "exit 0;\n";
		int exitCode = SshClient.ExecuteCommand(depFrom.GetIpAddress("public"), configScript);
		Trace.WriteLine(TraceLevel.INFO, " Done removing dependencies for the Web Load Balancer [%s]  -->  [%d].", depFrom.GetIpAddress("public"), exitCode);
	}
}
