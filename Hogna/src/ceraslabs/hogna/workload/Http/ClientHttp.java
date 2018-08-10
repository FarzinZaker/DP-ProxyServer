package ceraslabs.hogna.workload.Http;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Random;

import ceraslabs.hogna.data.MetricCollection;
import ceraslabs.hogna.workload.ClientStats;
import ceraslabs.hogna.workload.WorkMetrics;


public class ClientHttp extends Thread
{
	static private int IdGenerator = 0;

	/**
	 * The ID of this client.
	 */
    int m_id = -1;

    private final String SYNC_STATS = "Thread synchronize object for statistics.";
    private ClientStats m_statistics = null;
    
    /**
     * The scenario to be executed.
     */
    ScenarioHttp m_scenario = null;
    /**
     * The number of executions of the scenario. Value 0 means that the scenario will
     * be executed forever or until a request for stop is received. 
     */
    int m_executionCnt = 0;

    Random thinkRand;

    /**
     *  Becomes true when there is a request to stop the execution of this thread.
     *  When a stop is requested, no more runs for this client.
     */
    boolean m_bStopRequested = false;

    public ClientHttp(ScenarioHttp scenario, int executionsCnt)
    {
    	super();
    	this.m_id = ++ClientHttp.IdGenerator;
    	this.m_scenario = scenario;
    	this.m_executionCnt = executionsCnt;
    	
    	//Trace.WriteLine(TraceLevel.INFO, "Http client created: ID=[%d], URL=[%s], Think Time Mean=[%d]",
    	//		this.m_id,
    	//		m_scenario.GetUrl(),
    	//		this.m_scenario.GetThinkTimeMean());
    }

    /**
     * The timestamp when the last statistics were extracted.
     */
    long m_timestampStats;
    /**
     * Get the statistics for this client. The statistics are valid since the last query.
     * The statistics object is not thread safe. Should not be modified.
     * @return
     */
    public final ClientStats GetStatistics()
    {
    	ClientStats oldStats = null;
    	ClientStats newStats = new ClientStats();
    	newStats.clientId = this.m_id;
    	long oldTimestamp = 0;
    	long newTimestamp = 0;
    	synchronized (SYNC_STATS)
		{
			oldStats = this.m_statistics;
			this.m_statistics = newStats;
			
			newTimestamp = System.currentTimeMillis();
			oldTimestamp = this.m_timestampStats;
			this.m_timestampStats = newTimestamp;
		}
    	oldStats.intervalLength = newTimestamp - oldTimestamp;
    	return oldStats;
    }
    
    public final MetricCollection GetMetrics()
    {
    	ClientStats oldStats = null;
    	ClientStats newStats = new ClientStats();
    	newStats.clientId = this.m_id;
    	long oldTimestamp = 0;
    	long newTimestamp = 0;
    	synchronized (SYNC_STATS)
		{
			oldStats = this.m_statistics;
			this.m_statistics = newStats;
			
			newTimestamp = System.currentTimeMillis();
			oldTimestamp = this.m_timestampStats;
			this.m_timestampStats = newTimestamp;
		}
    	oldStats.intervalLength = newTimestamp - oldTimestamp;

    	MetricCollection metrics = new MetricCollection();

    	metrics.SetIntervalLength(oldStats.intervalLength);
    	metrics.Add(WorkMetrics.RESPONSE_TIME,  String.valueOf(this.m_id), oldStats.reqSuccessResp);
    	metrics.Add(WorkMetrics.DATA_IN,        "",                        oldStats.reqSuccessDataIn);
    	metrics.Add(WorkMetrics.DATA_OUT,       "",                        oldStats.reqSuccessDataOut);
    	metrics.Add(WorkMetrics.SUCCESS_CNT,    "",                        oldStats.reqSuccessCount);
    	metrics.Add(WorkMetrics.SERVER_ERR_CNT, "",                        oldStats.reqErrorServerCount);
    	metrics.Add(WorkMetrics.CLIENT_ERR_CNT, "",                        oldStats.reqErrorClientCount);
    	
    	return metrics;
    }

	public double getThinkTime(double mean)
	{
		double thinkTime = 0.0;

		if (mean > 0.0)
		{
			//thinkTime = 0 - mean * Math.log(1 - thinkRand.nextDouble());
			thinkTime = 2.0 / 3.0 * mean * (1 + thinkRand.nextDouble());
		}
		else if (mean < 0.0)
		{
			System.out.println("mean is less than 0.0");
			return -1.0;
        }
		return thinkTime;
	}

