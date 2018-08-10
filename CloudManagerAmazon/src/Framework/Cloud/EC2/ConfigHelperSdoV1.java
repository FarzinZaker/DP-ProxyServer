package Framework.Cloud.EC2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import Framework.Cloud.IConfigHelper;
import Framework.Cloud.Topology.Node;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

// Configures the web application named "Simple Database Operations"
public class ConfigHelperSdoV1 implements IConfigHelper
{
	public static String workerPublicIP=null, workerPrivateIP=null;
	//holds the number of workers; we need to keep track of this because of vxlan setting
	public static int index=0;

	@Override
	public void Configure(Node node)
	{
		index++;
		workerPublicIP=node.GetIpAddress("public");	
		workerPrivateIP=node.GetIpAddress("private");
		Trace.WriteLine(TraceLevel.INFO, "Start configuring the ec2Instance [%s] as 'Simple Database Operations' ...", node.GetIpAddress("public"));

		String configScript =
                "sudo service snmpd start;\n" +
                		"echo 'export CATALINA_OPTS=\"$CATALINA_OPTS -Xms1024m\"' >> /tmp/t;\n"+
        				"echo 'export CATALINA_OPTS=\"$CATALINA_OPTS -Xmx2048m\"'>> /tmp/t;\n"+
        				"echo 'export CATALINA_OPTS=\"$CATALINA_OPTS -XX:MaxPermSize=512m\"'>> /tmp/t;\n"+
        				"echo 'export CATALINA_OPTS=\"$CATALINA_OPTS -ea\"'>> /tmp/t; \n" +
        				"sudo mv /tmp/t /usr/share/tomcat7/bin/setenv.sh; \n" +
        				"sudo service tomcat7 restart;\n" +
        				//"sudo apt-get install openvswitch-switch openvswitch-common;\n"+
        				"exit 0;\n";
		int exitCode = SshClient.ExecuteCommand(node.GetIpAddress("public"), configScript);

		Trace.WriteLine(TraceLevel.INFO, " Done configuring the ec2Instance [%s] as 'Simple Database Operations' --> [%d]", node.GetIpAddress("public"), exitCode);
//		Trace.WriteLine(TraceLevel.INFO, " Done configuring the ec2Instance [%s] as 'Simple Database Operations' --> [DONE]", node.GetIpAddress("public"));
	
		//update the vxlan setting and build an interface, we might have different number of worker
		//ConfigureVxlan(index);		
	}
	
	@Override
	public void ConfigureVxlan(int index){	
		String name="amazon_worker"+Integer.toString(index);
		//current number of virtual interfaces
		int currentNoNic=0;
		try {
			String command = "python3 /Users/Nasim/Dropbox/PycharmProjects/setup-on-Amazon/update_with_amazon.py "
					+ name + " " + currentNoNic +  " " + workerPublicIP+ " " + workerPrivateIP;
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
		Trace.WriteLine(TraceLevel.INFO, "Start configuring the ec2Instance [%s] as 'Simple Database Operations'. Connecting it to database on [%s] ...", depFrom.GetIpAddress("public"), depTo.get(0).GetIpAddress("private"));
		
		String configScript =  
				  "sudo sed -e 's/url.*:3306/url=\"jdbc:mysql:\\/\\/" + depTo.get(0).GetIpAddress("private") + ":3306/' /var/lib/tomcat7/webapps/DatabaseOperations/META-INF/context.xml > tmp;\n" + 
				  "sudo mv tmp /var/lib/tomcat7/webapps/DatabaseOperations/META-INF/context.xml > /dev/null;\n" +
				  "sudo service tomcat7 start > /dev/null;\n" +
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
