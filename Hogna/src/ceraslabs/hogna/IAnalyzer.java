package ceraslabs.hogna;

import ceraslabs.hogna.data.MetricCollection;
import opera.OperaModel;
import Framework.AnalyzerResults;
import Framework.Cloud.Topology.Topology;

/**
 * Interface to be implemented by the analyzer component of the MAPE loop.
 * 
 * @author Cornel
 *
 */
public interface IAnalyzer
{
	AnalyzerResults Analyze(MetricCollection theMetrics, Topology theTopology, OperaModel theModel);
}
