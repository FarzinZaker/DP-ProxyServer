package ceraslabs.hogna.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;




import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import ceraslabs.hogna.monitoring.IMonitorBuilder;
import ceraslabs.hogna.monitoring.Monitor;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class MonitorConfigurationSection extends ConfigurationSection
{
	private static final String TAG_ROOT = "monitoring";
	private static final String TAG_SETTINGS = "add";
	private static final String ATTRIBUTE_SETTING_NAME = "key";
	private static final String ATTRIBUTE_SETTING_VALUE = "value";

	private static final String TAG_LOADERS = "loaders";
	private static final String TAG_LOADER  = "loader";
	private static final String ATTRIBUTE_LOADER_TYPE = "type";
	private static final String ATTRIBUTE_LOADER_VALUE = "value";

	private static final String TAG_MONITORS = "monitors";
	private static final String TAG_MONITOR  = "monitor";
	private static final String ATTRIBUTE_MONITOR_TYPE = "type";
	private static final String ATTRIBUTE_MONITOR_NAME = "name";

	private Map<String, String> m_settings = new HashMap<String, String>();
	private IMonitorBuilder[] m_monitors = null;

	@Override
	protected void ParseSection(Element elemSection)
	{
		Trace.Assert (elemSection.getNodeName() == MonitorConfigurationSection.TAG_ROOT,
						"MonitorConfigurationSection received [%s] section instead of [%s]. Did the name of the section changed?",
						elemSection.getNodeName(), MonitorConfigurationSection.TAG_ROOT);
		Trace.WriteLine (TraceLevel.DEBUG, "Parsing configuration section [%s].", MonitorConfigurationSection.TAG_ROOT);

		NodeList nodes = elemSection.getChildNodes();
		Map<String, IXmlObjectLoader> monitorLoaders = null;
		for (int i = 0; i < nodes.getLength(); ++i)
		{
			// remove text nodes, that contain only white spaces
			if (nodes.item(i) instanceof Text && ((Text)nodes.item(i)).getData().trim().length() == 0)
			{
				continue;
			}
			Element elemChild = (Element)nodes.item(i);

			// check if this is the settings element:
			// <add key="..." value="..." />
			if (elemChild.getNodeName().equals (TAG_SETTINGS))
			{
				String sKey = elemChild.getAttribute(ATTRIBUTE_SETTING_NAME);
				String sValue = elemChild.getAttribute(ATTRIBUTE_SETTING_VALUE);
				this.m_settings.put(sKey, sValue);
			}
			else if (elemChild.getNodeName().equals(TAG_LOADERS))
			{
				monitorLoaders = this.ParseMonitorLoaders(elemChild); 
			}
			else if (elemChild.getNodeName().equals(TAG_MONITORS))
			{
				this.ParseMonitors(elemChild, monitorLoaders);
			}
			else
			{
				Trace.WriteLine(TraceLevel.WARNING, "Unknown tag [%s] in the monitor configuration section.", elemChild.getNodeName());
			}
		}
	}
	
	private Map<String, IXmlObjectLoader> ParseMonitorLoaders(Element elem)
	{
		Map<String, IXmlObjectLoader> monitorLoaders = new HashMap<String, IXmlObjectLoader>();
		NodeList nodesSection = elem.getElementsByTagName(TAG_LOADER);
		for (int j = 0; j < nodesSection.getLength(); ++j)
		{
			Element elemSection = (Element)nodesSection.item(j);
			String sType = elemSection.getAttribute(ATTRIBUTE_LOADER_TYPE);
			String sLoader = elemSection.getAttribute(ATTRIBUTE_LOADER_VALUE);
			try
			{
				IXmlObjectLoader loader;
				loader = (IXmlObjectLoader) Class.forName(sLoader).newInstance();
				monitorLoaders.put(sType, loader);
			}
			catch (ClassNotFoundException e)
			{
				Trace.WriteLine(TraceLevel.ERROR, "Cannot instantiate monitor loader [%s]. Type not found.", sLoader);
			}
			catch (Exception e)
			{
				Trace.WriteLine(TraceLevel.ERROR, "Cannot instantiate monitor loader [%s]. Unknown error.\n\tError message: [%s].", sLoader, e.toString());
			}
		}
		return monitorLoaders;
	}
	
	private void ParseMonitors(Element elem, Map<String, IXmlObjectLoader> monitorLoaders)
	{
		NodeList nodesMonitor = elem.getElementsByTagName(TAG_MONITOR);
		ArrayList<IMonitorBuilder> lstMonitors = new ArrayList<>();
		for (int i = 0; i < nodesMonitor.getLength(); ++i)
		{
            Element elemMonitor = (Element) nodesMonitor.item(i);
            String sType = elemMonitor.getAttribute(ATTRIBUTE_MONITOR_TYPE);
            IXmlObjectLoader loader = monitorLoaders.get(sType);
            if (loader != null)
            {
            	//listMonitors.add((Monitor)loader.Load(elemMonitor));
            	lstMonitors.add((IMonitorBuilder)loader.Load(elemMonitor));
            }
            else
            {
            	Trace.WriteLine(TraceLevel.ERROR, "Cannot load monitor [%s], no loader for type [%s].",
            										elemMonitor.getAttribute(ATTRIBUTE_MONITOR_NAME),
            										elemMonitor.getAttribute(ATTRIBUTE_MONITOR_TYPE));
            }
		}
		this.m_monitors = lstMonitors.toArray(new IMonitorBuilder[lstMonitors.size()]);
	}
	
	public IMonitorBuilder[] GetMonitorBuilders() { return this.m_monitors; }
	
	public IMonitorBuilder GetMonitorBuilder(String name)
	{
		for (IMonitorBuilder monitor : this.m_monitors)
		{
			if (monitor.GetName().equals(name))
			{
				return monitor;
			}
		}
		return null;
	}
	
	public int RegisterMonitors(String[] sNames)
	{
		Trace.WriteNotImplemented();
		return 0;
	}
}
