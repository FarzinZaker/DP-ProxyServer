package Framework;

public class ClusterResizeRule
{
	public String strMetric;
	public double treshold;
	public String strCluster;
	public int svrToAdd;

	public ClusterResizeRule(String strMetric, double treshold, String strCluster, int svrToAdd)
	{
		this.strMetric = strMetric;
		this.treshold = treshold;
		this.svrToAdd = svrToAdd;
		this.strCluster = strCluster;
	}
}
