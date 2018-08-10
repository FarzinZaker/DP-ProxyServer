package ceraslabs.hogna.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import ceraslabs.hogna.workload.Scenario;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class WorkloadConfigurationSection extends ConfigurationSection
{
	Map<String, String> m_settings = new HashMap<String, String>();
	Scenario[] m_scenarios = null;

	@Override
	protected void ParseSection(Element elemSection)
	{
		Trace.WriteLine(TraceLevel.DEBUG, "Parsing section [%s].", elemSection.getNodeName());

		NodeList nodes = elemSection.getChildNodes();
		Map<String, IXmlObjectLoader> scenarioLoaders = null;
		for (int i = 0; i < nodes.getLength(); ++i)
		{
			// remove text nodes, that contain only white spaces
			if (nodes.item(i) instanceof Text && ((Text)nodes.item(i)).getData().trim().length() == 0)
			{
				continue;
			}
			Element elemChild = (Element)nodes.item(i);
			if (elemChild.getNodeName().equals("add"))
			{
				String sKey = elemChild.getAttribute("key");
				String sValue = elemChild.getAttribute("value");
				this.m_settings.put(sKey, sValue);
			}
			else if (elemChild.getNodeName().equals("loaders"))
			{
				scenarioLoaders = this.ParseScenarioLoaders(elemChild); 
			}
			else if (elemChild.getNodeName().equals("scenarios"))
			{
				this.ParseScenarios(elemChild, scenarioLoaders);
			}
			else
			{
				Trace.WriteLine(TraceLevel.WARNING, "Unknown tag [%s] in the workload section.", elemChild.getNodeName());
			}
		}
	}
	
	private Map<String, IXmlObjectLoader> ParseScenarioLoaders(Element elem)
	{
		Map<String, IXmlObjectLoader> scenarioLoaders = new HashMap<String, IXmlObjectLoader>();
		NodeList nodesSection = elem.getElementsByTagName("loader");
		for (int j = 0; j < nodesSection.getLength(); ++j)
		{
			Element elemSection = (Element)nodesSection.item(j);
			String sType = elemSection.getAttribute("type");
			String sLoader = elemSection.getAttribute("value");
			try
			{
				IXmlObjectLoader loader;
				loader = (IXmlObjectLoader) Class.forName(sLoader).newInstance();
				scenarioLoaders.put(sType, loader);
			}
			catch (ClassNotFoundException e)
			{
				Trace.WriteLine(TraceLevel.ERROR, "Cannot instantiate scenario loader [%s]. Type not found.", sLoader);
			}
			catch (Exception e)
			{
				Trace.WriteLine(TraceLevel.ERROR, "Cannot instantiate scenario loader [%s]. Unknown error.\n\tError message: [%s].", sLoader, e.toString());
			}
		}
		return scenarioLoaders;
	}
	
	private void ParseScenarios(Element elem, Map<String, IXmlObjectLoader> scenarioLoaders)
	{
		NodeList nodesScenario = elem.getElementsByTagName("scenario");
		ArrayList<Scenario> listScenarios = new ArrayList<Scenario>();
		for (int i = 0; i < nodesScenario.getLength(); ++i)
		{
            Element elemScenario = (Element) nodesScenario.item(i);
            String sType = elemScenario.getAttribute("type");
            IXmlObjectLoader loader = scenarioLoaders.get(sType);
            if (loader != null)
            {
            	listScenarios.add((Scenario)loader.Load(elemScenario));
            }
            else
            {
            	Trace.WriteLine(TraceLevel.ERROR, "Cannot load scenario [%s], no loader for type [%s].",
            										elemScenario.getAttribute("name"),
            										elemScenario.getAttribute("type"));
            }
		}
		this.m_scenarios = listScenarios.toArray(new Scenario[listScenarios.size()]);
	}
	
	public String GetSetting(String setting)
	{
		return this.m_settings.get(setting);
	}
	
	public Scenario[] GetScenarios()
	{
		return this.m_scenarios;
	}
}
