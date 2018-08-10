package ceraslabs.hogna.monitoring;

import org.w3c.dom.Element;

import ceraslabs.hogna.configuration.IXmlObjectLoader;

public class CorproMonitorLoader implements IXmlObjectLoader
{
	private static final String ATTRIBUTE_NAME = "name";
	
	@Override
	public Object Load(Element elem)
	{
		String sName = elem.getAttribute(ATTRIBUTE_NAME);
		String sDescription = ((Element)elem.getElementsByTagName("description").item(0)).getTextContent().trim();

		//get the connection
		Element elemConnection = (Element)elem.getElementsByTagName("connection").item(0);
		String sNetwork  = elemConnection.getAttribute("network");
		String sPort = elemConnection.getAttribute("port");
		int timeout = Integer.parseInt(elemConnection.getAttribute("timeout"));
		int retries = Integer.parseInt(elemConnection.getAttribute("retries"));

		CorproMonitorBuilder builder = new CorproMonitorBuilder();
		builder.m_strName = sName;
		builder.m_strDescription = sDescription;
		builder.m_strNetwork = sNetwork;
		builder.m_strPort = sPort;
		builder.m_timeout = timeout;
		builder.m_retires = retries;

		return builder;
	}
}
