package ceraslabs.hogna.workload;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ceraslabs.hogna.configuration.IXmlObjectLoader;
import ceraslabs.hogna.workload.Http.ScenarioHttp;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class HttpScenarioLoader implements IXmlObjectLoader
{
	@Override
	public Object Load(Element elemScenario)
	{
		// make sure that the scenario we got is HTTP
		if (false == elemScenario.getAttribute("type").equals("http"))
		{
			return null;
		}
		
		ScenarioHttp scenario = new ScenarioHttp();
		scenario.SetName (elemScenario.getAttribute("name"));
		scenario.SetUrl  (elemScenario.getElementsByTagName("url").item(0).getTextContent());
		scenario.SetThinkTimeMean(Integer.parseInt(elemScenario.getElementsByTagName("thinkTime").item(0).getTextContent()));

		Element elemParams = (Element)elemScenario.getElementsByTagName("parameters").item(0);
		NodeList nodesParam = elemParams.getElementsByTagName("parameter");
		
		for (int i = 0; i < nodesParam.getLength(); ++i)
		{
			Element elemParam = (Element) nodesParam.item(i);
			String paramName = elemParam.getAttribute("name");
			if (elemParam.getAttribute("type").equals("alphanumeric"))
			{
				// this is a string parameter
				scenario.AddParameter(new ParameterString(paramName));
			}
			else if (elemParam.getAttribute("type").equals("integer"))
			{
				int minVal = Integer.parseInt(elemParam.getAttribute("minValue"));
				int maxVal = Integer.parseInt(elemParam.getAttribute("maxValue"));
				scenario.AddParameter(new ParameterInt(paramName, minVal, maxVal));
			}
			else
			{
				Trace.WriteLine(TraceLevel.WARNING, "Scenario [%s]: Cannot create parameter [%s], unknown type [%s].",
													scenario.GetName(), paramName, elemParam.getAttribute("type"));
			}
		}
		
		return scenario;
	}
}
