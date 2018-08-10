package Framework.Cloud.EC2;

import java.util.List;

import Framework.Cloud.IConfigHelper;
import Framework.Cloud.Topology.Node;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class ConfigHelperApacheLB implements IConfigHelper
{
	@Override
	public void Configure(Node node)
	{
		Trace.WriteLine(TraceLevel.INFO, "Start configuring the ec2Instance [%s] as Web Load Balancer ...", node.GetIpAddress("public"));
		String configSection = 	"ProxyPass /balancer-manager !\n" +
								"ProxyPass / balancer://Valhalla_Cluster/\n" +
								"<Proxy balancer://Valhalla_Cluster>\n" +
								"</Proxy>\n" +
								"<Location /balancer-manager>\n" +
								"	SetHandler balancer-manager\n" +
								"</Location>\n";

		String configScript = "echo \"" + configSection + "\" > tmp;\n" +
		"sudo /etc/init.d/tomcat6 stop;\n" +
		"sudo /etc/init.d/apache2 stop;\n" +
		"sudo mv /etc/apache2/conf.d/proxy-balancer.conf old.conf;\n" +
		"sudo mv ./tmp /etc/apache2/conf.d/proxy-balancer.conf;\n" +
		"sudo sed -e 's/Deny/#Deny/' /etc/apache2/mods-available/proxy.conf > tmp.conf;\n" +
		"sudo mv /etc/apache2/mods-available/proxy.conf original.conf;\n" +
		"sudo sed -e 's/#Allow/Allow from all#/' tmp.conf > tmp2.conf;\n" +
		"sudo mv tmp2.conf /etc/apache2/mods-available/proxy.conf;\n" +
		"sudo /etc/init.d/apache2 restart;\n" +
		"sudo su -c \"echo 'root soft nofile 200000' >> /etc/security/limits.conf\"\n" +
		"sudo su -c \"echo 'root hard nofile 200000' >> /etc/security/limits.conf\"\n" +
		"sudo su -c \"echo 'ubuntu soft nofile 200000' >> /etc/security/limits.conf\"\n" +
		"sudo su -c \"echo 'ubuntu hard nofile 200000' >> /etc/security/limits.conf\"\n" +
//		"wget http://ceras.eso.yorku.ca/etavirp/proxy/ProxyWithMonitors.jar > proxy.log \n" +
//		"sudo nohup java -jar ./ProxyWithMonitors.jar 127.0.0.1 > proxy.log &\n" +
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
			configSection += "\\tBalancerMember http:\\/\\/" + node.GetIpAddress("private") + ":80\\n";
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
//								"sudo apache2ctl graceful;\n" +
								"sudo /etc/init.d/apache2 restart;\n" +
							    "exit 0;\n";
		int exitCode = SshClient.ExecuteCommand(depFrom.GetIpAddress("public"), configScript);
		Trace.WriteLine(TraceLevel.INFO, " Done removing dependencies for the Web Load Balancer [%s]  -->  [%d].", depFrom.GetIpAddress("public"), exitCode);
	}
}
