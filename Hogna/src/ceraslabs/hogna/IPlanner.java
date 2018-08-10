package ceraslabs.hogna;

import java.util.ArrayList;

import ceraslabs.hogna.data.MetricCollection;
import ceraslabs.hogna.executor.commands.Command;
import opera.OperaModel;
import Framework.AnalyzerResults;
import Framework.Cloud.Topology.Topology;

/**
 * Interface to be implemented by the planner component of the MAPE loop.
 * 
 * @author Cornel
 *
 */
public interface IPlanner
{
	ArrayList<Command> CreateActionPlan(MetricCollection theMetrics, AnalyzerResults analyzerResults, Topology topology, OperaModel theModel);
}
