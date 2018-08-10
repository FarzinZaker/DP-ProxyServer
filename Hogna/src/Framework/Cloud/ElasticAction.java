package Framework.Cloud;

import java.util.Date;

import Framework.Cloud.Topology.Topology;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class ElasticAction implements IAction, Cloneable
{
	//I was previously using this as target rather than m_clusterId
	public ElasticAction()
	{
		
	}
	
	public ElasticAction(String mcid, int mcinst)
	{
		this.m_clusterId = mcid;
		this.m_cntInstances = mcinst;
	}
	
	
	private String m_clusterId = "";
	public String getM_clusterId() {
		return m_clusterId;
	}

	public void setM_clusterId(String m_clusterId) {
		this.m_clusterId = m_clusterId;
	}

	public int getM_cntInstances() {
		return m_cntInstances;
	}

	public void setM_cntInstances(int m_cntInstances) {
		this.m_cntInstances = m_cntInstances;
	}


	private int m_cntInstances = 0;
	private Date now;
	
	public Date getNow() {
		return now;
	}

	public void setNow(Date now) {
		this.now = now;
	}

	//Added Bradley Simmons
	public ElasticAction clone()
	{
		ElasticAction ea = new ElasticAction();
		ea.SetInstancesToAdd(this.m_clusterId,this.m_cntInstances);
		return ea;
	}
	
	@Override
	public boolean Execute(CloudManager theManager, Topology theTopology)
	{
		Trace.Assert(this.m_clusterId.equals("") == false, "The cluster ID is not set.");
		if (this.m_cntInstances == 0)
		{
			Trace.WriteLine(TraceLevel.INFO, "0 instances to add/remove. Nothing to do.");
			return true;
		}

		if (this.m_cntInstances < 0)
		{
			int workersCnt = theTopology.GetCluster(this.m_clusterId).GetSize() -1;
			int instToRemove = (workersCnt <= this.m_cntInstances * -1) ? workersCnt - 1 : this.m_cntInstances * -1;
			
			if (instToRemove > 0)
			{
				long start = System.currentTimeMillis();
				theManager.RemoveWorkerNodes(theTopology, this.m_clusterId, instToRemove);
				long end = System.currentTimeMillis();
				Trace.WriteLine(TraceLevel.DEBUG, "Removing [%d] more instances to the cluster [%s] took [%d]ms.", instToRemove, this.m_clusterId, end - start);
			}
			else
			{
				Trace.WriteLine(TraceLevel.DEBUG, "Cannot remove [%d] instances. The cluster has [%d] workers.", instToRemove, workersCnt);
			}
		}
		else
		{
			long start = System.currentTimeMillis();
			theManager.AddWorkerNodes(theTopology, this.m_clusterId, this.m_cntInstances);
			long end = System.currentTimeMillis();
			Trace.WriteLine(TraceLevel.DEBUG, "Adding [%d] more instances to the cluster [%s] took [%d]ms.", this.m_cntInstances, this.m_clusterId, end - start);
		}

		this.OnActionFinished();
		return true;
	}

	/*
	 * Set the number of instances to add/remove from a cluster.
	 */
	public void SetInstancesToAdd(String clusterId, int cntInstances)
	{
		this.m_clusterId = clusterId;
		this.m_cntInstances = cntInstances;
	}
	
	public int GetInstancesToAdd()
	{
		return this.m_cntInstances;
	}
	
	public String GetClusterId()
	{
		return this.m_clusterId;
	}
	
	
	/*
	 * Events
	 */
	private static final IActionFinishedEventListener[] ACTION_FINISHED_NONE = new IActionFinishedEventListener[0];
	private IActionFinishedEventListener [] listenersActionFinished = ACTION_FINISHED_NONE;
    private Object lockActionFinished = new Object();

	public void AddActionFinishedListener(IActionFinishedEventListener listener)
	{
		if (listener == null)
		{
			return;
		}

		synchronized (lockActionFinished)
		{
			IActionFinishedEventListener[] tmp = new IActionFinishedEventListener[listenersActionFinished.length + 1];
			tmp[0] = listener;
			for (int i = 0; i < listenersActionFinished.length; ++i)
			{
				tmp[i + 1] = listenersActionFinished[i];
			}
			listenersActionFinished = tmp;
		}
	}
	
	public void RemoveActionFinishedListener(IActionFinishedEventListener listener)
	{
		if (listener == null)
		{
			return;
		}
		
		synchronized (lockActionFinished)
		{
			IActionFinishedEventListener[] tmp = (listenersActionFinished.length > 1) ? new IActionFinishedEventListener[listenersActionFinished.length - 1] : ACTION_FINISHED_NONE;
			for (int i = 0, j = 0; i < listenersActionFinished.length; ++i)
			{
				if (listenersActionFinished[i] != listener)
				{
					tmp[j++] = listenersActionFinished[i];
				}
			}
		}
	}
	
	protected final void OnActionFinished()
	{
		if (listenersActionFinished != ACTION_FINISHED_NONE)
		{
			ElasticActionFinishedEventArgs eventArgs = new ElasticActionFinishedEventArgs();
			IActionFinishedEventListener[] tmp = listenersActionFinished;
			for (int i = tmp.length - 1; i >= 0; --i)
			{
				tmp[i].ActionFinished(this, eventArgs);
			}
		}
	}
	
	public String toString()
	{
		return "ElasticAction: " + (this.m_cntInstances >= 0 ? "adding " + this.m_cntInstances : "removing " + (-1 * m_cntInstances)) + " instances."; 
	}
}
