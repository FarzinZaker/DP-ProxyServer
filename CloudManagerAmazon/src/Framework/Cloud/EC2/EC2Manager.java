package Framework.Cloud.EC2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import Framework.Cloud.CloudManager;
import Framework.Cloud.IConfigHelper;
import Framework.Cloud.Topology.Node;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;
import ceraslabs.hogna.configuration.ConfigurationManager;
import ceraslabs.hogna.executor.actuators.IActuator;
import ceraslabs.hogna.executor.commands.CloudBuildTopologyCommand;
import ceraslabs.hogna.executor.commands.CloudBuildTopologyCommandResult;
import ceraslabs.hogna.executor.commands.CloudScaleCommand;
import ceraslabs.hogna.executor.commands.CloudScaleCommandResult;
import ceraslabs.hogna.executor.commands.Command;
import ceraslabs.hogna.executor.commands.CommandResult;
import ceraslabs.hogna.executor.commands.CommandResult.CommandResultCodes;
import ceraslabs.hogna.utilities.IO.FileText;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.ImportKeyPairRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceNetworkInterfaceSpecification;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
//import org.apache.http.client.UserTokenHandler;



@XmlRootElement(name="Ec2Actuator", namespace="ceraslabs.hogna.executor.actuators.Ec2Actuator")
public class EC2Manager extends CloudManager implements IActuator
{
	@XmlElement(name="credFile", namespace="ceraslabs.hogna.executor.actuators.Ec2Actuator")
    private String m_credFileAws;
	@XmlElement(name="keypairName", namespace="ceraslabs.hogna.executor.actuators.Ec2Actuator")
    private String m_keypairName;
	@XmlElement(name="keypairPrivateKeyFile", namespace="ceraslabs.hogna.executor.actuators.Ec2Actuator")
    private String m_keypairPrivateKeyFile;
	@XmlElement(name="keypairPublicKeyFile", namespace="ceraslabs.hogna.executor.actuators.Ec2Actuator")
    private String m_keypairPublicKeyFile;

	
	//private EC2ManagerSettings m_accessSettings;
	AmazonEC2Client m_theEC2Client;

	Map<String, AmazonEC2Client> m_cloudClients;

