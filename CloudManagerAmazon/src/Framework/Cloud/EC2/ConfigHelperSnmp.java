package Framework.Cloud.EC2;

import java.util.List;

import Framework.Cloud.IConfigHelper;
import Framework.Cloud.Topology.Node;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class ConfigHelperSnmp implements IConfigHelper
{
	@Override
	public void Configure(Node node)
	{
		Trace.WriteLine(TraceLevel.INFO, "Start configuring SNMP on the ec2Instance [%s] ...", node.GetIpAddress("public"));
		String configSection = 	"sudo wget http://ceras.eso.yorku.ca/etavirp/snmp/snmpd -O /etc/default/snmd > /dev/null 2> /dev/null;\n" +
								"sudo wget http://ceras.eso.yorku.ca/etavirp/snmp/snmpd.conf -O /etc/snmp/snmpd.conf > /dev/null 2> /dev/null;\n" +
								"sudo /etc/init.d/snmpd restart > /dev/null 2> /dev/null;\n" +
								"exit 0;\n";

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
		"exit 0;\n";

		int exitCode = SshClient.ExecuteCommand(node.GetIpAddress("public"), configScript);
		Trace.WriteLine(TraceLevel.INFO, " Done configuring SNMP on the ec2Instance [%s] --> [%d].", node.GetIpAddress("public"), exitCode);

	}
	public void ConfigureVxlan(int index)
	{
		
	}

	@Override
	public void AddDependency(Node depFrom, List<Node> depTo) { }

	@Override
	public void RemoveDependency(Node depFrom, List<Node> depTo) { }

}
