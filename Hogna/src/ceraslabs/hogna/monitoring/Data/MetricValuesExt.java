package ceraslabs.hogna.monitoring.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MetricValuesExt extends MetricValues
{
	HashMap<String, HashMap<String, Double>> m_metricValuesAverage = new HashMap<String, HashMap<String,Double>>();

	public void AddMetricValueAverage(String clusterId, String name, double value)
	{
		HashMap<String, Double> clusterMetricValues = this.m_metricValuesAverage.get(clusterId);
		if (clusterMetricValues == null)
		{
			clusterMetricValues = new HashMap<String, Double>();
			this.m_metricValuesAverage.put(clusterId, clusterMetricValues);
		}
		clusterMetricValues.put(name, value);
	}

	public double GetMetricValueAverage(String clusterId, String name)
	{
		HashMap<String, Double> clusterMetricValues = this.m_metricValuesAverage.get(clusterId);
		if (clusterMetricValues == null)
		{
			return Double.NaN;
		}
		
		Double value = clusterMetricValues.get(name);
		if (value == null)
		{
			return Double.NaN;
		}
		return value;
	}
	
	@Override
	public String toString()
	{
		// field sizes
		int sizeClusterId = 36;
		int sizeName = 15;
		int sizeNodeId = 15;
		String avgHeader = "[Average]";
		String formatterCluster = "| %" + sizeClusterId + "s |";
		String formatterMonitor = " %" + sizeName + "s |";
		String formatterNode    = " %" + sizeNodeId + "s |";
		String formatterValue   = " %" + sizeNodeId +".3f |";

		int prevLineLength = 0;
		StringBuilder sb = new StringBuilder();
		
		if (sizeNodeId < avgHeader.length())
		{
			avgHeader = avgHeader.substring(0, sizeNodeId - 3) + "...";
		}


		for (Map.Entry<String, HashMap<String, ArrayList<NodeMetricValue>>> entryMetricsCluster : this.m_metricValues.entrySet())
		{
			String clusterId = entryMetricsCluster.getKey();
			HashMap<String, ArrayList<NodeMetricValue>> metricsCluster = entryMetricsCluster.getValue();
			
			if (sizeClusterId < clusterId.length())
			{
				clusterId = clusterId.substring(0, sizeClusterId - 3) + "...";
			}

			String line1 = "";//separator
			String line2 = "";//headers

			line1 = String.format(formatterCluster, "").replaceAll("[ |]", "-");
			line2 = String.format(formatterCluster, clusterId);
			
			line1 += String.format(formatterMonitor, "").replaceAll("[ |]", "-");
			line2 += String.format(formatterMonitor, "");
			
			// the average column
			line2 += String.format(formatterNode, avgHeader);
			
			HashMap<String, Integer> mapNodeNames = new HashMap<String, Integer>();
			HashMap<Integer, String> mapMetricNames = new HashMap<Integer, String>();
			int idxNode = 0;
			int idxName = 0;
			// get a list with all nodes in this cluster
			for (Map.Entry<String, ArrayList<NodeMetricValue>> entryMetricsMonitor : metricsCluster.entrySet())
			{
				String name = entryMetricsMonitor.getKey();
				ArrayList<NodeMetricValue> metricsMonitor = entryMetricsMonitor.getValue();
				
				mapMetricNames.put(idxName, name);
				++idxName;

				for (NodeMetricValue metricNode : metricsMonitor)
				{
					String nodeId = metricNode.GetNodeId();
					if (mapNodeNames.get(nodeId) == null)
					{
						mapNodeNames.put(nodeId, idxNode);
						++idxNode;
						
						if (sizeNodeId < nodeId.length())
						{
							nodeId = nodeId.substring(0, sizeNodeId - 3) + "...";
						}

						line2 += String.format(formatterNode, nodeId);
					}
				}
			}
			
			int currentLineLength = (sizeNodeId + 3) * (mapNodeNames.size() + 1);
			line1 += String.format("%" + (prevLineLength > currentLineLength ? prevLineLength : currentLineLength) + "s\n", "").replaceAll("[ |]", "-");
			line2 += "\n";
			prevLineLength = currentLineLength;
			sb.append(line1);
			sb.append(line2);
			

			// build a table with metrics values, for this cluster
			double[][] dblMetricValues = new double[metricsCluster.size()][mapNodeNames.size()];
			int idxRow = 0;
			for (Map.Entry<String, ArrayList<NodeMetricValue>> entryMetricsMonitor : metricsCluster.entrySet())
			{
				ArrayList<NodeMetricValue> metricsMonitor = entryMetricsMonitor.getValue();

				for (NodeMetricValue metricNode : metricsMonitor)
				{
					String nodeId = metricNode.GetNodeId();
					int idxCol = mapNodeNames.get(nodeId);
					dblMetricValues[idxRow][idxCol] = metricNode.GetMetricValue();
				}
				++idxRow;
			}


			// create the string table
			for (int i = 0; i < mapMetricNames.size(); ++i)
			{
				String metricName = mapMetricNames.get(i);
				if (sizeName < metricName.length())
				{
					metricName = metricName.substring(0, sizeName - 3) + "...";
				}

				String line = String.format(formatterCluster, "");
				line += String.format(formatterMonitor, metricName);
				
				// do the average
				double avg = Double.NaN;
				if (this.m_metricValuesAverage.get(entryMetricsCluster.getKey()) != null)
				{
					if (this.m_metricValuesAverage.get(entryMetricsCluster.getKey()).get(metricName) != null)
					{
						avg = this.m_metricValuesAverage.get(entryMetricsCluster.getKey()).get(metricName);
					}
				}
				line += String.format(formatterValue, avg);

				for (int j = 0; j < mapNodeNames.size(); ++j)
				{
					line += String.format(formatterValue, dblMetricValues[i][j]);
				}
				line += "\n";
				sb.append(line);
			}
		}
		sb.append(String.format("%" + (sizeClusterId + 4 + sizeName + 3 + prevLineLength) + "s\n", "").replace(' ', '-'));

		return sb.toString();
	}

	/**
	 * Added by BPS so that I don't need to know this stuff in advance (though I likely will).
	 * Specifically, its easier to do it programatically, even if I know it in advance - must simply
	 * make sure the rules make sense
	 * @return
	 */
	public ArrayList<String>  getClusterNames()
	{
		ArrayList<String> clusternames = new ArrayList<String>();
		//HashMap<String, HashMap<String, Double>> m_metricValuesAverage = new HashMap<String, HashMap<String,Double>>();

		for(String name : m_metricValues.keySet())
			clusternames.add(name);
		 
		
		return clusternames;
	}
	
	
	public ArrayList<String> getMetricNames(String clustername)
	{
		ArrayList<String> metrics = new ArrayList<String>();
		for(String s : this.m_metricValues.get(clustername).keySet())
			metrics.add(s);
		
		return metrics;
	}
	
	
	public static void main(String[] args)
	{
		MetricValuesExt vals = new MetricValuesExt();
		vals.AddMetricValue("Database Server (MySql)", "Asgard", "CPU Utilization", 12.45);
		vals.AddMetricValue("Database Server (MySql)", "Asgard cel Mic", "CPU Utilization", 13.45);
		
		vals.AddMetricValue("Simple Web Cluster", "Freya", "CPU Utilization", 52.45);
		vals.AddMetricValue("Simple Web Cluster", "Freya", "Throughput", 5245);
		vals.AddMetricValue("Simple Web Cluster", "Odin", "Response Time", 5245);
		vals.AddMetricValue("Simple Web Cluster", "Odin", "CPU Utilization", 55.62);
		vals.AddMetricValue("Simple Web Cluster", "Odin", "Throughput", 221);
		vals.AddMetricValue("Simple Web Cluster", "Baldur", "CPU Utilization", 49.11);
		vals.AddMetricValue("Simple Web Cluster", "Baldur", "Throughput", 4911);

		vals.AddMetricValue("Web Cluster", "Baldurel", "CPU Utilization", 89.11);
		vals.AddMetricValue("Web Cluster", "Baldurel Jr.", "CPU Utilization", 100);
		
		vals.AddMetricValueAverage("Simple Web Cluster", "Response Time", 10);
		vals.AddMetricValueAverage("Simple Web Cluster", "CPU Utilization", 100);

		System.out.print(vals);
		System.out.println(vals.GetMetricValueAverage("Simple Web Cluster","Response Time"));

		System.out.println(vals.GetMetricValueAverage("Simple Web Cluster","CPU Utilization"));

		System.out.println(vals.GetMetricValueAverage("Simple Web Cluster","Throughput"));
		
		
		System.out.println("*******");
		System.out.println(vals.getClusterNames());
		for(String s : vals.getClusterNames())
		{
			System.out.println(vals.getMetricNames(s));
		}
	}
}
