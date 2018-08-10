package Framework.Model;

import java.util.Random;

import ceraslabs.hogna.monitoring.Data.MetricValuesExt;
import Framework.Cloud.ElasticAction;
import Framework.Cloud.IAction;
import Framework.Cloud.Topology.Topology;

public class SimpleModel implements IModel
{

	@Override
	public boolean IsAvailable()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IAction GetActionOnTopology()
	{
		ElasticAction theAction = new ElasticAction();
		Random rand = new Random();
		
		// TODO: the id of the web cluster is "624F1F47-87F4-4a98-A90D-25C024E31454": remove the hardcoded part
		
		double val = rand.nextDouble();
		if (val < 0.25)
		{
			theAction.SetInstancesToAdd("624F1F47-87F4-4a98-A90D-25C024E31454", -1);
		}
		else if (val < 0.75)
		{
			theAction.SetInstancesToAdd("624F1F47-87F4-4a98-A90D-25C024E31454", 0);
		}
		else
		{
			theAction.SetInstancesToAdd("624F1F47-87F4-4a98-A90D-25C024E31454", 1);
		}
		
		return theAction;
	}

	@Override
	public void ActionExecuted(IAction action)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean IsSyncronized(MetricValuesExt measuredMetrics)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void UpdateModel(Topology theTopology,
			MetricValuesExt measuredMetrics)
	{
		// TODO Auto-generated method stub
		
	}

}
