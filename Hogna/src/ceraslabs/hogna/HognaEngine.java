package ceraslabs.hogna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ceraslabs.hogna.configuration.ConfigurationManager;
import ceraslabs.hogna.configuration.MonitorConfigurationSection;
import ceraslabs.hogna.data.IDataStore;
import ceraslabs.hogna.data.MetricCollection;
import ceraslabs.hogna.executor.commands.CloudBuildTopologyCommand;
import ceraslabs.hogna.executor.commands.CloudBuildTopologyCommandResult;
import ceraslabs.hogna.executor.commands.CloudScaleCommand;
import ceraslabs.hogna.executor.commands.CloudScaleCommandResult;
import ceraslabs.hogna.executor.commands.Command;
import ceraslabs.hogna.executor.commands.CommandResult;
import ceraslabs.hogna.executor.commands.CommandResult.CommandResultCodes;
import ceraslabs.hogna.executor.commands.ICommandCompleteCallback;
import ceraslabs.hogna.monitoring.IMonitorBuilder;
import ceraslabs.hogna.monitoring.Monitor;
import ceraslabs.hogna.monitoring.MonitorManager;
import Framework.AnalyzerResults;
import Framework.Cloud.Topology.Node;
import Framework.Cloud.Topology.Topology;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;
import opera.OperaModel;
import opera.KalmanFilter.KalmanEstimator;


public class HognaEngine
{
	// the interval between two consecutive MAPE iterations (in milliseconds).
	long m_timeItInterval = 1000;
	long m_timeWaitAfterAction = 60000;

	// Components to be used by the Manager
	IAnalyzer  theAnalyzer = null;
	IPlanner   thePlanner  = null;
	IExecutor  theExecutor = null;
	OperaModel theModel    = null;
	KalmanEstimator theEstimator = null;
	Topology theTopology = null;
	MonitorManager m_theMonitorManager = null;
	IDataStore theDataStore = null;
	
	Map<String, IMonitorBuilder> m_mapMonitorBuilders = null;
	
	// contains metrics that need to be inserted into the model from the monitoring component
	HashMap<String, String> mapMetricsToModel;
	
	HashMap<String, Integer> mapMetricsToFilter;
	
	HognaEngine() { }
	
	void Initialize()
	{
		/**
		 * Initialize the monitor builders.
		 */
		this.m_mapMonitorBuilders = new HashMap<>();
		MonitorConfigurationSection secMonitors = (MonitorConfigurationSection)ConfigurationManager.GetSection("monitoring");
		IMonitorBuilder[] monBuilders = secMonitors.GetMonitorBuilders();
		for (IMonitorBuilder monBuilder : monBuilders)
		{
			this.m_mapMonitorBuilders.put(monBuilder.GetName(), monBuilder);
		}
	}

	private void UpdateMonitorList()
	{
		ArrayList<Monitor> lstMonitors = new ArrayList<>();
		
		for(Node node : theTopology.GetAllNodes())
		{
			String[] sMonitors = node.GetMonitorNames();
			if (sMonitors == null)
			{
				continue;
			}

			for (String sMonitor : sMonitors)
			{
				IMonitorBuilder builder = this.m_mapMonitorBuilders.get(sMonitor);
				if (builder != null)
				{
					Monitor theMonitor = builder.forInstance(node);
					lstMonitors.add(theMonitor);
				}
			}
		}
		Monitor[] theMonitors = lstMonitors.toArray(new Monitor[lstMonitors.size()]);
		this.m_theMonitorManager.SetMonitors(theMonitors);
	}
	
