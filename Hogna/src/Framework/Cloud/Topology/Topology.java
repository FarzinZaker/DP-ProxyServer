package Framework.Cloud.Topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

@XmlRootElement(name = "topology")
@XmlSeeAlso({Cluster.class})
public class Topology
{
	@XmlAttribute(name = "id")
	public String m_id = "";
	@XmlAttribute(name = "name")
	private String m_name = "";
	@XmlElementWrapper(name = "clusters")
	@XmlElement(name = "cluster")
	private List<Cluster> m_clusters = new ArrayList<Cluster>();
	private List<Dependency> m_dependencies = new ArrayList<Dependency>();
	private Map<String, Node> m_templates = new HashMap<String, Node>();

	
	static final String ELEMENT_ROOT = "topology";
	static final String ELEMENT_DEPENDENCIES = "dependencies";
	static final String ELEMENT_DEPENDENCY = "dependency";
	static final String ATTRIBUTE_DEPENDENCY_FROM = "from";
	static final String ATTRIBUTE_DEPENDENCY_TO = "to";
	static final String ATTRIBUTE_NAME = "name";
	static final String ATTRIBUTE_ID = "id";

	public Topology() { }
	
	public String GetName() { return this.m_name; }
	public String GetID() { return this.m_id; }
	public List<Cluster> GetClusters() { return Collections.unmodifiableList(this.m_clusters); }
	public List<Dependency> GetDependencies() { return Collections.unmodifiableList(this.m_dependencies); }
	
	public boolean ContainsCluster(String clusterId)
	{
		for (Cluster aCluster : this.m_clusters)
		{
			if (true == aCluster.GetId().equals(clusterId))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean ContainsCluster(Cluster cluster)
	{
		for (Cluster aCluster : this.m_clusters)
		{
			if (true == aCluster.equals(cluster))
			{
				return true;
			}
		}
		return false;
	}
	
	public Cluster GetCluster(String clusterId)
	{
		for (Cluster aCluster : this.m_clusters)
		{
			if (true == aCluster.GetId().equals(clusterId))
			{
				return aCluster;
			}
		}
		return null;
	}
	
	public Node[] GetAllNodes()
	{
		ArrayList<Node> lstNodes = new ArrayList<>();
		for (Cluster cluster : this.m_clusters)
		{
			lstNodes.addAll(cluster.GetNodes());
		}
		return lstNodes.toArray(new Node[lstNodes.size()]);
	}
	
	public Node GetTemplate(String name)
	{
		return this.m_templates.get(name);
	}
	
	public void BuildTemplateNodes()
	{
		// make a list with all nodes available
		ArrayList<Node> lstNodes = new ArrayList<Node>();

		for (Cluster cluster : this.m_clusters)
		{
			lstNodes.addAll(cluster.GetNodes());
		}
		
		for (Node node : lstNodes)
		{
			if (false == this.m_templates.containsKey(node.GetType()))
			{
				Node tplNode = new Node(node);
				tplNode.SetCluster(node.GetCluster());
				this.m_templates.put(node.GetType(), tplNode);
			}
		}
	}
	
	public static Topology CreateInstance(Element elemRoot)
	{
		if (false == elemRoot.getLocalName().equalsIgnoreCase(Topology.ELEMENT_ROOT))
		{
			Trace.WriteLine(TraceLevel.ERROR, "Expecting element 'topology', but got [%s]", elemRoot.getNodeName());
			return null;
		}
		
		Topology topology = new Topology();
		
		topology.m_name = elemRoot.getAttribute(Topology.ATTRIBUTE_NAME);
		topology.m_id = elemRoot.getAttribute(Topology.ATTRIBUTE_ID);

		NodeList listNodes = elemRoot.getChildNodes();
		for (int i = 0; i < listNodes.getLength(); ++i)
		{
			org.w3c.dom.Node node = listNodes.item(i);
			
			if (node.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
			{
				String elemValue = node.getNodeValue().trim();
				if (elemValue.equals(""))
				{
					// this is an empty text node
					// ignore it and continue
					continue;
				}
				Trace.WriteLine(TraceLevel.WARNING, "Found an unknown text node: [%s].", elemValue);
			}
			else if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
			{
				if (node.getNodeName().equals(Cluster.ELEMENT_ROOT))
				{
					topology.m_clusters.add( Cluster.CreateInstance((Element)node) );
				}
				else if (node.getNodeName().equals(Topology.ELEMENT_DEPENDENCIES))
				{
					NodeList lstDependencies = node.getChildNodes();
					for (int j = 0; j < lstDependencies.getLength(); ++j)
					{
						org.w3c.dom.Node nodeDepend = lstDependencies.item(j);
						if (nodeDepend.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
						{
							if (nodeDepend.getNodeName().equals(Topology.ELEMENT_DEPENDENCY))
							{
								Dependency dependency = new Dependency();
								dependency.idFrom = ((Element)nodeDepend).getAttribute(Topology.ATTRIBUTE_DEPENDENCY_FROM);
								dependency.idTo   = ((Element)nodeDepend).getAttribute(Topology.ATTRIBUTE_DEPENDENCY_TO);
								topology.m_dependencies.add(dependency);
							}
						}
						else
						{
							// ignore empty text nodes and comments.
							if (nodeDepend.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
							{
								String elemValue = nodeDepend.getNodeValue().trim();
								if (elemValue.equals(""))
								{
									// this is an empty text node
									// ignore it and continue
									continue;
								}
								Trace.WriteLine(TraceLevel.WARNING, "Found an unknown text node: [%s].", elemValue);
							}
							else if (nodeDepend.getNodeType() == org.w3c.dom.Node.COMMENT_NODE)
							{
								continue;
							}
							Trace.WriteLine(TraceLevel.WARNING, "Found an unknown element node: [%s].", nodeDepend.getNodeName());
						}
					}
				}
				else
				{
					Trace.WriteLine(TraceLevel.WARNING, "Found an unknown element node: [%s].", node.getNodeName());
				}
			}
			else
			{
				Trace.WriteLine(TraceLevel.WARNING, "Found an unknown node of type: [%d].", node.getNodeType());
			}
		}

		return topology;
	}

	public Element SerializeToXml(Document doc)
	{
		Element elemRoot = doc.createElement(Topology.ELEMENT_ROOT);
		elemRoot.setAttribute(ATTRIBUTE_NAME, this.m_name);
		elemRoot.setAttribute(ATTRIBUTE_ID, this.m_id);
		
		for (Cluster cluster : this.m_clusters)
		{
			elemRoot.appendChild(cluster.SerializeToXml(doc));
		}
		
		org.w3c.dom.Node nodeDependencies = elemRoot.appendChild(doc.createElement(ELEMENT_DEPENDENCIES));
		{
			for (Dependency dependency : this.m_dependencies)
			{
				Element elemDependency = doc.createElement(ELEMENT_DEPENDENCY);
				elemDependency.setAttribute(ATTRIBUTE_DEPENDENCY_FROM, dependency.idFrom);
				elemDependency.setAttribute(ATTRIBUTE_DEPENDENCY_TO, dependency.idTo);
				nodeDependencies.appendChild(elemDependency);
			}
		}
		
		return elemRoot;
	}
}
