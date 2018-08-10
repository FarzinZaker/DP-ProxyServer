package ceraslabs.hogna.utilities.Xml;

import org.w3c.dom.Node;

public class XmlSerializer
{
	public static String XmlAsString(Node node)
	{
		try
		{
	      javax.xml.transform.TransformerFactory transfac = javax.xml.transform.TransformerFactory.newInstance();
	      javax.xml.transform.Transformer trans = transfac.newTransformer();
	      trans.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
	      trans.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
	      trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

	      // Print the DOM node

	      java.io.StringWriter sw = new java.io.StringWriter();
	      javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(sw);
	      javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(node);
	      trans.transform(source, result);
	      String xmlString = sw.toString();

	      return xmlString;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
}