	EC2Manager()
	{
		EC2ConfigurationSection sectionEc2 = (EC2ConfigurationSection)ConfigurationManager.GetSection("ec2");
		if (sectionEc2 != null)
		{
			for (Entry<String, IConfigHelper> entry : sectionEc2.GetConfigHelpers().entrySet())
			{
				this.AddConfigHelper(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public EC2Manager(EC2ManagerSettings settings) throws Exception
	{
		this();

		this.m_credFileAws = settings.AwsCredentialsFile;
		this.m_keypairName = settings.KeyPairName;
		this.m_keypairPrivateKeyFile = settings.PrivateKeyFile;
		this.m_keypairPublicKeyFile = settings.PublicKeyFile;

/*
		AWSCredentials awsCredentials = new PropertiesCredentials(new FileInputStream(settings.AwsCredentialsFile));
		this.m_theEC2Client = new AmazonEC2Client(awsCredentials);

		DescribeKeyPairsRequest requestDescribeKeyPairs = new DescribeKeyPairsRequest();
		DescribeKeyPairsResult resultDescribeKeyPairs = this.m_theEC2Client.describeKeyPairs(requestDescribeKeyPairs);
		boolean isKeyAvailable = false;
		for (KeyPairInfo keyPair : resultDescribeKeyPairs.getKeyPairs())
		{
			if (keyPair.getKeyName().equals(settings.KeyPairName))
			{
				isKeyAvailable = true;
				break;
			}
		}

		if (false == isKeyAvailable)
		{
			String sPublicKey = FileText.GetContent(settings.PublicKeyFile);
			ImportKeyPairRequest requestImportKeyPair = new ImportKeyPairRequest(settings.KeyPairName, sPublicKey);
			this.m_theEC2Client.importKeyPair(requestImportKeyPair);
		}
		
		{
			// multiple regions
			 this.m_cloudClients = new HashMap<String, AmazonEC2Client>();
			 
			 this.AddCloudClient(awsCredentials, "us-east-1",      "ec2.us-east-1.amazonaws.com");
			 this.AddCloudClient(awsCredentials, "us-west-1",      "ec2.us-west-1.amazonaws.com");
			 this.AddCloudClient(awsCredentials, "us-west-2",      "ec2.us-west-2.amazonaws.com");
			 this.AddCloudClient(awsCredentials, "eu-west-1",      "ec2.eu-west-1.amazonaws.com");
			 //this.AddCloudClient(awsCredentials, "eu-central-1",   "ec2.eu-central-1.amazonaws.com");
			 this.AddCloudClient(awsCredentials, "ap-southeast-1", "ec2.ap-southeast-1.amazonaws.com");
			 this.AddCloudClient(awsCredentials, "ap-southeast-2", "ec2.ap-southeast-2.amazonaws.com");
			 this.AddCloudClient(awsCredentials, "ap-northeast-1", "ec2.ap-northeast-1.amazonaws.com");
			 this.AddCloudClient(awsCredentials, "sa-east-1",      "ec2.sa-east-1.amazonaws.com");
		}
*/
	}
	
	/*
	 * This function provides lazy-instantiation for the cloud clients.
	 */
	private AmazonEC2Client GetClient() throws FileNotFoundException, IOException
	{
		if (this.m_theEC2Client == null)
		{
			this.m_theEC2Client = this.CreateClient();
		}
		return m_theEC2Client;
	}
	
	private AmazonEC2Client CreateClient() throws FileNotFoundException, IOException
	{
		try
		{
			// create the cloud client
			AWSCredentials awsCredentials = new PropertiesCredentials(new FileInputStream(this.m_credFileAws));
			this.m_theEC2Client = new AmazonEC2Client(awsCredentials);
			//m_theEC2Client.setEndpoint("us-east-1d");
			
			// add the keypair, if it's missing
			DescribeKeyPairsRequest requestDescribeKeyPairs = new DescribeKeyPairsRequest();
			DescribeKeyPairsResult resultDescribeKeyPairs = this.m_theEC2Client.describeKeyPairs(requestDescribeKeyPairs);
			boolean isKeyAvailable = false;
			for (KeyPairInfo keyPair : resultDescribeKeyPairs.getKeyPairs())
			{
				if (keyPair.getKeyName().equals(this.m_keypairName))
				{
					isKeyAvailable = true;
					break;
				}
			}

			if (false == isKeyAvailable)
			{
				try
				{
					String sPublicKey = FileText.GetContent(this.m_keypairPublicKeyFile);
					ImportKeyPairRequest requestImportKeyPair = new ImportKeyPairRequest(this.m_keypairName, sPublicKey);
					this.m_theEC2Client.importKeyPair(requestImportKeyPair);
				}
				catch (Exception ex)
				{
					File file = new File(this.m_keypairPublicKeyFile);
					Trace.WriteLine(TraceLevel.ERROR, "Public-key file not found: [%s]. It might not be possible to login to instances.", file.getAbsolutePath());
				}
			}
		}
		catch (FileNotFoundException e)
		{
			File file = new File(this.m_credFileAws);
			Trace.WriteLine(TraceLevel.ERROR, "Credentials file not found: [%s].", file.getAbsolutePath());
			this.m_theEC2Client = null;
			throw e;
		}
		catch (IOException e)
		{
			File file = new File(this.m_credFileAws);
			Trace.WriteLine(TraceLevel.ERROR, "Cannot read the credentials file: [%s].", file.getAbsolutePath());
			this.m_theEC2Client = null;
			throw e;
		}
		
		return this.m_theEC2Client;
	}
	
	public CommandResult ScaleCluster(CloudScaleCommand cmd)
	{
		if (cmd.m_cntInstancesDelta > 0)
			this.AddWorkerNodes(cmd.m_topology, cmd.m_strClusterId, cmd.m_cntInstancesDelta);
		else
			this.RemoveWorkerNodes(cmd.m_topology, cmd.m_strClusterId, -1 * cmd.m_cntInstancesDelta);
		CloudScaleCommandResult result = new CloudScaleCommandResult();
		result.SetResultCode(CommandResultCodes.S_OK);
		return result;
	}

	public CommandResult BuildTopology(CloudBuildTopologyCommand cmd)
	{
		this.BuildTopology(cmd.m_topology);
		CloudBuildTopologyCommandResult result = new CloudBuildTopologyCommandResult();
		result.m_topology = cmd.m_topology;
		result.SetResultCode(CommandResultCodes.S_OK);
		return result;
	}
	
	
	@Override
	public CommandResult Execute(Command command)
	{
		if (command instanceof CloudScaleCommand)
		{
			CloudScaleCommand cmd = (CloudScaleCommand)command;
			return this.ScaleCluster(cmd);
		}
		else if (command instanceof CloudBuildTopologyCommand)
		{
			CloudBuildTopologyCommand cmd = (CloudBuildTopologyCommand)command;
			return this.BuildTopology(cmd);
		}
		return null;
	}
	
	/*
	private void AddCloudClient(AWSCredentials awsCredentials, String sRegion, String sEndPoint) throws Exception
	{
		 AmazonEC2Client aClient = new AmazonEC2Client(awsCredentials);
		 aClient.setEndpoint(sEndPoint);
		 this.m_cloudClients.put(sRegion, aClient);
		 
		DescribeKeyPairsRequest requestDescribeKeyPairs = new DescribeKeyPairsRequest();
		DescribeKeyPairsResult resultDescribeKeyPairs = aClient.describeKeyPairs(requestDescribeKeyPairs);
		boolean isKeyAvailable = false;
		for (KeyPairInfo keyPair : resultDescribeKeyPairs.getKeyPairs())
		{
			if (keyPair.getKeyName().equals(this.m_keypairName))
			{
				isKeyAvailable = true;
				break;
			}
		}

		if (false == isKeyAvailable)
		{
			String sPublicKey = FileText.GetContent(this.m_keypairPublicKeyFile);
			ImportKeyPairRequest requestImportKeyPair = new ImportKeyPairRequest(this.m_keypairName, sPublicKey);
			aClient.importKeyPair(requestImportKeyPair);
		}
	}
	*/

	public void WaitForTerminateCompletion(String instanceId) throws Exception
	{
		DescribeInstancesRequest describeRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
		
        for (;;)
        {
            DescribeInstancesResult describeResult = this.GetClient().describeInstances(describeRequest);
            String state = describeResult.getReservations().get(0).getInstances().get(0).getState().getName();
            System.out.println("Status is " + state);
            if ("terminated".equals(state))
            	break; 
        
            Thread.sleep(5000);  // go to sleep and poll again later
        }
	}

	@Override
	protected void StartInstances(List<Node> lstNodes)
	{
		InstanceStarter[] starters = new InstanceStarter[lstNodes.size()];
		
		for (int i = 0; i < lstNodes.size(); ++i)
		{
			starters[i] = new InstanceStarter(lstNodes.get(i));
			starters[i].start();
		}
		
		// wait for all to start
		for (int i = 0; i < starters.length; ++i)
		{
			try
			{
				starters[i].join();
			}
			catch (Exception e) { }
		}
	}

	/*
	protected void StartInstancess01(List<Node> lstNodes)
	{
		ArrayList<String> securityGroup = new ArrayList<String>();
		securityGroup.add("corba");

		RunInstancesRequest request = new RunInstancesRequest("ami-c713ddae", lstNodes.size(), lstNodes.size());
		request.setInstanceType("m1.small");
		request.setSecurityGroups(securityGroup);
		request.setKeyName(this.m_accessSettings.KeyPairName);

		RunInstancesResult result = this.m_theEC2Client.runInstances(request);
		try { Thread.sleep(5000); } catch (Exception e) { }

		int i = 0;
		for (Node node : lstNodes)
		{
			String instanceId = result.getReservation().getInstances().get(i).getInstanceId();
			++i;
			
			try
			{
				this.WaitForStartupCompletion(instanceId);
			}
			catch (Exception ex)
			{
				Trace.WriteException(ex);
			}

			DescribeInstancesRequest describeRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
			DescribeInstancesResult describeResult = this.m_theEC2Client.describeInstances(describeRequest);
			Instance ec2Instance = describeResult.getReservations().get(0).getInstances().get(0);
			
			this.UpdateNode(node, ec2Instance);
			
			CreateTagsRequest createTagsRequest = new CreateTagsRequest();
			createTagsRequest.withResources(instanceId);
			createTagsRequest.withTags(new Tag("type", node.GetType()));
			createTagsRequest.withTags(new Tag("name", node.GetName()));
			//createTagsRequest.withTags(new Tag("cluster", node.GetCluster().GetName()));
			this.m_theEC2Client.createTags(createTagsRequest);
		}
	}
	*/

	@Override
	protected void StopInstances(List<Node> lstNodes)
	{
        ArrayList<String> instancesToStop = new ArrayList<String>(lstNodes.size());
        for (Node node : lstNodes)
        {
        	instancesToStop.add(node.GetId());
        }
		
		TerminateInstancesRequest requestTerminate = new TerminateInstancesRequest(instancesToStop);
		try
		{
			this.GetClient().terminateInstances(requestTerminate);
		}
		catch (IOException e)
		{
			Trace.WriteLine(TraceLevel.ERROR, "Error while getting the cloud client. This should never happen.");
			Trace.WriteException(e);
		}
		/*
		for (String instanceId : instancesToStop)
		{
			try
			{
				//WaitForTerminateCompletion(instanceId);
			}
			catch (Exception e)
			{
				Trace.WriteException(e);
			}
		}
		*/
	}

	private  class InstanceStarter extends Thread
	{
		private Node m_node = null;

		public InstanceStarter(Node node)
		{
			this.m_node = node;
		}
		
		public void WaitForStartupCompletion(String instanceId)
		{
			DescribeInstancesRequest describeRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
	        while (true)
	        {
	        	try
	        	{
		            DescribeInstancesResult describeResult = EC2Manager.this.GetClient().describeInstances(describeRequest);
		            String state = describeResult.getReservations().get(0).getInstances().get(0).getState().getName();
		            if ("running".equals(state))
		            	break;
		            Thread.sleep(5000);  // go to sleep and poll again later
	        	}
	        	catch (Exception ex)
	        	{
	        		// this should happen only when the InstanceId is not available yet.
	        		Trace.WriteException(ex);
	        		try { Thread.sleep(5000); } catch (Exception exInner) { }
	        	}
	        }
		}
		
		private void UpdateNode(Instance ec2Instance)
		{
			this.m_node.SetId(ec2Instance.getInstanceId());
			this.m_node.AddIpAddress("public", ec2Instance.getPublicIpAddress());
			this.m_node.AddIpAddress("private", ec2Instance.getPrivateIpAddress());
		}


		@Override
		public void run()
		{
			try
			{
				ArrayList<String> securityGroup = new ArrayList<String>();
				if (this.m_node.GetSecurity().equals("") == false)
				{
					//securityGroup.add(this.m_node.GetSecurity());// comment for VPC
				}
				else
				{
					//securityGroup.add("default");// comment for VPC
				}
				securityGroup.add("sg-10a4ec6d"); // for VPC
	
				RunInstancesRequest request = new RunInstancesRequest(this.m_node.GetAmi(), 1, 1);
				request.setInstanceType(this.m_node.GetSize());
				//request.setSecurityGroups(securityGroup);// comment for VPC
				request.setSecurityGroupIds(securityGroup);// for VPC
				request.setKeyName(m_keypairName);
				request.setMonitoring(true);
				request.setSubnetId("subnet-c97be7f4");// for VPC
				
				RunInstancesResult result = EC2Manager.this.GetClient().runInstances(request);
				try
				{
					Thread.sleep(10000);
				}
				catch (Exception e) { }
	
				String instanceId = result.getReservation().getInstances().get(0).getInstanceId();
	
				this.WaitForStartupCompletion(instanceId);
	
				DescribeInstancesRequest describeRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
				DescribeInstancesResult describeResult = EC2Manager.this.GetClient().describeInstances(describeRequest);
				Instance ec2Instance = describeResult.getReservations().get(0).getInstances().get(0);
				
				this.UpdateNode(ec2Instance);
				
				CreateTagsRequest createTagsRequest = new CreateTagsRequest();
				createTagsRequest.withResources(instanceId);
				createTagsRequest.withTags(new Tag("Type", this.m_node.GetType()));
				createTagsRequest.withTags(new Tag("Name", this.m_node.GetName()));
				//createTagsRequest.withTags(new Tag("cluster", node.GetCluster().GetName()));
				EC2Manager.this.GetClient().createTags(createTagsRequest);
			}
			catch (Exception e)
			{
				Trace.WriteLine(TraceLevel.ERROR, "Cannot start instances.");
				Trace.WriteException(e);
			}
		}
	}
}
