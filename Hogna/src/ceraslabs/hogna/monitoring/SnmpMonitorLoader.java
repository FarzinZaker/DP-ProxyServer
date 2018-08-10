package ceraslabs.hogna.monitoring;

import org.w3c.dom.Element;

import ceraslabs.hogna.configuration.IXmlObjectLoader;

public class SnmpMonitorLoader implements IXmlObjectLoader
{
	@Override
	public Object Load(Element elem)
	{
		String sName = elem.getAttribute("name");
		String sDescription = ((Element)elem.getElementsByTagName("description").item(0)).getTextContent().trim();
		
		//get the connection
		Element elemConnection = (Element)elem.getElementsByTagName("connection").item(0);
		String sNetwork = elemConnection.getAttribute("network");
		String sPort = elemConnection.getAttribute("port");
		int timeout = Integer.parseInt(elemConnection.getAttribute("timeout"));
		int retries = Integer.parseInt(elemConnection.getAttribute("retries"));
		
		//get the OID
		Element elemObject = (Element)elem.getElementsByTagName("object").item(0);
		String sOid = elemObject.getAttribute("oid");
		String sCommunity = elemObject.getAttribute("community");
		
		SnmpMonitorBuilder monitor = new SnmpMonitorBuilder();
		monitor.m_strName = sName;
		monitor.m_strDescription = sDescription;
		monitor.m_strPort = sPort;
		monitor.m_retries = retries;
		monitor.m_timeout = timeout;
		monitor.m_strOid = sOid;
		monitor.m_strCommunity = sCommunity;
		monitor.m_strNetwork = sNetwork;
		
		return monitor;
	}
}
