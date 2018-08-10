package Framework.Cloud.EC2;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Element;

import Framework.Diagnostics.Trace;
import ceraslabs.hogna.configuration.IXmlObjectLoader;


public class CloudWatchMonitorLoader implements IXmlObjectLoader
{
	@Override
	public Object Load(Element elem)
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(CloudWatchMonitorBuilder.class);
			Unmarshaller um = context.createUnmarshaller();
			CloudWatchMonitorBuilder builder = (CloudWatchMonitorBuilder)um.unmarshal(elem);
			return builder;
		}
		catch (JAXBException e) { Trace.WriteException(e); }

		return null;
	}
}