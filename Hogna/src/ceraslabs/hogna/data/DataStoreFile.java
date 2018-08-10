package ceraslabs.hogna.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import Framework.Diagnostics.Trace;

public class DataStoreFile implements IDataStore
{
	ArrayList<Column> m_columns = new ArrayList<>();
	String m_strFileName = null;
	private boolean m_bPrintHeader = true;
	private boolean m_bPrintAllMetrics = true;
	
	@Override
	public void Save(MetricCollection theMetrics)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("    %16d", System.currentTimeMillis()));
		for (Column column : this.m_columns)
		{
			sb.append("    ");//separator
			if (column.sMetricFunction.equals("average"))
				sb.append(String.format(column.sFormat, theMetrics.GetAverage(column.sMetricName, column.sResource)));
			else
				sb.append(String.format(column.sFormat, theMetrics.Get(column.sMetricFullPath)));
		}
		
		try (BufferedWriter out = new BufferedWriter(new FileWriter(this.m_strFileName, true)))
		{
    		out.write(sb.toString() + "\n");
		}
		catch (Exception e) { Trace.WriteException(e); }
	}
	
	public void AddColumn(String sMetricFullPath, String sFormat)
	{
		Column column = new Column();
		column.sMetricFullPath = sMetricFullPath;
		column.sFormat = sFormat;
		this.m_columns.add(column);
	}
	
	public void AddColumn(String sResourcePattern, String sMetricName, String sFunction, String sFormat)
	{
		Column column = new Column();
		column.sMetricName = sMetricName;
		column.sResource = sResourcePattern;
		column.sMetricFullPath = sResourcePattern + "/" + sMetricName;
		column.sMetricFunction = sFunction;
		column.sFormat = sFormat;
		this.m_columns.add(column);
	}
	
	public DataStoreFile withColumn(String sMetricFullPath, String sFormat)
	{
		this.AddColumn(sMetricFullPath, sFormat);
		return this;
	}
	
	public DataStoreFile withColumn(String sResourcePattern, String sMetricName, String sFunction, String sFormat)
	{
		this.AddColumn(sResourcePattern, sMetricName, sFunction, sFormat);
		return this;
	}
	
	public DataStoreFile withOutputFileName(String strFileName)
	{
		this.m_strFileName = strFileName;
		return this;
	}

	public DataStoreFile withPrintHeader(boolean bPrintHeader)
	{
		this.m_bPrintHeader = bPrintHeader;
		return this;
	}

	public DataStoreFile withPrintAllMetrics(boolean bPrintAllMetrics)
	{
		this.m_bPrintAllMetrics = bPrintAllMetrics;
		return this;
	}

	private class Column
	{
		String sFormat = "%16.4f";
		String sResource = "";
		String sMetricName = "";
		String sMetricFullPath = "";
		String sMetricFunction = null;
	}
}
