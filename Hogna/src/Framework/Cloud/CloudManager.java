package Framework.Cloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Framework.Cloud.Topology.Cluster;
import Framework.Cloud.Topology.Container;
import Framework.Cloud.Topology.Node;
import Framework.Cloud.Topology.Service;
import Framework.Cloud.Topology.Topology;
import Framework.Cloud.Topology.Dependency;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public abstract class CloudManager
{
	ArrayList<Topology> m_topologies = new ArrayList<Topology>();
	Map<String, IConfigHelper> m_configHelpers = new HashMap<String, IConfigHelper>();
	
	protected final Topology AddTopology(Topology topology)
	{
		this.m_topologies.add(topology);
		return topology;
	}


	protected abstract void StartInstances(List<Node> lstNodes);
	protected abstract void StopInstances(List<Node> lstNodes);


	protected final IConfigHelper AddConfigHelper(String id, IConfigHelper configHelper)
	{
		this.m_configHelpers.put(id, configHelper);
		return configHelper;
	}

	protected final void ConfigureNode(Node node)
	{
		for (Container container : node.GetContainers())
		{
			IConfigHelper configHelper = this.m_configHelpers.get(container.GetID());
			if (configHelper != null)
			{
				configHelper.Configure(node);
			}
			
			for (Service service : container.GetServices())
			{
				configHelper = this.m_configHelpers.get(service.GetId());
				if (configHelper != null)
				{
					configHelper.Configure(node);
				}
			}
		}
	}
	
	public void BuildTopology(Topology topology)
	{
		ArrayList<Node> lstNodes = new ArrayList<Node>();

		for (Cluster cluster : topology.GetClusters())
		{
			lstNodes.addAll(cluster.GetNodes());
		}
		
		this.StartInstances(lstNodes);
		for (Node node : lstNodes)
		{
			this.ConfigureNode(node);
		}
		
		this.MakeDepend(topology.GetDependencies(), new ArrayList<Node>(), lstNodes);

		this.AddTopology(topology);
	}

	public void AddNode(Topology topology, Cluster cluster, List<Node> lstNodes)
	{
		// validate input
		if (false == this.m_topologies.contains(topology))
		{
			Trace.WriteLine(TraceLevel.ERROR, "Cannot add nodes: topology [%s] not found.", topology.GetName());
			return;
		}
		else if (false == topology.ContainsCluster(cluster))
		{
			Trace.WriteLine(TraceLevel.ERROR, "Cannot add nodes: cluster [%s] not found in topology [%s].", cluster.GetName(), topology.GetName());
			return;
		}
		
		this.StartInstances(lstNodes);
		
		// start configuring nodes
		for (Node node : lstNodes)
		{
			this.ConfigureNode(node);
			// the node is configured, can be added to the cluster
			cluster.AddNode(node);
		}
	}
	
	public void AddNodes(Topology theTopology, String type, int count)
	{
		Node nodeTemplate = theTopology.GetTemplate(type);
		Cluster cluster = nodeTemplate.GetCluster();
		
		List<Node> lstExistingNodes = new ArrayList<Node>();
		for (Cluster cl : theTopology.GetClusters())
		{
			lstExistingNodes.addAll(cl.GetNodes());
		}
		
		List<Node> lstNodes = new ArrayList<Node>(count);
		for (int i = 0; i < count; ++i)
		{
			lstNodes.add(new Node(nodeTemplate));
		}
		
		this.StartInstances(lstNodes);
		
		// start configuring nodes
		for (Node node : lstNodes)
		{
			this.ConfigureNode(node);

			// the node is configured, can be added to the cluster
			cluster.AddNode(node);
		}

		this.MakeDepend(theTopology.GetDependencies(), lstExistingNodes, lstNodes);
	}

	/**
	 * Creates a single dependency between one source and multiple destinations.
	 * <p>
	 * This method will select the correct IConfigHelper for the specified
	 * service and invoke the <code>AddDependency(Node, List&lt;Node&gt;)</code> function.
	 * 
	 * @param serviceId
	 *                    the ID of the service running on node <code>depFrom</code>
	 *                    which depends on other nodes.
	 * @param depFrom
	 *                    a node running a service that depends on other services.
	 * @param depTo
	 *                    a list of nodes upon which the service <code>serviceId<code>
	 *                    depends.
	 */
	protected final void MakeDependency(String serviceId, Node depFrom, List<Node> depTo)
	{
		IConfigHelper configHelper = this.m_configHelpers.get(serviceId);
		if (configHelper != null)
		{
			configHelper.AddDependency(depFrom, depTo);
		}
	}

	/**
	 * Creates dependencies between nodes.
	 * <p>
	 * The assumption is that <code>lstNodesUnconnected</code> are new nodes, and
	 * <code>lstNodesConnected</code> are existing nodes in the topology, already
	 * connected.
	 * <p>
	 * This method will create all the dependencies between nodes in
	 * <code>lstNodesUnconnected</code>, and all dependencies between nodes in
	 * <code>lstNodesUnconnected</code> and nodes in <code>lstNodesConnected</code>.
	 * <p>
	 * There are four types of possible dependencies:
	 * <ol>
	 *     <li> <code>lstNodesUnconnected</code>  -->    <code>lstNodesUnconnected</code>
	 *     <li> <code>lstNodesUnconnected</code>  -->    <code>lstNodesConnected</code>
	 *     <li> <code>lstNodesConnected</code>    -->    <code>lstNodesUnconnected</code>
	 *     <li> <code>lstNodesConnected</code>    -->    <code>lstNodesConnected</code>
	 * </ol>
	 * This method will create only connections of the first three types.
	 * 
	 * @param lstNodesConnected
	 *                    nodes that already are already connected with each other
	 * @param lstNodesUnconnected
	 *                    nodes that are not connected yet with anything
	 */
	public void MakeDepend(List<Dependency> dependencies, List<Node> lstNodesConnected, List<Node> lstNodesUnconnected)
	{
		ArrayList<Node> lstFrom = new ArrayList<Node>(); // for connections of type 1 AND 2
		ArrayList<Node> lstTo = new ArrayList<Node>(); // for connection of type 1 AND 2
		ArrayList<Node> lstFromImportant = new ArrayList<Node>(); // for connection of type 3
		ArrayList<Node> lstToImportant = new ArrayList<Node>(); // for connection of type 3

		for (Dependency dependency : dependencies)
		{
			lstFrom.clear();
			lstTo.clear();
			lstFromImportant.clear();
			lstToImportant.clear();

			for (Node node : lstNodesUnconnected)
			{
				for (Container container : node.GetContainers())
				{
					for (Service service : container.GetServices())
					{
						if (dependency.idFrom.equals(service.GetId()))
						{
							lstFrom.add(node); // connection of type 1 OR 2
						}
						else if (dependency.idTo.equals(service.GetId()))
						{
							lstTo.add(node); // connection of type 1
							lstToImportant.add(node); // connection of type 3
						}
					}
				}
			}
			
			for (Node node : lstNodesConnected)
			{
				for (Container container : node.GetContainers())
				{
					for (Service service : container.GetServices())
					{
						if (dependency.idFrom.equals(service.GetId()))
						{
							lstFromImportant.add(node); // connection of type 3
						}
						else if (dependency.idTo.equals(service.GetId()))
						{
							lstTo.add(node); // connection of type 2
						}
					}
				}
			}

			if (lstTo.size() > 0)
			{
				for (Node node : lstFrom)
				{
					this.MakeDependency(dependency.idFrom, node, lstTo);
				}
			}
			if (lstToImportant.size() > 0)
			{
				for (Node node : lstFromImportant)
				{
					this.MakeDependency(dependency.idFrom, node, lstToImportant);
				}
			}
		}
	}

	protected final void BreakDependency(String serviceId, Node depFrom, List<Node> depTo)
	{
		IConfigHelper configHelper = this.m_configHelpers.get(serviceId);
		if (configHelper != null)
		{
			configHelper.RemoveDependency(depFrom, depTo);
		}
	}
	
	public void BreakDepend(List<Dependency> dependencies, List<Node> lstNodesConnected, List<Node> lstNodesToDisconnect)
	{
		List<Node> lstFrom = new ArrayList<Node>();
		List<Node> lstTo = new ArrayList<Node>();
		
		for (Dependency dependency : dependencies)
		{
			lstFrom.clear();
			lstTo.clear();

			for (Node node : lstNodesConnected)
			{
				for (Container container : node.GetContainers())
				{
					for (Service service : container.GetServices())
					{
						if (dependency.idFrom.equals(service.GetId()))
						{
							lstFrom.add(node);
						}
					}
				}
			}
			
			for (Node node : lstNodesToDisconnect)
			{
				for (Container container : node.GetContainers())
				{
					for (Service service : container.GetServices())
					{
						if (dependency.idTo.equals(service.GetId()))
						{
							lstTo.add(node);
						}
					}
				}
			}
			
			if (lstTo.size() > 0)
			{
				for (Node node : lstFrom)
				{
					this.BreakDependency(dependency.idFrom, node, lstTo);
				}
			}
		}
	}
	
	private void AddWorkerNodesPrivate(Topology topology, Cluster cluster, int howMany)
	{
		Node templateWorker = null;
		for (Node node : cluster.GetNodes())
		{
			if (node.GetType().equals("worker"))
			{
				templateWorker = node;
				break;
			}
		}

		Trace.Assert(templateWorker != null, "There are no worker nodes in cluster [%s]. Cannot add more worker nodes.", cluster.GetName());

		List<Node> lstExistingNodes = new ArrayList<Node>();
		for (Cluster cl : topology.GetClusters())
		{
			lstExistingNodes.addAll(cl.GetNodes());
		}
		
		List<Node> lstNodes = new ArrayList<Node>(howMany);
		for (int i = 0; i < howMany; ++i)
		{
			lstNodes.add(new Node(templateWorker));
		}
		
		this.StartInstances(lstNodes);
		
		// start configuring nodes
		for (Node node : lstNodes)
		{
			this.ConfigureNode(node);

			// the node is configured, can be added to the cluster
			cluster.AddNode(node);
		}

		this.MakeDepend(topology.GetDependencies(), lstExistingNodes, lstNodes);
	}
	
	private void RemoveWorkerNodesPrivate(Topology topology, Cluster cluster, int howMany)
	{
		List<Node> lstNodes = new ArrayList<Node>(howMany);
		int i = 0;
		for (Node node : cluster.GetNodes())
		{
			if (node.GetType().equals("worker"))
			{
				lstNodes.add(node);
				++i;
				
				if (i >= howMany)
				{
					break;
				}
			}
		}

		for (Node node : lstNodes)
		{
			cluster.RemoveNode(node);
		}

		List<Node> lstNodesConnected = new ArrayList<Node>();
		for (Cluster cl : topology.GetClusters())
		{
			for (Node node : cl.GetNodes())
			{
				lstNodesConnected.add(node);
			}
		}
		this.BreakDepend(topology.GetDependencies(), lstNodesConnected, lstNodes);

		this.StopInstances(lstNodes);
	}
	
	public void AddWorkerNodes(Topology topology, String clusterId, int howMany)
	{
		// validate input
		if (false == this.m_topologies.contains(topology))
		{
			Trace.WriteLine(TraceLevel.ERROR, "Cannot add nodes: topology [%s] not found.", topology.GetName());
			return;
		}

		// search for the cluster
		for (Cluster cluster : topology.GetClusters())
		{
			if (cluster.GetId().equals(clusterId))
			{
				this.AddWorkerNodesPrivate(topology, cluster, howMany);
				return;
			}
		}
		
		// no cluster found
		Trace.WriteLine(TraceLevel.ERROR, "Cannot add nodes: cluster [%s] not found in topology [%s].", clusterId, topology.GetName());
	}
	
	public void AddWorkerNodes(Topology topology, Cluster cluster, int howMany)
	{
		// validate input
		if (false == this.m_topologies.contains(topology))
		{
			Trace.WriteLine(TraceLevel.ERROR, "Cannot add nodes: topology [%s] not found.", topology.GetName());
			return;
		}
		else if (false == topology.ContainsCluster(cluster))
		{
			Trace.WriteLine(TraceLevel.ERROR, "Cannot add nodes: cluster [%s] not found in topology [%s].", cluster.GetName(), topology.GetName());
			return;
		}

		this.AddWorkerNodesPrivate(topology, cluster, howMany);
	}
	
	public void RemoveWorkerNodes(Topology topology, String clusterId, int howMany)
	{
		// validate input
		if (false == this.m_topologies.contains(topology))
		{
			Trace.WriteLine(TraceLevel.ERROR, "Cannot remove nodes: topology [%s] not found.", topology.GetName());
			return;
		}
		
		// search for the cluster
		for (Cluster cluster : topology.GetClusters())
		{
			if (cluster.GetId().equals(clusterId))
			{
				this.RemoveWorkerNodesPrivate(topology, cluster, howMany);
				return;
			}
		}

		// no cluster found
		Trace.WriteLine(TraceLevel.ERROR, "Cannot remove nodes: cluster [%s] not found in topology [%s].", clusterId, topology.GetName());
	}
	
	public void RemoveWorkerNodes(Topology topology, Cluster cluster, int howMany)
	{
		// validate input
		if (false == this.m_topologies.contains(topology))
		{
			Trace.WriteLine(TraceLevel.ERROR, "Cannot remove nodes: topology [%s] not found.", topology.GetName());
			return;
		}
		else if (false == topology.ContainsCluster(cluster))
		{
			Trace.WriteLine(TraceLevel.ERROR, "Cannot remove nodes: cluster [%s] not found in topology [%s].", cluster.GetName(), topology.GetName());
			return;
		}
		
		this.RemoveWorkerNodesPrivate(topology, cluster, howMany);
	}
}
