package ceraslabs.hogna.monitoring.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MetricValues
{
	HashMap<String, HashMap<String, ArrayList<NodeMetricValue>>> m_metricValues = new HashMap<String, HashMap<String,ArrayList<NodeMetricValue>>>();
	
	private int m_id = 0;
	
	public void SetId(int id) { this.m_id = id; }
	public int GetId() { return this.m_id; }
	
	public NodeMetricValue GetNodeMetricValue(String clusterId, String nodeId, String name)
	{
		HashMap<String, ArrayList<NodeMetricValue>> metricsCluster = this.m_metricValues.get(clusterId);

		if (metricsCluster != null)
		{
			ArrayList<NodeMetricValue> metricsMonitor = metricsCluster.get(name);
			if (metricsMonitor != null)
			{
				for (NodeMetricValue nodeMetricValue : metricsMonitor)
				{
					if (nodeId.equals(nodeMetricValue.GetNodeId()))
					{
						return nodeMetricValue;
					}
				}
			}
		}

		return null;
	}
	
	public double GetMetricValue (String clusterId, String nodeId, String name)
	{
		NodeMetricValue nodeMetricValue = this.GetNodeMetricValue(clusterId, nodeId, name);
		if (nodeMetricValue != null)
		{
			return nodeMetricValue.GetMetricValue();
		}
		
		return Double.NaN;
	}
	
	public ArrayList<NodeMetricValue> GetMetricValues (String clusterId, String name)
	{
		HashMap<String, ArrayList<NodeMetricValue>> metricsCluster = this.m_metricValues.get(clusterId);
		
		if (metricsCluster != null)
		{
			return metricsCluster.get(name);
		}

		return null;
	}
	
	public Set<String> GetStrClusters()
	{
		return this.m_metricValues.keySet();
	}
	
	public Set<String> GetStrNodes(String sCluster)
	{
		Set<String> sNodes = new HashSet<String>();
		
		// get the relevant cluster
		HashMap<String, ArrayList<NodeMetricValue>> metricsCluster = this.m_metricValues.get(sCluster);

		if (metricsCluster != null)
		{
			// extract all the metrics that exist in this cluster
			Set<String> sMetrics = metricsCluster.keySet();

			for (String sMetric : sMetrics)
			{
				ArrayList<NodeMetricValue> metricsMonitor = metricsCluster.get(sMetric);
				if (metricsMonitor != null)
				{
					for (NodeMetricValue nodeMetricValue : metricsMonitor)
					{
						sNodes.add(nodeMetricValue.GetNodeId());
					}
				}
			}
		}
		
		return sNodes;
	}
	
	public Set<String> GetStrMetrics(String sCluster)
	{
		return this.m_metricValues.get(sCluster).keySet();
	}
	
//	public Set<String> GetStrMetrics(String sCluster, String sNode)
//	{
//		return this.m_metricValues.get(sCluster).get(sNode);
//	}
	
	public double GetMetricValueAverage (String clusterId, String name)
	{
		HashMap<String, ArrayList<NodeMetricValue>> metricsCluster = this.m_metricValues.get(clusterId);
		
		if (metricsCluster != null)
		{
			ArrayList<NodeMetricValue> metricsMonitor = metricsCluster.get(name);
			if (metricsMonitor != null)
			{
				double avg = 0;
				int cntVals = 0;

				for (NodeMetricValue nodeMetricValue : metricsMonitor)
				{
					if (Double.isNaN(nodeMetricValue.GetMetricValue()) == false)
					{
						avg += nodeMetricValue.GetMetricValue();
						++cntVals;
					}
				}
				
				return (cntVals > 0) ? (avg / cntVals) : 0;
			}
		}

		return Double.NaN;
	}
	
	public void AddMetricValue(String clusterId, String nodeId, String name, double value)
	{
		NodeMetricValue nodeMetricValue = new NodeMetricValue();

		nodeMetricValue.m_timestamp = System.currentTimeMillis();
		nodeMetricValue.m_metricName = name;
		nodeMetricValue.m_nodeId = nodeId;
		nodeMetricValue.m_value = value;
		
		HashMap<String, ArrayList<NodeMetricValue>> clusterMetricValues = this.m_metricValues.get(clusterId);
		if (clusterMetricValues == null)
		{
			// new cluster
			clusterMetricValues = new HashMap<String, ArrayList<NodeMetricValue>>();
			this.m_metricValues.put(clusterId, clusterMetricValues);
		}
		
		ArrayList<NodeMetricValue> monitorMetricValues = clusterMetricValues.get(name);
		if (monitorMetricValues == null)
		{
			// new Metric Name
			monitorMetricValues = new ArrayList<NodeMetricValue>();
			clusterMetricValues.put(name, monitorMetricValues);
		}
		monitorMetricValues.add(nodeMetricValue);
	}

	/**
	 * @Override
	 */
	public String toString()
	{
		// field sizes
		int sizeClusterId = 36;
		int sizeName = 25;
		int sizeNodeId = 15;
		String formatterCluster = "| %" + sizeClusterId + "s |";
		String formatterMonitor = " %" + sizeName + "s |";
		String formatterNode    = " %" + sizeNodeId + "s |";
		String formatterValue   = " %" + sizeNodeId +".3f |";

		int prevLineLength = 0;
		StringBuilder sb = new StringBuilder();

		for (Entry<String, HashMap<String, ArrayList<NodeMetricValue>>> entryMetricsCluster : this.m_metricValues.entrySet())
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
			
			HashMap<String, Integer> mapNodeNames = new HashMap<String, Integer>();
			HashMap<Integer, String> mapMetricNames = new HashMap<Integer, String>();
			int idxNode = 0;
			int idxName = 0;
			// get a list with all nodes in this cluster
			for (Entry<String, ArrayList<NodeMetricValue>> entryMetricsMonitor : metricsCluster.entrySet())
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
			
			int currentLineLength = (sizeNodeId + 3) * mapNodeNames.size();
			line1 += String.format("%" + (prevLineLength > currentLineLength ? prevLineLength : currentLineLength) + "s\n", "").replaceAll("[ |]", "-");
			line2 += "\n";
			prevLineLength = currentLineLength;
			sb.append(line1);
			sb.append(line2);
			

			// build a table with metrics values, for this cluster
			double[][] dblMetricValues = new double[metricsCluster.size()][mapNodeNames.size()];
			int idxRow = 0;
			for (Entry<String, ArrayList<NodeMetricValue>> entryMetricsMonitor : metricsCluster.entrySet())
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
	
	public String toStringExt()
	{
		// field sizes
		int sizeClusterId = 36;
		int sizeName = 15;
		int sizeNodeId = 15;
		String formatterCluster = "| %" + sizeClusterId + "s |";
		String formatterMonitor = " %" + sizeName + "s |";
		String formatterNode    = " %" + sizeNodeId + "s |";
		String formatterValue   = " %" + sizeNodeId +".3f |";
		
		int prevLineLength = 0;

		StringBuilder sb = new StringBuilder();
		
		for (Entry<String, HashMap<String, ArrayList<NodeMetricValue>>> entryMetricsCluster : this.m_metricValues.entrySet())
		{
			String clusterId = entryMetricsCluster.getKey();
			HashMap<String, ArrayList<NodeMetricValue>> metricsCluster = entryMetricsCluster.getValue();
			
			if (sizeClusterId < clusterId.length())
			{
				clusterId = clusterId.substring(0, sizeClusterId - 3) + "...";
			}

			String line1 = "";//separator
			String line2 = "";//headers
			String line3 = "";//values

			line1 = String.format(formatterCluster, "").replaceAll(" |\\|", "-");
			line2 = String.format(formatterCluster, clusterId);
			line3 = String.format(formatterCluster, "");
			
			
			for (Entry<String, ArrayList<NodeMetricValue>> entryMetricsMonitor : metricsCluster.entrySet())
			{
				String name = entryMetricsMonitor.getKey();
				ArrayList<NodeMetricValue> metricsMonitor = entryMetricsMonitor.getValue();
				
				if (sizeName < name.length())
				{
					name = name.substring(0, sizeName - 3) + "...";
				}
				
				String line11 = line1 + String.format(formatterMonitor, "").replaceAll(" |\\|", "-");
				String line21 = line2 + String.format(formatterMonitor, "");
				String line31 = line3 + String.format(formatterMonitor, name);

				line1 = line2 = line3;

				for (NodeMetricValue metricNode : metricsMonitor)
				{
					String nodeId = metricNode.GetNodeId();
					if (sizeNodeId < nodeId.length())
					{
						nodeId = nodeId.substring(0, sizeNodeId - 3) + "...";
					}
					line21 += String.format(formatterNode, nodeId);
					line31 += String.format(formatterValue, metricNode.GetMetricValue());
				}
				int currentLineLength = (sizeNodeId + 3) * metricsMonitor.size();
				line11 += String.format("%" + (prevLineLength > currentLineLength ? prevLineLength : currentLineLength) + "s\n", "").replaceAll("[ |]", "-");
				line21 += "\n";
				line31 += "\n";
				prevLineLength = currentLineLength;
				
				sb.append(line11);
				sb.append(line21);
				sb.append(line31);
			}
		}
		
		sb.append(String.format("%" + (sizeClusterId + 4 + sizeName + 3 + prevLineLength) + "s\n", "").replace(' ', '-'));
		
		return sb.toString();
	}
	
	public static void main(String[] args)
	{
		MetricValues vals = new MetricValues();
		vals.AddMetricValue("Database Server (MySql)", "Asgard", "CPU Utilization", 12.45);
		vals.AddMetricValue("Database Server (MySql)", "Asgard cel Mic", "CPU Utilization", 13.45);
		
		vals.AddMetricValue("Simple Web Cluster", "Freya", "CPU Utilization", 52.45);
		vals.AddMetricValue("Simple Web Cluster", "Freya", "Throughput", 5245);
		vals.AddMetricValue("Simple Web Cluster", "Odin", "Response Time", 5245);
		vals.AddMetricValue("Simple Web Cluster", "Odin", "CPU Utilization", 55.62);
		vals.AddMetricValue("Simple Web Cluster", "Odin", "Throughput", 221);
		vals.AddMetricValue("Simple Web Cluster", "Baldur", "CPU Utilization", 49.11);
		vals.AddMetricValue("Simple Web Cluster", "Baldur", "Throughput", 4911);

		vals.AddMetricValue("Web Cluster", "Baldurel", "Ceva", 89.11);
		vals.AddMetricValue("Web Cluster", "Baldurel Jr.", "Ceva", 100);

		System.out.print(vals);
	}
}
