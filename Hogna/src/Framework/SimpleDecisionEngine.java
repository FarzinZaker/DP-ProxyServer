package Framework;

import java.util.ArrayList;

import ceraslabs.hogna.HognaConstants;
import ceraslabs.hogna.IAnalyzer;
import ceraslabs.hogna.IPlanner;
import ceraslabs.hogna.data.MetricCollection;
import ceraslabs.hogna.executor.commands.CloudScaleCommand;
import ceraslabs.hogna.executor.commands.Command;
import ceraslabs.hogna.monitoring.Data.MetricValues;
import opera.OperaModel;
import Framework.AnalyzerResults;
import Framework.Cloud.Topology.Cluster;
import Framework.Cloud.Topology.Node;
import Framework.Cloud.Topology.Topology;


/**
 * 
 * "SimpleDecisionEngine" is a threshold based decision engine that uses
 * simple rules to make decision. It's an analyzer/planner component.
 *
 * This is a general implementation of a threshold engine that makes
 * only simple analysis and takes decisions regarding the elasticity.
 * 
 * 
 * @author Cornel
 *
 */

public class SimpleDecisionEngine implements IAnalyzer, IPlanner
{
	ArrayList<ClusterResizeRule> lstExpandRules = new ArrayList<>();
	ArrayList<ClusterResizeRule> lstContractRules = new ArrayList<>();

//	ArrayList<String> listExceededDosThresholds = new ArrayList<>();
//	Map<String, Double> mapDosThresholds      = new HashMap<>();

	
	/*
	 * Methods specific to this class. 
	 */
	
	/**
	 * Add the threshold-based rule to expand the cluster.
	 * 
	 * @param theRule
	 *     the rule, that when triggered, instances will be added.
	 */
	public void AddExpandRule(ClusterResizeRule theRule)
	{
		this.lstExpandRules.add(theRule);
	}

	/**
	 * Add the threshold-based rule to shrink the cluster.
	 * 
	 * @param theRule
	 *     the rule, that when trigered, instances will be removed.
	 */
	public void AddContractRule(ClusterResizeRule theRule)
	{
		this.lstContractRules.add(theRule);
	}

	///**
	// * Example of adding a new DoS threshold:
	// *    SetDosThresholds(new String[]{"/WebCluster/[Balancer]/Arrival Rate select 0"}, new double[] {0.05})
	// * 
	// *    In this example if the metric "/WebCluster/[Balancer]/Arrival Rate select 0"
	// *    has a value more than "0.05" then is considered that the system is under
	// *    a DoS attack. When the metric value goes below the threshold, the attack
	// *    is considered finished.
	// *    
	// *    The semantic of "0.05" is not important here. It could represent
	// *    requests/millisecond or requests/second.
	// *     
	// * 
	// * @param strMetricPaths
	// * @param thresholds
	// */
	//public void SetDosThresholds(String[] strMetricPaths, double[] thresholds)
	//{
	//	for (int i = 0; i < strMetricPaths.length; ++i)
	//	{
	//		this.mapDosThresholds.put(strMetricPaths[i], thresholds[i]);
	//	}
	//}
	
	protected void SaveMetricsToFile(MetricValues theMetrics, Topology theTopology)
	{
		/*
		// get the filename
		String strMetricsFile = ConfigurationManager.GetSetting("SimpleApp: Metrics File");
		if (strMetricsFile != null)
		{
			String strOutput = "";
	
			// the ID
			strOutput = String.format("%s    %16d", strOutput, System.currentTimeMillis());
	
			// servers count
			strOutput = String.format("%s    %16d", strOutput, theTopology.GetCluster("DatabaseCluster").GetSize());
			strOutput = String.format("%s    %16d", strOutput, theTopology.GetCluster("WebCluster").GetSize());
	
			// CPU utilization
			strOutput = String.format("%s    %16.2f", strOutput, theMetrics.GetMetricValueAverage("DatabaseCluster", "CPU Utilization")); // average
			strOutput = String.format("%s    %16.2f", strOutput, theMetrics.GetMetricValueAverage("WebCluster", "CPU Utilization")); // average
	
			

			try
			{
	    		BufferedWriter out;
				out = new BufferedWriter(new FileWriter(strMetricsFile, true));
	    		out.write(strOutput + "\n");
	    		out.close();
			}
			catch (Exception e) { Trace.WriteException(e); }

		}
		*/
	}
	
