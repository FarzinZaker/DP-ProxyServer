package Framework.Cloud.Topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class Node
{
	private String m_id = "";
	private String m_name = "";
	private String m_type =""; // worker | monitor | balancer
	private String m_ami = "";
	private String m_publicIP = "";
	private String m_privateIP = "";
	private String m_region = "";
	private String m_size = "";
	private String m_security = "";
	private String[] m_monitors = null;
	private ArrayList<Container> m_containers = new ArrayList<Container>();
	private Cluster m_cluster = null;

	// not saved in the configuration file (yet) 
	private String m_strKeyName = "";
	private String m_strPrivateKeyFile = "";
	private String m_strPublicKeyFile = "";

	
	private Map<String, String> m_mapIpAddresses = new HashMap<String, String>();
	

	static final String ELEMENT_ROOT = "node";
	static final String ATTRIBUTE_ID = "id";
	static final String ATTRIBUTE_NAME = "name";
	static final String ATTRIBUTE_TYPE = "type";
	static final String ATTRIBUTE_AMI = "ami";
	static final String ATTRIBUTE_PUBLIC_IP = "publicIP";
	static final String ATTRIBUTE_PRIVATE_IP = "privateIP";
	static final String ATTRIBUTE_REGION = "region";
	static final String ATTRIBUTE_SIZE = "size";
	static final String ATTRIBUTE_SECURITY = "security";
	static final String ATTRIBUTE_MONITORS = "monitors";
	
	Node () { }

	Node(String name)
	{
		Trace.Assert(name != null, "Cannot create a node with a null name.");
		this.m_name = name;
	}
	
	public Node (Node templateNode)
	{
		this.m_name     = templateNode.m_name + " (copy: " + System.currentTimeMillis() + ")";
		this.m_type     = templateNode.m_type;
		this.m_ami      = templateNode.m_ami;
		this.m_region   = templateNode.m_region;
		this.m_size     = templateNode.m_size;
		this.m_security = templateNode.m_security;
		this.m_strKeyName = templateNode.m_strKeyName;
		this.m_strPrivateKeyFile = templateNode.m_strPrivateKeyFile;
		this.m_strPublicKeyFile = templateNode.m_strPublicKeyFile;
		
		this.m_monitors = templateNode.m_monitors;
//		this.m_monitors = new String[templateNode.m_monitors.length];
//		for (int i = 0; i < templateNode.m_monitors.length; ++i)
//		{
//			this.m_monitors[i] = templateNode.m_monitors[i];
//		}

		for (Container container : templateNode.m_containers)
		{
			this.m_containers.add(new Container(container));
		}
	}

	/**
	 * Sets the public IP of the node.
	 *
	 * @deprecated use {@link #AddIpAddress("public", "10.10.10.10")} instead.  
	 */
	@Deprecated
	public void SetPublicIp(String ip)
	{
		this.m_publicIP = ip;
		this.AddIpAddress("public", ip);
	}
	/**
	 * Sets the private IP of the node.
	 *
	 * @deprecated use {@link #AddIpAddress("private", "10.10.10.10")} instead.  
	 */
	@Deprecated
	public void SetPrivateIp(String ip)
	{
		this.m_privateIP = ip;
		this.AddIpAddress("private", ip);
	}
	public void SetId(String id) { this.m_id = id; }
	public void SetKeyName(String strKeyName) { this.m_strKeyName = strKeyName; }
	public void SetPrivateKeyFile(String strPrivateKeyFile) { this.m_strPrivateKeyFile = strPrivateKeyFile; }
	public void SetPublicKeyFile(String strPublicKeyFile) { this.m_strPublicKeyFile = strPublicKeyFile; }

	
	
	public void AddIpAddress(String strNetworkId, String strIp)
	{
		this.m_mapIpAddresses.put(strNetworkId, strIp);
	}
	
	public String GetIpAddress(String strNetworkId)
	{
		if (this.m_mapIpAddresses.containsKey(strNetworkId))
			return this.m_mapIpAddresses.get(strNetworkId);
		return "";
	}
	
	
	///**
	// * Gets the public IP of the node.
	// *
	// * @deprecated use {@link #GetIpAddress("public")} instead.  
	// */
	//@Deprecated
	//public String GetPublicIp()  { return this.m_publicIP;  }
	///**
	// * Gets the private IP of the node.
	// *
	// * @deprecated use {@link #GetIpAddress("private")} instead.  
	// */
	//@Deprecated
	//public String GetPrivateIp() { return this.m_privateIP; }
	public String GetName()      { return this.m_name;      }
	public String GetId()        { return this.m_id;        }
	public String GetAmi()       { return this.m_ami;       }
	public String GetType()      { return this.m_type;      }
	public String GetRegion()    { return this.m_region;    }
	public String GetSize()      { return this.m_size;      }
	public String GetSecurity()  { return this.m_security;  }
	
	public String GetKeyName() { return this.m_strKeyName; }
	public String GetPrivateKeyFile() { return this.m_strPrivateKeyFile; }
	public String GetPublicKeyFile() { return this.m_strPublicKeyFile; }
	
	public List<Container> GetContainers() { return this.m_containers; }
	public Cluster GetCluster() { return this.m_cluster; }
	
	public String[] GetMonitorNames() { return this.m_monitors; }

	void SetCluster(Cluster cluster)
	{
		Trace.Assert(cluster != null, "Node (name = [%s]) cannot be added to a null cluster.", this.m_name);
//		Trace.WriteLine(null != this.m_cluster, TraceLevel.WARNING, "Changing node [%s] from cluster [%s] to cluster [%s].",
//						this.m_name, this.m_cluster.GetName(), cluster.GetName());

		this.m_cluster = cluster;
	}

	static Node CreateInstance(Element elemRoot)
	{
		if (false == elemRoot.getLocalName().equals(Node.ELEMENT_ROOT))
		{
			Trace.WriteLine(TraceLevel.ERROR, "Expecting element [%s], but got [%s]", Node.ELEMENT_ROOT, elemRoot.getNodeName());
			return null;
		}

		Node theNode = new Node();
		theNode.m_id        = elemRoot.getAttribute(Node.ATTRIBUTE_ID);
		theNode.m_name      = elemRoot.getAttribute(Node.ATTRIBUTE_NAME);
		theNode.m_type      = elemRoot.getAttribute(Node.ATTRIBUTE_TYPE);
		theNode.m_ami       = elemRoot.getAttribute(Node.ATTRIBUTE_AMI);
		theNode.m_publicIP  = elemRoot.getAttribute(Node.ATTRIBUTE_PUBLIC_IP);
		theNode.m_privateIP = elemRoot.getAttribute(Node.ATTRIBUTE_PRIVATE_IP);
		theNode.m_region    = elemRoot.getAttribute(Node.ATTRIBUTE_REGION);
		theNode.m_size      = elemRoot.getAttribute(Node.ATTRIBUTE_SIZE);
		theNode.m_security  = elemRoot.getAttribute(Node.ATTRIBUTE_SECURITY);
		if (elemRoot.getAttribute(Node.ATTRIBUTE_MONITORS) != "")
		{
			theNode.m_monitors  = elemRoot.getAttribute(Node.ATTRIBUTE_MONITORS).split(" *, *");
		}
		
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
				if (node.getNodeName().equals(Container.ELEMENT_ROOT))
				{
					theNode.m_containers.add( Container.CreateInstance((Element)node) );
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
		
		return theNode;
	}

	public Element SerializeToXml(Document doc)
	{
		Element elemRoot = doc.createElement(ELEMENT_ROOT);
		elemRoot.setAttribute(ATTRIBUTE_NAME,       this.m_name);
		elemRoot.setAttribute(ATTRIBUTE_ID,         this.m_id);
		elemRoot.setAttribute(ATTRIBUTE_TYPE,       this.m_type);
		elemRoot.setAttribute(ATTRIBUTE_AMI,        this.m_ami);
		elemRoot.setAttribute(ATTRIBUTE_PUBLIC_IP,  this.m_publicIP);
		elemRoot.setAttribute(ATTRIBUTE_PRIVATE_IP, this.m_privateIP);
		elemRoot.setAttribute(ATTRIBUTE_REGION,     this.m_region);
		elemRoot.setAttribute(ATTRIBUTE_SIZE,       this.m_size);
		elemRoot.setAttribute(ATTRIBUTE_SECURITY,   this.m_security);
		
		for (Container container : this.m_containers)
		{
			elemRoot.appendChild(container.SerializeToXml(doc));
		}
		
		return elemRoot;
	}

}
