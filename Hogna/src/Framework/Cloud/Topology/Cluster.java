package Framework.Cloud.Topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class Cluster
{
	private String m_name = "";
	private String m_id = "";
	private ArrayList<Node> m_nodes = new ArrayList<Node>();
	
	static final String ELEMENT_ROOT = "cluster";
	static final String ATTRIBUTE_NAME = "name";
	static final String ATTRIBUTE_ID = "id";

	Cluster()
	{
		// nothing to do here;
	}
	
	Cluster(String name)
	{
		Trace.Assert(name != null, "The name of the cluster cannot be null.");
		this.m_name = name;
	}
	
	public Node AddNode(Node node)
	{
		Trace.Assert(node != null, "Cannot add a null node to the cluster (name = [%s]).", this.m_name);
		
		this.m_nodes.add(node);
		node.SetCluster(this);
		
		return node;
	}
	
	public Node RemoveNode(Node node)
	{
		Trace.Assert(node != null, "Cannot remove a null node from the cluster (name = [%s]).", this.m_name);
		Trace.Assert(node.GetCluster() == this, "Node [%s] does not belong to cluster [%s]. Cannot remove it.", node.GetName(), this.m_name);
		
		this.m_nodes.remove(node);
		
		return node;
	}
	
	static Cluster CreateInstance(Element elemRoot)
	{
		if (false == elemRoot.getLocalName().equals(Cluster.ELEMENT_ROOT))
		{
			Trace.WriteLine(TraceLevel.ERROR, "Expecting element [%s], but got [%s]", Cluster.ELEMENT_ROOT, elemRoot.getNodeName());
			return null;
		}

		Cluster theCluster = new Cluster();
		theCluster.m_name = elemRoot.getAttribute(Cluster.ATTRIBUTE_NAME);
		theCluster.m_id = elemRoot.getAttribute(Cluster.ATTRIBUTE_ID);
		
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
				if (node.getNodeName().equals(Node.ELEMENT_ROOT))
				{
					theCluster.AddNode(Node.CreateInstance((Element)node));
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
		
		return theCluster;
	}
	
	public int GetSize()
	{
		return this.m_nodes.size();
	}

	public Element SerializeToXml(Document doc)
	{
		Element elemRoot = doc.createElement(ELEMENT_ROOT);
		elemRoot.setAttribute(ATTRIBUTE_NAME, this.m_name);
		elemRoot.setAttribute(ATTRIBUTE_ID, this.m_id);
		
		for (Node node : this.m_nodes)
		{
			elemRoot.appendChild(node.SerializeToXml(doc));
		}
		
		return elemRoot;
	}

	public String GetId() { return this.m_id; }
	public String GetName() { return this.m_name; }
	public List<Node> GetNodes() { return Collections.unmodifiableList(this.m_nodes); }
	
	public Node GetNodeByName(String name)
	{
		for (Node node : this.m_nodes)
		{
			if (node.GetName().equals(name))
			{
				return node;
			}
		}
		return null;
	}
	
	public Node GetNodeById(String sId)
	{
		for (Node node : this.m_nodes)
		{
			if (node.GetId().equals(sId))
			{
				return node;
			}
		}
		return null;
	}
}
