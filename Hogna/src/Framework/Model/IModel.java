package Framework.Model;

import ceraslabs.hogna.monitoring.Data.MetricValuesExt;
import Framework.Cloud.IAction;
import Framework.Cloud.Topology.Topology;


// this is a VERY bad designed interface.
// E.G. the model is supposed work multi-threaded - what is the purpose of "IsAvailable"?
// The model can become available/unavailable at any moment (including microseconds
// after the method has been called, making useless the result of the call).
// The interface should have only "GetActionOnTopology" which should returns "null"
// if the model is not available/synchronized.
public interface IModel
{
	// return true if the model can be used.
	public boolean IsAvailable();

	// return true if the model provides similar results with the measured data
	public boolean IsSyncronized(MetricValuesExt measuredMetrics);

	// update the model (rebuild if necessary) with the measured data
	public void UpdateModel(Topology theTopology, MetricValuesExt measuredMetrics);

	// based on the data that the model has, create an action to be performed on topology
	public IAction GetActionOnTopology();

	// an action has been executed. The model is informed about it so it can update internal structure.
	// If the model is not available, it should remember all actions that have been performed so they can be
	// incorporated when the model becomes available.
	public void ActionExecuted(IAction action);
}