	/*
	 * Request that this thread should stop. The thread will not stop immediately,
	 * but no more HTTP Requests will be made.
	 */
	public void RequestStop()
	{
		this.m_bStopRequested = true;
	}
	
	public void run()
	{
		Thread.currentThread().setName("WorkGenClient-" + (this.m_id));
		// initialize statistics
		this.m_statistics = new ClientStats();
		this.m_statistics.clientId = this.m_id;
		this.m_timestampStats = System.currentTimeMillis();

		URL url = null;
		HttpURLConnection httpConnection = null;
		//String cookie = null;
		//totaltime = 0.0;

		//int reqTotal = 0;
		//int reqError = 0;
		//int reqOk = 0;

		for (int i = 0; this.m_executionCnt == 0 ? true : i < this.m_executionCnt; ++i)
		{
			// check for a stop request
			if (this.m_bStopRequested == true)
			{
		        //Trace.WriteLine(TraceLevel.INFO, "Client [%d] acknowledges the stop request. Cleaning and then terminate.", this.m_id);
				break;
			}

			double reqSuccessResp      = 0;
			double reqSuccessDataIn    = 0;
			
			// DEBUG DATA
			//int debug_bytesRead = 0;
			//ArrayList<char[]> debug_buf = new ArrayList<>();
			//HttpURLConnection debug_httpConnection = null;

			try
			{
				// wait before making the next request
				Thread.sleep((long) this.m_scenario.GetThinkTime());

				// prepare the new request
				url = new URL(this.m_scenario.GetRequestURL());

				// record the time before doing the request.
				// this is used to record the total time necessary for this request
				long startTime = System.currentTimeMillis();
				
				httpConnection = (HttpURLConnection) url.openConnection();
				httpConnection.setRequestMethod("GET");
				httpConnection.setRequestProperty("Accept-Encoding", "gzip");
				httpConnection.addRequestProperty("Connection", "close");
				httpConnection.setReadTimeout(120000);
				
				//debug_httpConnection = httpConnection;

				/*
				try (BufferedReader rd = new BufferedReader(new InputStreamReader(httpConnection.getInputStream())))
				{
					int bytesRead = 0;
					char[] buf = new char[1024];
					while ((bytesRead = rd.read(buf)) > 0)
					{
						reqSuccessDataIn += bytesRead;
					}
				}*/
				try (InputStream inputStream = httpConnection.getInputStream())
				{
					int bytesRead = 0;
					byte[] buf = new byte[1024];
					while ((bytesRead = inputStream.read(buf)) > 0)
					{
						reqSuccessDataIn += bytesRead;
					}
				}

				reqSuccessResp = System.currentTimeMillis() - startTime;

				int responseCode = httpConnection.getResponseCode(); 
				if (200 <= responseCode && responseCode < 300)
				{
					synchronized (SYNC_STATS)
					{
						this.m_statistics.reqSuccessCount += 1;
						this.m_statistics.reqSuccessDataIn += reqSuccessDataIn;
						this.m_statistics.reqSuccessResp += reqSuccessResp;
					}
				}
				else if (500 <= responseCode && responseCode < 600)
				{
					// server error
					synchronized (SYNC_STATS)
					{
						this.m_statistics.reqErrorServerCount += 1;
					}
				}
				else
				{
					// client problem
					synchronized (SYNC_STATS)
					{
						this.m_statistics.reqErrorClientCount += 1;
					}
				}
			}
			catch (Exception ex)
			{
				try (BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream())))
				{
					// read the response body
					while (in.readLine() != null) { }
				}
				catch(Exception ex1)
				{
					//ex1.printStackTrace();
				}

				//ex.printStackTrace();
				synchronized (SYNC_STATS)
				{
					this.m_statistics.reqErrorClientCount += 1;
				}
			}

			httpConnection.disconnect();
        }
		//Trace.WriteLine(TraceLevel.INFO, "Client [%d] finished. Made [%d] requests (OK=[%d], Error=[%d]); response time=[%7.2f]", this.m_id, reqTotal, reqOk, reqError, meantime);
	}
}
