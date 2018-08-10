package ceraslabs.hogna.monitoring;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Element;

import Framework.Diagnostics.Trace;
import ceraslabs.hogna.configuration.IXmlObjectLoader;


/**
 * <pre>{@code
 * <monitor name="BusyThreads" type="jmx">
 *   <description>
 *     ...
 *   </description>
 *   <con-network>yorku-net<con-network/>
 *   <con-port>1092</con-port>
 *   <object-name>Catalina:type=ThreadPool,name=http-80</object-name>
 *   <object-attribute>currentThreadsBusy</object-attribute>
 * </monitor>
 * }</pre>
 *
 * @author Cornel
 *
 */
public class JmxMonitorLoader implements IXmlObjectLoader
{
	@Override
	public Object Load(Element elem)
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(JmxMonitorBuilder.class);
			Unmarshaller um = context.createUnmarshaller();
			JmxMonitorBuilder builder = (JmxMonitorBuilder)um.unmarshal(elem);
			return builder;
		}
		catch (JAXBException e) { Trace.WriteException(e); }

		return null;
	}
}
