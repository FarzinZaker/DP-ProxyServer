package Framework.Cloud.Topology;

import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Service
{
	private String m_name = "";
	private String m_id = null;
	private Container m_container = null;
	
	static final String ELEMENT_ROOT = "service";
	static final String ATTRIBUTE_ID = "id";
	static final String ATTRIBUTE_NAME = "name";

	
	Service(String name, String ID)
	{
		this.m_name = name;
		this.m_id = ID;
	}
	
	Service (Service templateService)
	{
		this.m_name = templateService.m_name;
		this.m_id = templateService.m_id;
	}
	
	static Service CreateInstance(Element elemRoot)
	{
		if (false == elemRoot.getLocalName().equals(Service.ELEMENT_ROOT))
		{
			Trace.WriteLine(TraceLevel.ERROR, "Expecting element [%s], but got [%s]", Service.ELEMENT_ROOT, elemRoot.getNodeName());
			return null;
		}

		Service theContainer = new Service(	elemRoot.getAttribute(Service.ATTRIBUTE_NAME),
											elemRoot.getAttribute(Service.ATTRIBUTE_ID));
		
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
				Trace.WriteLine(TraceLevel.WARNING, "Found an unknown element node: [%s].", node.getNodeName());
			}
			else
			{
				Trace.WriteLine(TraceLevel.WARNING, "Found an unknown node of type: [%d].", node.getNodeType());
			}
		}
		
		return theContainer;
	}
	
	public Element SerializeToXml(Document doc)
	{
		Element elemRoot = doc.createElement(ELEMENT_ROOT);
		elemRoot.setAttribute(ATTRIBUTE_NAME, this.m_name);
		elemRoot.setAttribute(ATTRIBUTE_ID, this.m_id);
		
		
		return elemRoot;
	}


	
	void SetContainer(Container container)
	{
		Trace.Assert(container != null, "Cannot set the conainer for the service (name = [%s], ID = [%s]) to null.", this.m_name, this.m_id.toString());

		this.m_container = container;
	}
	
	public String GetId() { return this.m_id; }
	public String GetName() { return this.m_name; }
	public Container GetContainer() { return this.m_container; }
}
