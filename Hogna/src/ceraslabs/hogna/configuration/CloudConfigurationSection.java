package ceraslabs.hogna.configuration;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import Framework.Cloud.IConfigHelper;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class CloudConfigurationSection extends ConfigurationSection
{
	private static final String TAG_ROOT = "cloud";

	private static final String TAG_HELPERS = "configHelpers";
	private static final String TAG_HELPER = "helper";
	private static final String ATTRIBUTE_LOADER_TYPE = "type";
	private static final String ATTRIBUTE_LOADER_SERVICE = "serviceId";

	private Map<String, IConfigHelper> m_mapConfigHelpers = new HashMap<String, IConfigHelper>();
	
	Map<String, IConfigHelper> GetConfigHelpers()
	{
		return this.m_mapConfigHelpers;
	}

	
	@Override
	protected void ParseSection(Element elemSection)
	{
		Trace.Assert (elemSection.getNodeName() == TAG_ROOT,
				"EC2ConfigurationSection received [%s] section instead of [%s]. Did the name of the section changed?",
				elemSection.getNodeName(), TAG_ROOT);
		Trace.WriteLine (TraceLevel.DEBUG, "Parsing configuration section [%s].", TAG_ROOT);

		NodeList nodes = elemSection.getChildNodes();
		for (int i = 0; i < nodes.getLength(); ++i)
		{
			// ignore comments
			if (nodes.item(i) instanceof Comment)
			{
				continue;
			}
			// remove text nodes, that contain only white spaces
			if (nodes.item(i) instanceof Text && ((Text)nodes.item(i)).getData().trim().length() == 0)
			{
				continue;
			}
			Element elemChild = (Element)nodes.item(i);

			if (elemChild.getNodeName().equals (TAG_HELPERS))
			{
				this.ParseConfigHelpers(elemChild);
			}
		}
	}
	
	private void ParseConfigHelpers(Element elem)
	{
		NodeList nodesHelpers = elem.getElementsByTagName(TAG_HELPER);
		for (int i = 0; i < nodesHelpers.getLength(); ++i)
		{
			Element elemHelper = (Element)nodesHelpers.item(i);
			String sType = elemHelper.getAttribute(ATTRIBUTE_LOADER_TYPE);
			String sService = elemHelper.getAttribute(ATTRIBUTE_LOADER_SERVICE);
			
			try
			{
				IConfigHelper helper = (IConfigHelper)Class.forName(sType).newInstance();
				this.m_mapConfigHelpers.put(sService, helper);
			}
			catch (ClassNotFoundException e)
			{
				Trace.WriteLine(TraceLevel.ERROR, "Cannot instantiate configuration helper [%s]. Type not found.", sType);
			}
			catch (Exception e)
			{
				Trace.WriteLine(TraceLevel.ERROR, "Cannot instantiate configuration helper [%s]. Unknown error.\n\tError message: [%s].", sType, e.toString());
			}
		}
	}
}
