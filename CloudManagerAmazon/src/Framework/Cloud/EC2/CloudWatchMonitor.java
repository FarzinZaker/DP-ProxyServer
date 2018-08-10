package Framework.Cloud.EC2;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import Framework.Diagnostics.Trace;
import ceraslabs.hogna.data.MetricValue;
import ceraslabs.hogna.monitoring.Monitor;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;

public class CloudWatchMonitor extends Monitor
{
	AWSCredentials awsCredentials = null;
	AmazonCloudWatchClient cloudWatchClient = null;
	private String m_strInstanceId;
	private String m_strCluster;
	private String m_strMetricName;
	private String m_strCredentialFile;

	public CloudWatchMonitor withInstanceId(String strInstanceId)
	{
		this.m_strInstanceId = strInstanceId;
		return this;
	}
	public CloudWatchMonitor withCluster(String strCluster)
	{
		this.m_strCluster = strCluster;
		return this;
	}
	public CloudWatchMonitor withMetricName(String strMetricName)
	{
		this.m_strMetricName = strMetricName;
		return this;
	}
	public CloudWatchMonitor withCredentialFile(String strCredentialFile)
	{
		this.m_strCredentialFile = strCredentialFile;
		try
		{
			this.awsCredentials = new PropertiesCredentials(new FileInputStream(this.m_strCredentialFile));
			this.cloudWatchClient = new AmazonCloudWatchClient(awsCredentials);
		}
		catch (IOException e)
		{
			Trace.WriteException(e);
		}
		return this;
	}

	@Override
	public double GetValue()
	{
		GetMetricStatisticsRequest getMetricRequest = new GetMetricStatisticsRequest();
		getMetricRequest.withNamespace("AWS/EC2")
						.withPeriod(120)
						.withMetricName(this.m_strMetricName)
						.withStatistics("Average", "Minimum", "Maximum")
						.withDimensions(new Dimension().withName("InstanceId").withValue(m_strInstanceId));

		GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		calendar.add(GregorianCalendar.SECOND, -1 * calendar.get(GregorianCalendar.SECOND));
		calendar.add(GregorianCalendar.MINUTE, -1);
		getMetricRequest.setEndTime(calendar.getTime());
		
		calendar.add(GregorianCalendar.MINUTE, -2);
		getMetricRequest.setStartTime(calendar.getTime());
		
		GetMetricStatisticsResult metricStatistics = cloudWatchClient.getMetricStatistics(getMetricRequest);
		if (null != metricStatistics.getDatapoints() && metricStatistics.getDatapoints().size() > 0)
		{
			return metricStatistics.getDatapoints().get(0).getAverage() / 100;
		}
		
		return Double.NaN;
	}
	
	@Override
	public MetricValue[] GetValues()
	{
		MetricValue value = new MetricValue(this.m_strCluster + "/" + this.m_strInstanceId, this.GetName(), this.GetValue());
		return new MetricValue[] { value };
	}
}
