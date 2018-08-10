package ceraslabs.hogna.configuration;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class MetricMappingsConfigurationSection extends ConfigurationSection
{
	private static final String TAG_ROOT = "metricMappings";

	private static final String TAG_METRICS_TO_MODEL = "metricsToModel";
	private static final String TAG_METRICS_TO_FILTER = "metricsToFilter";

	private static final String TAG_METRIC = "metric";
	private static final String ATTRIBUTE_MONITOR_PATH = "monitorPath";
	private static final String ATTRIBUTE_MODEL_PATH = "modelPath";
	private static final String ATTRIBUTE_FILTER_IDX = "filterIdx";
	
	
	HashMap<String, String> mapMetricsToModel = new HashMap<>();
	HashMap<String, Integer> mapMetricsToFilter = new HashMap<>();
	
	public HashMap<String, String> GetMetricsToModelMappings()
	{
		return this.mapMetricsToModel;
	}
	
	public HashMap<String, Integer> GetMetricsToFilterMappings()
	{
		return this.mapMetricsToFilter;
	}


	@Override
	protected void ParseSection(Element elemSection)
	{
		Trace.Assert (elemSection.getNodeName() == MetricMappingsConfigurationSection.TAG_ROOT,
				"EC2ConfigurationSection received [%s] section instead of [%s]. Did the name of the section changed?",
				elemSection.getNodeName(), MetricMappingsConfigurationSection.TAG_ROOT);
		Trace.WriteLine (TraceLevel.DEBUG, "Parsing configuration section [%s].", MetricMappingsConfigurationSection.TAG_ROOT);

		NodeList nodes = elemSection.getChildNodes();
		for (int i = 0; i < nodes.getLength(); ++i)
		{
			// remove text nodes, that contain only white spaces
			if (nodes.item(i) instanceof Text && ((Text)nodes.item(i)).getData().trim().length() == 0)
			{
				continue;
			}
			Element elemChild = (Element)nodes.item(i);

			if (elemChild.getNodeName().equals (TAG_METRICS_TO_MODEL))
			{
				this.ParseMetricsToModel(elemChild);
			}
			else if (elemChild.getNodeName().equals (TAG_METRICS_TO_FILTER))
			{
				this.ParseMetricsToFilter(elemChild);
			}
		}
	}
	
	private void ParseMetricsToModel(Element elem)
	{
		NodeList nodesMetrics = elem.getElementsByTagName(TAG_METRIC);
		for (int i = 0; i < nodesMetrics.getLength(); ++i)
		{
			Element elemHelper = (Element)nodesMetrics.item(i);
			String sMonitorPath = elemHelper.getAttribute(ATTRIBUTE_MONITOR_PATH);
			String sModelPath = elemHelper.getAttribute(ATTRIBUTE_MODEL_PATH);

			this.mapMetricsToModel.put(sMonitorPath, sModelPath);
		}
	}
	
	private void ParseMetricsToFilter(Element elem)
	{
		NodeList nodesMetrics = elem.getElementsByTagName(TAG_METRIC);
		for (int i = 0; i < nodesMetrics.getLength(); ++i)
		{
			Element elemHelper = (Element)nodesMetrics.item(i);
			String sMonitorPath = elemHelper.getAttribute(ATTRIBUTE_MONITOR_PATH);
			String sFilterIdx = elemHelper.getAttribute(ATTRIBUTE_FILTER_IDX);

			this.mapMetricsToFilter.put(sMonitorPath, Integer.parseInt(sFilterIdx));
		}
	}

}
