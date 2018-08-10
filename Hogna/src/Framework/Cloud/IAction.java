package Framework.Cloud;

import Framework.Cloud.Topology.Topology;

public interface IAction
{
	/*
	 * Executes the action using the specified cloud manager.
	 * 
	 */
	public boolean Execute(CloudManager theManager, Topology theTopology);

//	public void AddActionFinishedListener(IActionFinishedEventListener listener);
//	public void RemoveActionFinishedListener(IActionFinishedEventListener listener);
}