	public void Run()
	{
		// check if there is a topology to build
		if (this.theTopology != null)
		{
			this.theExecutor.AddCommandCompleteCallback(new CmdComCal());

			CloudBuildTopologyCommand cmdBuildTopology = new CloudBuildTopologyCommand("cloud.build-topology");
			cmdBuildTopology.m_topology = this.theTopology;
			this.theExecutor.Execute(cmdBuildTopology);

			// give a little time for the action to take effect
			try
			{
				Thread.sleep(this.m_timeWaitAfterAction);
			}
			catch (Exception ex) {}
		}
		
		this.m_theMonitorManager.start();
		
		
		while (true)
		{
			// get metrics
			MetricCollection theMetrics = this.m_theMonitorManager.GetLastSample();
			
			if (theMetrics != null)
			{
				// run model and kalman
//				if (this.theModel != null)
//					this.UpdateModel(metricValues);

				AnalyzerResults results = this.theAnalyzer.Analyze(theMetrics, this.theTopology, this.theModel);
				Trace.WriteLine(TraceLevel.DEBUG, "%s", results.resultCode.toString());

				// plan a set of actions (using the model, this is the same object as the analyzer)
				ArrayList<Command> actionPlan = this.thePlanner.CreateActionPlan(theMetrics, results, this.theTopology, this.theModel);

				if (this.theDataStore != null)
				{
					this.theDataStore.Save(theMetrics);
				}

				// implement action plan
				{
					if (actionPlan.size() > 0)
					{
						this.theExecutor.Execute(actionPlan);

						// give a little time for the action to take effect
						try
						{
							Thread.sleep(this.m_timeWaitAfterAction);
						}
						catch (Exception ex) {}
					}
				}
			}
			
			try
			{
				Thread.sleep(this.m_timeItInterval);
			}
			catch (Exception ex) {}
		}
	}

/*
	void UpdateModel(MetricCollection metricValues)
	{
		// put the extracted workload into the model
		for (Entry<String, String> entry : this.mapMetricsToModel.entrySet())
		{
			String[] strMetric = entry.getKey().split("/");
			double value = 0;
			if (strMetric[2].equals("Average"))
			{
				value = metricValues.GetMetricValueAverage(strMetric[1], strMetric[3]);
			}
			else
			{
				value = metricValues.GetMetricValue(strMetric[1], strMetric[2], strMetric[3]);
			}

			this.theModel.SetXPathModelNodesValue(entry.getValue(), String.valueOf(value));
		}
		
		
		// put the extracted performance metrics into kalman
		double[] trackedMetricValues = new double[this.mapMetricsToFilter.size()];
		for (Entry<String, Integer> entry : this.mapMetricsToFilter.entrySet())
		{
			String[] strMetric = entry.getKey().split("/");
			double value = 0;
			if (strMetric[2].equals("Average"))
			{
				value = metricValues.GetMetricValueAverage(strMetric[1], strMetric[3]);
			}
			else
			{
				value = metricValues.GetMetricValue(strMetric[1], strMetric[2], strMetric[3]);
			}

			trackedMetricValues[entry.getValue()] = value;
		}
		
		// run kalman filter to fix the model
		try
		{
			EstimationResults results = this.theEstimator.EstimateModelParameters(trackedMetricValues);
			Trace.WriteLine(TraceLevel.DEBUG, "%s", results.toString());
		}
		catch (Exception e)
		{
			Trace.WriteLine(TraceLevel.ERROR, "Singular matrix -- cannot calibrate the model (most likely this means there is a problem with the metrics).");
		}
		
		// out: a model that is properly calibrated
	}
*/


