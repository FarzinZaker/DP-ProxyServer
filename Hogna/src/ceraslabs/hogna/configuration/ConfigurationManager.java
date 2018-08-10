package ceraslabs.hogna.configuration;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class ConfigurationManager
{
	private static ConfigurationManager theManager = new ConfigurationManager();
	
	private Map<String, String> m_appSettings = new HashMap<String, String>();
	private Map<String, ConfigurationSection> m_sections = new HashMap<String, ConfigurationSection>();
	
	public static void Configure(String configFileName)
	{
		Trace.WriteLine(TraceLevel.INFO, "Loading configuration file [%s].", configFileName);
		File configFile = new File(configFileName);
		try
		{
			configFileName = configFile.getCanonicalPath();
		}
		catch (Exception ex)
		{
			Trace.WriteLine(TraceLevel.ERROR, "Could not get the canonical file name for [%s].\n\tError message: [%s].", configFileName, ex.getMessage());
			return;
		}

		if (configFile.exists() == false)
		{
			Trace.WriteLine(TraceLevel.ERROR, "Configuration file [%s] does not exist! Cannot load workload scenarios.", configFileName);
			return;
		}

		Document xmlDoc = null;
		
		// load the configuration file as an XML document.
		try
		{
			DOMParser parser = new DOMParser();
			parser.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
			parser.parse(configFileName);
			xmlDoc = parser.getDocument();
		}
		catch (Exception ex)
		{Trace.WriteException(ex);}

		NodeList nodes = xmlDoc.getDocumentElement().getChildNodes();
		Map<String, String> sectionHandlers = null;
		for (int i = 0; i < nodes.getLength(); ++i)
		{
			// remove comments
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
			if (elemChild.getNodeName().equals("appSettings"))
			{
				ConfigurationManager.theManager.ParseAppSettings(elemChild);
			}
			else if (elemChild.getNodeName().equals("configSections"))
			{
				sectionHandlers = ConfigurationManager.theManager.ParseConfigSections(elemChild);
			}
			else
			{
				ConfigurationManager.theManager.ParseCustomSection(elemChild, sectionHandlers);
			}
		}
	}
	
	private void ParseAppSettings(Element elem)
	{
		NodeList nodesSection = elem.getElementsByTagName("add");
		for (int j = 0; j < nodesSection.getLength(); ++j)
		{
			Element elemSection = (Element)nodesSection.item(j);
			String sKey = elemSection.getAttribute("key");
			String sValue = elemSection.getAttribute("value");
			this.m_appSettings.put(sKey, sValue);
		}
	}
	
	private Map<String, String> ParseConfigSections(Element elem)
	{
		Map<String, String> sectionHandlers = new HashMap<String, String>();
		NodeList nodesSection = elem.getElementsByTagName("section");
		for (int j = 0; j < nodesSection.getLength(); ++j)
		{
			Element elemSection = (Element)nodesSection.item(j);
			String sName = elemSection.getAttribute("name");
			String sType = elemSection.getAttribute("type");
			sectionHandlers.put(sName, sType);
		}
		return sectionHandlers;
	}
	
	private void ParseCustomSection(Element elem, Map<String, String> sectionHandlers)
	{
		String sName = elem.getNodeName();
		String sType = sectionHandlers.get(sName);
		if (sType == null)
		{
			Trace.WriteLine(TraceLevel.ERROR, "No type registered to parse section [%s].", sName);
			return;
		}
		try
		{
			ConfigurationSection configSection = (ConfigurationSection)Class.forName(sType).newInstance();
			configSection.ParseSection(elem);
			this.m_sections.put(sName, configSection);
		}
		catch (ClassNotFoundException e)
		{
			Trace.WriteLine(TraceLevel.ERROR, "Cannot parse section [%s]. Type [%s] not found.", sName, sType);
		}
		catch (Exception ex)
		{
			Trace.WriteLine(TraceLevel.ERROR, "Cannot parse section [%s]. Unknown error.\n\tError message [%s].", sName, ex.getMessage());
		}
	}

	public static String GetSetting(String setting)
	{
		return ConfigurationManager.theManager.m_appSettings.get(setting);
	}
	
	public static ConfigurationSection GetSection(String sSection)
	{
		ConfigurationSection configSection = ConfigurationManager.theManager.m_sections.get(sSection);
		
		Trace.WriteLine (null == configSection, TraceLevel.WARNING, "Configuration section [%s] not found!", sSection);

		return configSection;
	}
}
