package Framework.Cloud.EC2;

import java.util.List;

import Framework.Cloud.IConfigHelper;
import Framework.Cloud.Topology.Node;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

// Configures the web application named "Simple Database Operations"
public class ConfigHelperSDO implements IConfigHelper
{
	@Override
	public void Configure(Node node)
	{
		//Trace.WriteLine(TraceLevel.INFO, "Start configuring the ec2Instance [%s] as 'Simple Database Operations' ... [0]", node.GetPublicIp());
		
		Trace.WriteLine(TraceLevel.INFO, "Start configuring the ec2Instance [%s] as 'Simple Database Operations' ...", node.GetIpAddress("public"));
		
		String configScript =  
				"sudo sed -e 's/<Connector port=\"9200\"/<Connector port=\"9200\"\\n               maxThreads=\"150\"/' /etc/tomcat6/server.xml > tmp;\n" +
				"sudo mv ./tmp /etc/tomcat6/server.xml;\n" +
				"echo 'export CATALINA_OPTS=\"$CATALINA_OPTS -Xms1024m\"' >> /tmp/t;\n"+
				"echo 'export CATALINA_OPTS=\"$CATALINA_OPTS -Xmx2048m\"'>> /tmp/t;\n"+
				"echo 'export CATALINA_OPTS=\"$CATALINA_OPTS -XX:MaxPermSize=512m\"'>> /tmp/t;\n"+
				"echo 'export CATALINA_OPTS=\"$CATALINA_OPTS -ea\"'>> /tmp/t; \n" +
				"sudo mv /tmp/t /usr/share/tomcat7/bin/setenv.sh; \n" +
				"sudo /etc/init.d/tomcat6 restart;\n" +
				"exit 0;\n";
				
		int exitCode = SshClient.ExecuteCommand(node.GetIpAddress("public"), configScript);
		
		Trace.WriteLine(TraceLevel.INFO, " Done configuring the ec2Instance [%s] as 'Simple Database Operations' --> [%d]", node.GetIpAddress("public"), exitCode);
		
		 
	}
	@Override
	public void ConfigureVxlan(int index)
	{
		
	}
	
	public void Configure_old(Node node)
	{
//		String configScript = "sudo chmod 666 /etc/default/tomcat6;\n"; 
//		configScript += "sudo echo 'JAVA_OPTS=\"-Dcom.sun.management.jmxremote " +
//				"-Dcom.sun.management.jmxremote.port=21001 " +
//				"-Dcom.sun.management.jmxremote.ssl=false " +
//				"-Dcom.sun.management.jmxremote.authenticate=false " +
//				"-Djava.rmi.server.hostname=" + node.GetPublicIp() +"\"' >> /etc/default/tomcat6;\n";
//		configScript += "sudo chmod 644 /etc/default/tomcat6;\n";
//		configScript += "sudo /etc/init.d/tomcat6 restart > /dev/null;";
//		SshClient.ExecuteCommand(node.GetPublicIp(), configScript);
	
		// Trace.WriteLine(TraceLevel.WARNING, "The ec2Instance [%s] is a 'Simple Database Operations'. Should be only dependencies to configure.", node.GetPublicIp());
		
		Trace.WriteLine(TraceLevel.INFO, "Start configuring the ec2Instance [%s] as 'Simple Database Operations' ...", node.GetIpAddress("public"));
		
		String configScript =  
				  "sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080 > /dev/null;\n" +
				  "exit 0;\n";
		int exitCode = SshClient.ExecuteCommand(node.GetIpAddress("public"), configScript);
		
		Trace.WriteLine(TraceLevel.INFO, " Done configuring the ec2Instance [%s] as 'Simple Database Operations' --> [%d]", node.GetIpAddress("public"), exitCode);
	}

	@Override
	public void AddDependency(Node depFrom, List<Node> depTo)
	{
		Trace.WriteLine(TraceLevel.INFO, "Start configuring the ec2Instance [%s] as 'Simple Database Operations'. Connecting it to database on [%s] ...", depFrom.GetIpAddress("public"), depTo.get(0).GetIpAddress("private"));
		
		String configScript =  
				  "sudo /etc/init.d/tomcat6 stop > /dev/null;\n" +
				  "sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080 > /dev/null;\n" +
				  "sudo sed -e 's/url.*:3306/url=\"jdbc:mysql:\\/\\/" + depTo.get(0).GetIpAddress("private") + ":3306/' /etc/tomcat6/Catalina/localhost/DatabaseOperations.xml > tmp;\n" + 
				  "sudo mv /etc/tomcat6/Catalina/localhost/DatabaseOperations.xml oldDB.xml > /dev/null;\n" + 
				  "sudo mv tmp /etc/tomcat6/Catalina/localhost/DatabaseOperations.xml > /dev/null;\n" +  
				  "sudo cat /etc/tomcat6/Catalina/localhost/DatabaseOperations.xml > /dev/null;\n" + 
				  "sudo sed -e 's/url.*:3306/url=\"jdbc:mysql:\\/\\/" + depTo.get(0).GetIpAddress("private") + ":3306/' /var/lib/tomcat6/webapps/DatabaseOperations/META-INF/context.xml > tmp;\n" +
				  "sudo mv /var/lib/tomcat6/webapps/DatabaseOperations/META-INF/context.xml oldContext.xml > /dev/null;\n" + 
				  "sudo mv tmp /var/lib/tomcat6/webapps/DatabaseOperations/META-INF/context.xml > /dev/null;\n" +
				  "sudo cat /var/lib/tomcat6/webapps/DatabaseOperations/META-INF/context.xml > /dev/null;\n" +
				  "sudo /etc/init.d/tomcat6 start > /dev/null;\n" +
				  "exit 0;\n";
		int exitCode = SshClient.ExecuteCommand(depFrom.GetIpAddress("public"), configScript);
		
		Trace.WriteLine(TraceLevel.INFO, " Done configuring the ec2Instance [%s] as 'Simple Database Operations'. Connecting it to database on [%s] --> [%d]", depFrom.GetIpAddress("public"), depTo.get(0).GetIpAddress("private"), exitCode);
	}

	@Override
	public void RemoveDependency(Node depFrom, List<Node> depTo)
	{
		Trace.WriteNotImplemented();
	}
	
	
	
	public static void main(String args[])
	{
		String configScript = "sudo chmod 666 /etc/default/tomcat6;\n"; 
		configScript += "sudo echo 'JAVA_OPTS=\"-Dcom.sun.management.jmxremote " +
				"-Dcom.sun.management.jmxremote.port=21001 " +
				"-Dcom.sun.management.jmxremote.ssl=false " +
				"-Dcom.sun.management.jmxremote.authenticate=false " +
				"-Djava.rmi.server.hostname=50.19.44.215\"' >> /etc/default/tomcat6;\n";
		configScript += "sudo chmod 644 /etc/default/tomcat6;\n";
		configScript += "sudo /etc/init.d/tomcat6 restart > /dev/null;\n";
		int code = SshClient.ExecuteCommand("50.19.44.215", configScript);
		Trace.WriteLine(TraceLevel.DEBUG,"[%d]", code);
	}
}