	@SuppressWarnings("unused")
	private class CmdComCalBuild implements ICommandCompleteCallback
	{
		@Override
		public void CommandComplete(Command cmd, CommandResult cmdResult)
		{
			if (cmdResult == null)
			{
				Trace.WriteLine(TraceLevel.ERROR, "The actuator for building a topology didn't return any result. Cannot work on this conditions; I quit!");
				throw new RuntimeException("The actuator for build topology is not correctly implemented.");
			}
			else
			{
				CloudBuildTopologyCommandResult cmdBuildResult = (CloudBuildTopologyCommandResult)cmdResult;
				if (cmdBuildResult.GetResultCode() != CommandResultCodes.S_OK)
				{
					Trace.WriteLine(TraceLevel.ERROR, "Could not build the topology. Cannot continue.");
					throw new RuntimeException("Could not build the topology.");
				}
				else if (cmdBuildResult.m_topology == null)
				{
					Trace.WriteLine(TraceLevel.ERROR, "The actuator for building a topology didn't return the built topology. Cannot work on this conditions; I quit!");
					throw new RuntimeException("The actuator for build topology is not correctly implemented.");
				}
				else
				{
					HognaEngine.this.theTopology = cmdBuildResult.m_topology;
					HognaEngine.this.UpdateMonitorList();
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private class CmdComCalScale implements ICommandCompleteCallback
	{
		@Override
		public void CommandComplete(Command cmd, CommandResult cmdResult)
		{
			if (cmdResult == null)
			{
				Trace.WriteLine(TraceLevel.ERROR, "The actuator for scalling a topology didn't return any result. Cannot work on this conditions; I quit!");
				throw new RuntimeException("The actuator for scalling topology is not correctly implemented.");
			}
			else
			{
				CloudScaleCommandResult cmdScaleResult = (CloudScaleCommandResult)cmdResult;
				if (cmdScaleResult.GetResultCode() != CommandResultCodes.S_OK)
				{
					Trace.WriteLine(TraceLevel.ERROR, "Could not scale the topology. Cannot continue.");
					throw new RuntimeException("Could not scale the topology.");
				}
				else
				{
					// everything went fine, the topology object was updated by the Actuator
					HognaEngine.this.UpdateMonitorList();
				}
			}
		}
	}

	private class CmdComCal implements ICommandCompleteCallback
	{
		@Override
		public void CommandComplete(Command cmd, CommandResult cmdResult)
		{
			if (cmdResult == null)
			{
				Trace.WriteLine(TraceLevel.WARNING, "The actuator for command [%s] didn't return any result.", cmd.GetType());
			}
			else
			{
				if (cmd instanceof CloudBuildTopologyCommand)
				{
					this.HandleBuildTopology((CloudBuildTopologyCommand)      cmd,
							                 (CloudBuildTopologyCommandResult)cmdResult);
				}
				else if (cmd instanceof CloudScaleCommand)
				{
					this.HandleScaleCluster((CloudScaleCommand)      cmd,
							                (CloudScaleCommandResult)cmdResult);
				}
				else
				{
					// record the code
					Trace.WriteLine(TraceLevel.INFO, "%s [%d]: Command finished.", cmd.GetType(), cmdResult.GetResultCode());
				}
			}
		}
		
		void HandleBuildTopology(CloudBuildTopologyCommand cmd, CloudBuildTopologyCommandResult cmdResult)
		{
			if (cmdResult.GetResultCode() != CommandResultCodes.S_OK)
			{
				Trace.WriteLine(TraceLevel.ERROR, "Build-Topo [%d]: Could not build the topology. Cannot continue!",
						                          cmdResult.GetResultCode());
				throw new RuntimeException("Could not build the topology.");
			}
			else if (cmdResult.m_topology == null)
			{
				Trace.WriteLine(TraceLevel.ERROR, "Build-Topo [%d]: The actuator for building a topology didn't return the built topology. Cannot work on this conditions; I quit!",
						                          cmdResult.GetResultCode());
				throw new RuntimeException("The actuator for build topology is not correctly implemented.");
			}
			else
			{
				Trace.WriteLine(TraceLevel.INFO, "Build-Topo [%d]: Topology built!", cmdResult.GetResultCode());
				HognaEngine.this.theTopology = cmdResult.m_topology;
				HognaEngine.this.UpdateMonitorList();
			}
		}
		
		void HandleScaleCluster(CloudScaleCommand cmd, CloudScaleCommandResult cmdResult)
		{
			if (cmdResult.GetResultCode() != CommandResultCodes.S_OK)
			{
				Trace.WriteLine(TraceLevel.ERROR, "Scale-Cluster [%d]: Could not scale the topology.", cmdResult.GetResultCode());
			}
			else
			{
				// everything went fine, the topology object was updated by the Actuator
				if (cmd.m_cntInstancesDelta > 0)
				{
					Trace.WriteLine(TraceLevel.INFO, "Scale-Cluster [%d]: Added [%d] instances to cluster [%s].",
							                         cmdResult.GetResultCode(),
							                         cmd.m_cntInstancesDelta,
							                         cmd.m_strClusterId);
				}
				else
				{
					Trace.WriteLine(TraceLevel.INFO, "Scale-Cluster [%d]: Removed [%d] instances from cluster [%s].",
							                         cmdResult.GetResultCode(),
							                         cmd.m_cntInstancesDelta * -1,
							                         cmd.m_strClusterId);
				}
				HognaEngine.this.UpdateMonitorList();
			}
		}
	}
}
