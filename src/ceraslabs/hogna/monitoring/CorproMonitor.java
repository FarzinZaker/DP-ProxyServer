package ceraslabs.hogna.monitoring;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import ProxyServer.Monitoring.DataSample;
import ceraslabs.hogna.data.MetricValue;
import ceraslabs.hogna.monitoring.Metrics;

public class CorproMonitor extends Monitor
{
	String m_strHost;
	String m_strPort;
	String m_strCluster;

	int m_conTimeout;
	int m_conRetries;
	
	@Override
	public double GetValue()
	{
		return Double.NaN;
	}

	@Override
	public MetricValue[] GetValues()
	{
		DataSample[] samples = null;
		try(Socket socket = new Socket(this.m_strHost, Integer.parseInt(this.m_strPort)))
		{
			// get 180 samples, each representing data for 1 second.
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject("GET DATA WINDOW: 60");
		 
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			samples = (DataSample[]) ois.readObject();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
    

		
		ArrayList<MetricValue> theMetrics = new ArrayList<>();
		
		if (samples != null && samples.length > 0)
		{
			int cntScenarios = samples[0].scenarioNames.length;
			double[] throughput   = new double[cntScenarios];
			double[] arrivalRateU = new double[cntScenarios];
			double[] arrivalRateB = new double[cntScenarios];
			double[] responseTime = new double[cntScenarios];
			double timeTotal = 0;
			for (int j = 0; j < samples.length; ++j)//for (DataSample sample : samples)
			{
				DataSample sample = samples[j];

				timeTotal += sample.sampleIntervalLength;
				for (int i = 0; i < cntScenarios; ++i)
				{
					throughput[i]   += sample.requestsProcessedCnt[i];
					arrivalRateU[i] += sample.requestsCnt[i];
					arrivalRateB[i] += sample.requestsBlockedCnt[i];
					responseTime[i] += sample.responseTime[i] * sample.requestsProcessedCnt[i];
				}
			}
			double timeInSeconds = timeTotal / 1000.0;
			for (int i = 0; i < cntScenarios; ++i)
			{
				theMetrics.add(new MetricValue(this.m_strCluster + "/" + samples[0].scenarioNames[i],
						                       Metrics.THROUGHPUT,
						                       throughput[i] / timeInSeconds));
				
				theMetrics.add(new MetricValue(this.m_strCluster + "/" + samples[0].scenarioNames[i],
	                                           Metrics.ARRIVAL_RATE_UNFILTERED,
	                                           arrivalRateU[i] / timeInSeconds));

				theMetrics.add(new MetricValue(this.m_strCluster + "/" + samples[0].scenarioNames[i],
                                               Metrics.ARRIVAL_RATE_FILTERED,
                                               arrivalRateB[i] / timeInSeconds));

				theMetrics.add(new MetricValue(this.m_strCluster + "/" + samples[0].scenarioNames[i],
                                               Metrics.ARRIVAL_RATE,
                                               (arrivalRateU[i] + arrivalRateB[i]) / timeInSeconds));

				theMetrics.add(new MetricValue(this.m_strCluster + "/" + samples[0].scenarioNames[i],
                                               Metrics.RESPONSE_TIME,
                                               throughput[i] == 0 ? 0 : responseTime[i] / throughput[i]));
			}
		}

		return theMetrics.toArray(new MetricValue[theMetrics.size()]);
	}

	public CorproMonitor withConectionTimeout(int timeout)
	{
		this.m_conTimeout = timeout;
		return this;
	}
	public CorproMonitor withConnectionRetries(int cntRetries)
	{
		this.m_conRetries = cntRetries;
		return this;
	}
}