	/**
	 * IAnalyzer.Analyze
	 */
	@Override
	public AnalyzerResults Analyze(MetricCollection theMetrics, Topology theTopology, OperaModel theModel)
	{
		AnalyzerResults results = new AnalyzerResults();
		// first check for DoS
		/*
		for (Entry<String, Double> entry : this.mapDosThresholds.entrySet())
		{
			String[] strMetric = entry.getKey().split("/");
			double valMetric = 0;
			if (strMetric[2].equals("Average"))
			{
				valMetric = theMetrics.GetMetricValueAverage(strMetric[1], strMetric[3]);
			}
			else
			{
				valMetric = theMetrics.GetMetricValue(strMetric[1], strMetric[2], strMetric[3]);
			}
			double valThreshold = entry.getValue();
			if (valMetric > valThreshold &&
				false == this.listExceededDosThresholds.contains(entry.getKey()))
			{
				// DOSA_DETECTED
				this.listExceededDosThresholds.add(entry.getKey());
				
				
				results.resultCode = HognaConstants.DOSA_DETECTED;
				return results;// DOSA_DETECTED
			}
		}
		
		// check if an existing DoS attack finished
		for (String entry : this.listExceededDosThresholds)
		{
			double valThreshold = mapDosThresholds.get(entry);
			String[] strMetric = entry.split("/");
			double valMetric = 0;
			if (strMetric[2].equals("Average"))
			{
				valMetric = theMetrics.GetMetricValueAverage(strMetric[1], strMetric[3]);
			}
			else
			{
				valMetric = theMetrics.GetMetricValue(strMetric[1], strMetric[2], strMetric[3]);
			}
			if (valMetric < valThreshold)
			{
				// DOSA_FINISHED
				this.listExceededDosThresholds.remove(entry);
				
				results.resultCode = HognaConstants.DOSA_FINISHED;
				return results; // DOSA_FINISHED
			}
		}
		*/

/*
		// check for expand threshold
		for (ClusterResizeRule rule : this.lstExpandRules)
		{
			String[] strMetric = rule.strMetric.split("/");
			double valMetric = 0;
			if (strMetric[2].equals("Average"))
			{
				valMetric = theMetrics.GetMetricValueAverage(strMetric[1], strMetric[3]);
			}
			else
			{
				valMetric = theMetrics.GetMetricValue(strMetric[1], strMetric[2], strMetric[3]);
			}
			double valThreshold = rule.treshold;
			if (valMetric > valThreshold)
			{
				// SYSTEM_OVERLOAD
				
				results.resultCode = HognaConstants.SYSTEM_OVERLOAD;
				results.data = rule;
				return results; // SYSTEM_OVERLOAD
			}
		}

		// check for contract threshold
		for (ClusterResizeRule rule : this.lstContractRules)
		{
			String[] strMetric = rule.strMetric.split("/");
			double valMetric = 0;
			if (strMetric[2].equals("Average"))
			{
				valMetric = theMetrics.GetMetricValueAverage(strMetric[1], strMetric[3]);
			}
			else
			{
				valMetric = theMetrics.GetMetricValue(strMetric[1], strMetric[2], strMetric[3]);
			}
			double valThreshold = rule.treshold;
			if (valMetric < valThreshold)
			{
				// SYSTEM_UNDERLOAD
				
				results.resultCode = HognaConstants.SYSTEM_UNDERLOAD;
				results.data = rule;
				return results; // SYSTEM_UNDERLOAD
			}
		}
*/
		results.resultCode = HognaConstants.SYSTEM_HEALTHY;
		return results; // SYSTEM_HEALTHY
	}

	/**
	 * IPlanner.CreateActionPlan
	 */
	@Override
	public ArrayList<Command> CreateActionPlan(MetricCollection theMetrics, AnalyzerResults analyzerResults, Topology theTopology, OperaModel theModel)
	{
		ArrayList<Command> actionPlan = new ArrayList<>();

/*
		this.SaveMetricsToFile(theMetrics, theTopology);
		

		switch(analyzerResults.resultCode)
		{
			case HognaConstants.SYSTEM_HEALTHY:
				break;
			case HognaConstants.SYSTEM_OVERLOAD:
			{
				ClusterResizeRule rule = (ClusterResizeRule)analyzerResults.data;

				CloudScaleCommand cmd = new CloudScaleCommand("cloud.scale-cluster");
				cmd.m_topology          = theTopology;
				cmd.m_strClusterId      = rule.strCluster;
				cmd.m_cntInstancesDelta = rule.svrToAdd;
				actionPlan.add(cmd);
				break;
			}
			case HognaConstants.SYSTEM_UNDERLOAD:
			{
				ClusterResizeRule rule = (ClusterResizeRule)analyzerResults.data;

				// search the topology for a worker node in the specified cluster
				// remove workers only if there are more than one.
				int cntWorkers = 0;
				Cluster cluster = theTopology.GetCluster(rule.strCluster);
				for (Node node : cluster.GetNodes())
				{
					if (node.GetType().equals("worker"))
					{
						++cntWorkers;
					}
				}
				// will remove an worker only if there are more than 1, else leave the system under-loaded
				if (cntWorkers > 1)
				{
					CloudScaleCommand cmd = new CloudScaleCommand("cloud.scale-cluster");
					cmd.m_topology          = theTopology;
					cmd.m_strClusterId      = rule.strCluster;
					cmd.m_cntInstancesDelta = (cntWorkers + rule.svrToAdd > 1 ? rule.svrToAdd : cntWorkers - 1);
					actionPlan.add(cmd);
				}
				break;
			}
		}
*/
		return actionPlan;
	}
}
