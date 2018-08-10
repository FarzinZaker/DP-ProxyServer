package Framework.Cloud.Topology;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class Container
{
	private String m_name = "";
	private String m_id = "";
	private ArrayList<Service> m_services = new ArrayList<Service>();
	private Node m_node = null;

	static final String ELEMENT_ROOT = "container";
	static final String ATTRIBUTE_ID = "id";
	static final String ATTRIBUTE_NAME = "name";

	Container(String name, String ID)
	{
		this.m_name = name;
		this.m_id = ID;
	}
	
	Container(Container templateContainer)
	{
		this.m_name = templateContainer.m_name + " (copy: " + System.currentTimeMillis() + ")";
		this.m_id = templateContainer.m_id;
		
		for (Service service : templateContainer.m_services)
		{
			this.AddService(new Service(service));
		}
	}

	static Container CreateInstance(Element elemRoot)
	{
		if (false == elemRoot.getLocalName().equals(Container.ELEMENT_ROOT))
		{
			Trace.WriteLine(TraceLevel.ERROR, "Expecting element [%s], but got [%s]", Container.ELEMENT_ROOT, elemRoot.getNodeName());
			return null;
		}

		Container theContainer = new Container(	elemRoot.getAttribute(Container.ATTRIBUTE_NAME),
												elemRoot.getAttribute(Container.ATTRIBUTE_ID));
		
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
				if (node.getNodeName().equals(Service.ELEMENT_ROOT))
				{
					theContainer.m_services.add( Service.CreateInstance((Element)node) );
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
		
		return theContainer;
	}

	
	void SetNode(Node node)
	{
		Trace.Assert(node != null, "Cannot set the node for the container (name = [%s], ID = [%s]) to null.", this.m_name, this.m_id.toString());

		this.m_node = node;
	}
	
	private Service AddServiceInternal(Service service)
	{
		this.m_services.add(service);
		return service;
	}
	
	public Service AddService(Service service)
	{
		Trace.Assert(service != null, "Cannot add a null service to container (name = [%s], ID = [%s]).", this.m_name, this.m_id.toString());

		return this.AddServiceInternal(service);
	}
	
	public String GetID() { return this.m_id; }
	public String GetName() { return this.m_name; }
	public Node GetNode() { return this.m_node; }
	public List<Service> GetServices() { return this.m_services; }

	public Element SerializeToXml(Document doc)
	{
		Element elemRoot = doc.createElement(ELEMENT_ROOT);
		elemRoot.setAttribute(ATTRIBUTE_NAME, this.m_name);
		elemRoot.setAttribute(ATTRIBUTE_ID, this.m_id);
		
		for (Service service : this.m_services)
		{
			elemRoot.appendChild(service.SerializeToXml(doc));
		}
		
		return elemRoot;
	}
}
