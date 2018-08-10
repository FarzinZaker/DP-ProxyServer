package ceraslabs.hogna.configuration;

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import ceraslabs.hogna.data.DataStoreFile;
import ceraslabs.hogna.data.IDataStore;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class DataStoreFileConfigurationSection extends DataStoreConfigurationSection
{
	private static final String TAG_ROOT = "data-store-file";
	private static final String TAG_OUTPUT_FILE = "output-file";
	private static final String TAG_PRINT_HEADER = "print-header";
	private static final String TAG_PRINT_ALL_METRICS = "print-all-metrics";
	private static final String TAG_COLUMNS = "columns";
	
	private static final String TAG_COLUMN = "column";
	private static final String TAG_COLUMN_ATTRIBUTE_RESOURCE = "resource";
	private static final String TAG_COLUMN_ATTRIBUTE_METRIC_NAME = "metric-name";
	private static final String TAG_COLUMN_ATTRIBUTE_FUNCTION = "function";
	private static final String TAG_COLUMN_ATTRIBUTE_FORMAT = "format";

	private DataStoreFile m_dataStore = new DataStoreFile();
	
	@Override
	protected void ParseSection(Element elemSection)
	{
		// TODO Auto-generated method stub
		Trace.Assert (elemSection.getNodeName() == TAG_ROOT,
				"DataStoreFileConfigurationSection received [%s] section instead of [%s]. Did the name of the section changed?",
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

			if (elemChild.getNodeName().equals (TAG_OUTPUT_FILE))
			{
				String strFileName = elemChild.getTextContent().trim();
				this.m_dataStore.withOutputFileName(strFileName);
			}
			else if (elemChild.getNodeName().equals (TAG_PRINT_HEADER))
			{
				String strPrintHeader = elemChild.getTextContent().trim();
				this.m_dataStore.withPrintHeader(Boolean.parseBoolean(strPrintHeader));
			}
			else if (elemChild.getNodeName().equals (TAG_PRINT_ALL_METRICS))
			{
				String strPrintAllMetrics = elemChild.getTextContent().trim();
				this.m_dataStore.withPrintAllMetrics(Boolean.parseBoolean(strPrintAllMetrics));
			}
			else if (elemChild.getNodeName().equals (TAG_COLUMNS))
			{
				this.ParseColumns(elemChild);
			}
		}
	}

	private void ParseColumns(Element elemColumns)
	{
		NodeList lstNodes = elemColumns.getChildNodes();
		for (int i = 0; i < lstNodes.getLength(); ++i)
		{
			// ignore comments
			if (lstNodes.item(i) instanceof Comment)
			{
				continue;
			}
			// remove text nodes, that contain only white spaces
			if (lstNodes.item(i) instanceof Text && ((Text)lstNodes.item(i)).getData().trim().length() == 0)
			{
				continue;
			}
			Element elemChild = (Element)lstNodes.item(i);
			if (elemChild.getNodeName().equals (TAG_COLUMN))
			{
				String strResource = elemChild.getAttribute(TAG_COLUMN_ATTRIBUTE_RESOURCE);
				String strMetricName = elemChild.getAttribute(TAG_COLUMN_ATTRIBUTE_METRIC_NAME);
				String strFunction = elemChild.getAttribute(TAG_COLUMN_ATTRIBUTE_FUNCTION);
				String strFormat = elemChild.getAttribute(TAG_COLUMN_ATTRIBUTE_FORMAT);
				
				this.m_dataStore.withColumn(strResource, strMetricName, strFunction, strFormat);
			}
		}
	}

	@Override
	public IDataStore GetDataStore()
	{
		return this.m_dataStore;
	}
}
