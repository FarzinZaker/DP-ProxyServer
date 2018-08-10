package opera.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MetricCollection implements Serializable
{
	/**
	 * Required by Serializable interface
	 */
	private static final long serialVersionUID = 4101664453072420419L;

	private long m_timestamp = System.currentTimeMillis();
	private long m_intervalLength = 0;
	
	private Map<String, Double>       m_mapMetricData        = new HashMap<String, Double>();
	private Map<String, List<String>> m_mapMetricToResources = new HashMap<String, List<String>>();
	private Map<String, List<String>> m_mapResourceToMetrics = new HashMap<String, List<String>>();

	public void Add(String strMetric, String strResource, double value)
	{
		if (Double.isNaN(value))
		{
			return;
		}
		String strResourceMetric = strResource + "/" + strMetric;
		this.m_mapMetricData.put(strResourceMetric, value);

		{
			List<String> lstResources = this.m_mapMetricToResources.get(strMetric);
			if (lstResources == null)
			{
				lstResources = new ArrayList<String>();
				this.m_mapMetricToResources.put(strMetric, lstResources);
			}
			lstResources.add(strResource);
		}

		{
			List<String> lstMetrics = this.m_mapResourceToMetrics.get(strResource);
			if (lstMetrics == null)
			{
				lstMetrics = new ArrayList<String>();
				this.m_mapResourceToMetrics.put(strResource, lstMetrics);
			}
			lstMetrics.add(strMetric);
		}
	}
	
	public void Add(MetricValue metric)
	{
		if (Double.isNaN(metric.value) == false)
		{
			this.Add(metric.sMetricName, metric.sResource, metric.value);
		}
	}
	
	public void Add(MetricValue[] metrics)
	{
		for (MetricValue metric : metrics)
		{
			if (Double.isNaN(metric.value) == false)
			{
				this.Add(metric.sMetricName, metric.sResource, metric.value);
			}
		}
	}
	
	public void SetIntervalLength(long length)
	{
		this.m_intervalLength = length;
	}

	/**
	 * Merge the metrics from another object into this one.
	 * @param otherMetrics
	 */
	public void Merge(MetricCollection other)
	{
		for (Entry<String, List<String>> entry : other.m_mapMetricToResources.entrySet())
		{
			String strMetric = entry.getKey();
			List<String> lstResources = entry.getValue();
			for (String strResource : lstResources)
			{
				this.Add(strMetric, strResource, other.Get(strMetric, strResource));
			}
		}
	}
	
//	public String[] GetAllMetricNames()
//	{
//		this.m_mapMetricData.keySet();
//	}

	/**
	 * 
	 * @param strMetricFullPath
	 * @return
	 */
	public double Get(String strMetricFullPath)
	{
		Double value = this.m_mapMetricData.get(strMetricFullPath);
		if (value != null)
			return value;
		else
			return Double.NaN;
	}
	
	public double Get(String strMetric, String strResource)
	{
		return this.Get(strResource + "/" + strMetric);
	}
	
	public double[] GetAllValues(String strMetric)
	{
		return null;
	}
	
	public double[] GetAllValues(String strMetric, String strResourcePattern)
	{
		return null;
	}

	public double GetAverage(String strMetric)
	{
		double avg = 0;
		List<String> lstResources = this.m_mapMetricToResources.get(strMetric);
		if (lstResources != null && lstResources.size() > 0)
		{
			for (String strResource : lstResources)
			{
				avg += this.m_mapMetricData.get(strResource + "/" + strMetric);
			}
			avg /= lstResources.size();
		}
		return avg;
	}
	
	public double GetAverage(String strMetric, String strResourcePattern)
	{
		double avg = 0;
		int size = 0;
		List<String> lstResources = this.m_mapMetricToResources.get(strMetric);
		if (lstResources != null && lstResources.size() > 0)
		{
			for (String strResource : lstResources)
			{
				if (strResource.matches(strResourcePattern))
				{
					avg += this.m_mapMetricData.get(strResource + "/" + strMetric);
					++size;
				}
			}
			if (size > 0)
			{
				avg /= size;
			}
		}
		return avg;
	}

	/*
   ┌──────────────────────────────┬───────────┐
   │ Tue Sep 15 14:37:32 EDT 2015 │   360,000 │
┌──┴──────────────────────────────┴───────────┴──────┐
│ WG-1/Client-1/Cnt Success                  12.0000 │
│ WG-2/Client-1/Data In                    1134.0000 │
│ WG-1/Client-2/Cnt Client Error              3.0000 │
│ WG-1/Client-2/Data In                    1092.0000 │
│ select 0/Response Time                    324.0000 │
│ WG-2/Client-2/Response Time                80.0000 │
│ WG-1/Client-2/Cnt Server Error              1.0000 │
│ WG-1/Client-1/Data In                    1234.0000 │
│ WG-2/Client-1/Cnt Client Error             11.0000 │
│ insert/Arrival Rate                         5.5400 │
│ WG-2/Client-2/Cnt Success                  26.0000 │
│ WG-2/Client-2/Cnt Client Error             13.0000 │
│ WG-2/Client-1/Response Time               110.0000 │
│ insert/Throughput                           5.0500 │
│ select 0/Arrival Rate                      15.8670 │
│ WG-1/Client-2/Response Time                90.0000 │
│ insert/Response Time                      123.0000 │
│ WG-2/Client-1/Cnt Server Error             10.0000 │
│ WG-2/Client-2/Cnt Server Error             11.0000 │
│ WG-1/Client-2/Cnt Success                  16.0000 │
│ WG-1/Client-1/Cnt Server Error              0.0000 │
│ select 0/Throughput                        16.0570 │
│ WG-1/Client-1/Response Time               100.0000 │
│ WG-2/Client-2/Data In                    1192.0000 │
│ WG-1/Client-1/Cnt Client Error              1.0000 │
│ WG-2/Client-1/Cnt Success                  11.0000 │
└────────────────────────────────────────────────────┘

   ┌──────────────────────────────┬───────────┐
   │ Tue Sep 15 14:37:32 EDT 2015 │   360,000 │
┌──┴──────────────────────────────┴───────────┴─────┐
│  ┌── WG-1                                         │
│  │   ├── Client-1                                 │
│  │   │   ├── Cnt Success                  12.0000 │
│  │   │   ├── Cnt Server Error              0.0000 │
│  │   │   ├── Response Time               100.0000 │
│  │   │   ├── Cnt Client Error              1.0000 │
│  │   │   └── Data In                    1234.0000 │
│  │   └── Client-2                                 │
│  │       ├── Cnt Client Error              3.0000 │
│  │       ├── Data In                    1092.0000 │
│  │       ├── Cnt Server Error              1.0000 │
│  │       ├── Response Time                90.0000 │
│  │       └── Cnt Success                  16.0000 │
│  ├── WG-2                                         │
│  │   ├── Client-1                                 │
│  │   │   ├── Data In                    1134.0000 │
│  │   │   ├── Cnt Client Error             11.0000 │
│  │   │   ├── Response Time               110.0000 │
│  │   │   ├── Cnt Server Error             10.0000 │
│  │   │   └── Cnt Success                  11.0000 │
│  │   └── Client-2                                 │
│  │       ├── Response Time                80.0000 │
│  │       ├── Cnt Success                  26.0000 │
│  │       ├── Cnt Server Error             11.0000 │
│  │       ├── Data In                    1192.0000 │
│  │       └── Cnt Client Error             13.0000 │
│  ├── insert                                       │
│  │   ├── Arrival Rate                      5.5400 │
│  │   ├── Throughput                        5.0500 │
│  │   └── Response Time                   123.0000 │
│  └── select 0                                     │
│      ├── Response Time                   324.0000 │
│      ├── Arrival Rate                     15.8670 │
│      └── Throughput                       16.0570 │
└───────────────────────────────────────────────────┘

   ┌──────────────────────────────┬───────────┐
   │ Tue Sep 15 14:37:32 EDT 2015 │   360,000 │
┌──┴──────────────────────────────┴───────────┴──┐
│  ┌── Response Time                             │
│  │   ├── insert                       123.0000 │
│  │   ├── select 0                     324.0000 │
│  │   ├── WG-2                                  │
│  │   │   ├── Client-2                  80.0000 │
│  │   │   └── Client-1                 110.0000 │
│  │   └── WG-1                                  │
│  │       ├── Client-2                  90.0000 │
│  │       └── Client-1                 100.0000 │
│  ├── Cnt Success                               │
│  │   ├── WG-1                                  │
│  │   │   ├── Client-1                  12.0000 │
│  │   │   └── Client-2                  16.0000 │
│  │   └── WG-2                                  │
│  │       ├── Client-2                  26.0000 │
│  │       └── Client-1                  11.0000 │
│  ├── Data In                                   │
│  │   ├── WG-1                                  │
│  │   │   ├── Client-2                1092.0000 │
│  │   │   └── Client-1                1234.0000 │
│  │   └── WG-2                                  │
│  │       ├── Client-1                1134.0000 │
│  │       └── Client-2                1192.0000 │
│  ├── Cnt Client Error                          │
│  │   ├── WG-1                                  │
│  │   │   ├── Client-2                   3.0000 │
│  │   │   └── Client-1                   1.0000 │
│  │   └── WG-2                                  │
│  │       ├── Client-1                  11.0000 │
│  │       └── Client-2                  13.0000 │
│  ├── Cnt Server Error                          │
│  │   ├── WG-1                                  │
│  │   │   ├── Client-2                   1.0000 │
│  │   │   └── Client-1                   0.0000 │
│  │   └── WG-2                                  │
│  │       ├── Client-1                  10.0000 │
│  │       └── Client-2                  11.0000 │
│  ├── Arrival Rate                              │
│  │   ├── insert                         5.5400 │
│  │   └── select 0                      15.8670 │
│  └── Throughput                                │
│      ├── insert                         5.0500 │
│      └── select 0                      16.0570 │
└────────────────────────────────────────────────┘
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		Date date = new Date(this.m_timestamp);
		sb.append("   ┌──────────────────────────────┬───────────┐\n");
		sb.append(String.format("   │ %s │ %9d │\n", date.toString(), this.m_intervalLength));
		//sb.append("┌──┴──────────────────────────────┴───────────┴────────┐\n");


		// create a copy using a TreeMap so we can have sorted items.
		Map<String, Double> copy = new TreeMap<String, Double>(this.m_mapMetricData);
		// find the longest metric name
		int size = 32; // minimum size;
		for (Entry<String, Double> entry : copy.entrySet())
		{
			String strResourceMetric = entry.getKey();
			size = size > strResourceMetric.length() ? size : strResourceMetric.length(); 
		}
		
		String strFormatter = "│ %-" + size + "s    %16.6f │\n";
		sb.append(String.format("┌──┴──────────────────────────────┴───────────┴%" + (size-24) + "s┐\n", "").replace(' ', '─'));
		for (Entry<String, Double> entry : copy.entrySet())
		{
			String strResourceMetric = entry.getKey();
			double value = entry.getValue();

			sb.append(String.format(strFormatter, strResourceMetric, value));
		}
		
		sb.append(String.format("└──────────────────────────────────────────────%" + (size-24) + "s┘\n", "").replace(' ', '─'));
		
		return sb.toString();
	}

	public static void main (String ... args)
	{
		MetricCollection theDataSample = new MetricCollection();
		theDataSample.Add("Response Time",    "WG-1/Client-1", 100);
		theDataSample.Add("Data In",          "WG-1/Client-1", 1234);
		theDataSample.Add("Cnt Success",      "WG-1/Client-1", 12);
		theDataSample.Add("Cnt Client Error", "WG-1/Client-1", 1);
		theDataSample.Add("Cnt Server Error", "WG-1/Client-1", 0);

		theDataSample.Add("Response Time",    "WG-1/Client-2", 90);
		theDataSample.Add("Data In",          "WG-1/Client-2", 1092);
		theDataSample.Add("Cnt Success",      "WG-1/Client-2", 16);
		theDataSample.Add("Cnt Client Error", "WG-1/Client-2", 3);
		theDataSample.Add("Cnt Server Error", "WG-1/Client-2", 1);

		theDataSample.Add("Response Time",    "WG-2/Client-1", 110);
		theDataSample.Add("Data In",          "WG-2/Client-1", 1134);
		theDataSample.Add("Cnt Success",      "WG-2/Client-1", 11);
		theDataSample.Add("Cnt Client Error", "WG-2/Client-1", 11);
		theDataSample.Add("Cnt Server Error", "WG-2/Client-1", 10);

		theDataSample.Add("Response Time",    "WG-2/Client-2", 80);
		theDataSample.Add("Data In",          "WG-2/Client-2", 1192);
		theDataSample.Add("Cnt Success",      "WG-2/Client-2", 26);
		theDataSample.Add("Cnt Client Error", "WG-2/Client-2", 13);
		theDataSample.Add("Cnt Server Error", "WG-2/Client-2", 11);

		
		theDataSample.Add("Response Time", "insert", 123);
		theDataSample.Add("Arrival Rate", "insert", 5.54);
		theDataSample.Add("Throughput", "insert", 5.05);

		theDataSample.Add("Response Time", "select 0", 324);
		theDataSample.Add("Arrival Rate", "select 0", 15.867);
		theDataSample.Add("Throughput", "select 0", 16.057);


		System.out.println("Cnt Server Error (avg): " + theDataSample.GetAverage("Cnt Server Error"));
		System.out.println("Cnt Server Error (wg-1, avg): " + theDataSample.GetAverage("Cnt Server Error", "WG-1/.*"));
		System.out.println(theDataSample.toString());
	}
}
