package ceraslabs.hogna.workload;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

import ceraslabs.hogna.data.MetricCollection;
import ceraslabs.hogna.workload.Http.ClientHttp;
import ceraslabs.hogna.workload.Http.ScenarioHttp;
import Framework.Diagnostics.Trace;

/**
 * Generate quasi-identical repeated requests.
 * 
 * @author Cornel
 *
 */
public class WorkloadGenerator extends Thread
{
    private static WorkloadGenerator instance = null;
    private static int IdGenerator = 0;
    private final int ID;

    int m_clientCnt = 100;// the number of clients simulated
    int runs = 10;// the number of invocations for each client

    WorkGenStats m_statistics = new WorkGenStats();
    
    Scenario m_scenario = null;

    /**
     * Stores the array of clients
     */
    ClientHttp[] m_httpClients = null;

    /*
     * Will be set to true when no more workload should be generated and all clients should terminate.
     */
    boolean m_bStopRequested = false;

    public WorkloadGenerator(Scenario scenario, int clientCnt, int repeatCnt)
    {
    	super();
    	
    	this.m_scenario = scenario;
    	this.m_clientCnt = clientCnt;
    	this.runs = repeatCnt;

    	this.ID = ++WorkloadGenerator.IdGenerator;
		this.setName("WorkGen-" + this.ID);
    }

    public WorkloadGenerator()
    {
        super();

        this.ID = ++WorkloadGenerator.IdGenerator;
		this.setName("WorkGen-" + this.ID);
    }

    public static WorkloadGenerator getInstance() {

        if (instance == null)
            instance = new WorkloadGenerator();
        return instance;
    }

	/**
	 * Request that no more work should be generated. All running clients will receive a message to stop
	 * and execute the cleaning code. The stop will not happen immediately.
	 */
	public void RequestStop()
	{
		this.m_bStopRequested = true;

		// I don't think the request for stop should happen here, but in the "run" method.
		// However, the modifications in the "run" method seems to be more extensive (the Workload Generator thread
		// should spin while waiting for clients to finish, instead of just waiting).
		for (ClientHttp client : this.m_httpClients)
		{
			client.RequestStop();
		}
	}

	/**
	 * Request for all clients to stop. The function will return only when all client threads are finished.
	 */
	public void Stop()
	{
		this.m_bStopRequested = true;
		for (ClientHttp client : this.m_httpClients)
		{
			client.RequestStop();
		}
		for (ClientHttp client : this.m_httpClients)
		{
			try
			{
				client.join();
			}
			catch (InterruptedException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	public MetricCollection GetMetrics()
	{
		MetricCollection metrics = new MetricCollection();
		int reqErrorClientCount = 0;
		int reqErrorServerCount = 0;
		int reqSuccessCount = 0;
		double reqSuccessResp = 0;
		double reqSuccessDataIn = 0;
		
		for (ClientHttp client : this.m_httpClients)
		{
			ClientStats clientStats = client.GetStatistics();
			reqErrorClientCount += clientStats.reqErrorClientCount;
			reqErrorServerCount += clientStats.reqErrorServerCount;
			reqSuccessResp   = (reqSuccessCount + clientStats.reqSuccessCount > 0) ? (reqSuccessResp   * reqSuccessCount + clientStats.reqSuccessResp) / (reqSuccessCount + clientStats.reqSuccessCount) : 0;
			reqSuccessDataIn = (reqSuccessCount + clientStats.reqSuccessCount > 0) ? (reqSuccessDataIn * reqSuccessCount + clientStats.reqSuccessDataIn) / (reqSuccessCount + clientStats.reqSuccessCount) : 0;
			reqSuccessCount += clientStats.reqSuccessCount;
		}
		
    	metrics.Add(WorkMetrics.RESPONSE_TIME,  this.getName(), reqSuccessResp);
    	metrics.Add(WorkMetrics.DATA_IN,        this.getName(), reqSuccessDataIn);
    	metrics.Add(WorkMetrics.SUCCESS_CNT,    this.getName(), reqSuccessCount);
    	metrics.Add(WorkMetrics.SERVER_ERR_CNT, this.getName(), reqErrorServerCount);
    	metrics.Add(WorkMetrics.CLIENT_ERR_CNT, this.getName(), reqErrorClientCount);
    	metrics.Add(WorkMetrics.CLIENTS_CNT,    this.getName(), this.m_httpClients.length);
    	metrics.Add(WorkMetrics.THINK_TIME,     this.getName(), this.m_scenario.GetThinkTimeMean());

    	return metrics;
	}
	
	public void SetClientsCnt(int cnt)
	{
		if (m_httpClients.length > cnt)
		{
			// remove clients
			// m_httpClients.length - cnt
			ClientHttp[] httpClients = new ClientHttp[cnt];
			for (int i = 0; i < cnt; ++i)
			{
				httpClients[i] = this.m_httpClients[i];
			}
			for (int i = cnt; i < m_httpClients.length; ++i)
			{
				this.m_httpClients[i].RequestStop();
			}
			this.m_httpClients = httpClients;
			this.m_clientCnt = cnt;
		}
		else if (m_httpClients.length < cnt)
		{
			// add clients
			ClientHttp[] httpClients = new ClientHttp[cnt];
			for (int i = 0; i < m_httpClients.length; ++i)
			{
				httpClients[i] = this.m_httpClients[i];
			}
			for (int i = m_httpClients.length; i < cnt; ++i)
			{
				httpClients[i] = new ClientHttp((ScenarioHttp)this.m_scenario, this.runs);
				httpClients[i].start();
			}
			this.m_httpClients = httpClients;
			this.m_clientCnt = cnt;
		}
		else
		{
			// no change, do nothing
		}
	}
	
	public class SequenceNumberGenerator
	{
		private Double[] theNumbers;
		private int idx = 0;

		public SequenceNumberGenerator(String strFileName)
		{
			try (BufferedReader theDataReader = new BufferedReader(new FileReader(strFileName)))
			{
				ArrayList<Double> lstNumbers = new ArrayList<>();
				String line = null;
				while ((line = theDataReader.readLine()) != null)
				{
					line = line.trim();

					if (line == "")
						continue;
					
					String[] tokens = line.trim().split("[ \t]+");
					for (String token : tokens)
					{
						try
						{
							Double value = Double.parseDouble(token);
							lstNumbers.add(value);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				
				theNumbers = lstNumbers.toArray(new Double[lstNumbers.size()]);
			}
			catch (Exception e) { }
		}

		public double GetNext()
		{
			if (theNumbers != null && theNumbers.length > 0)
			{
				idx = ++idx % theNumbers.length;
				return theNumbers[idx];
			}
			return Double.NaN;
		}
	}
	
	public void run()
	{
		// create the array of clients
		m_httpClients = new ClientHttp[m_clientCnt];

		// create the threads; they are identical
		for (int i = 0; i < m_clientCnt; i++)
		{
			m_httpClients[i] = new ClientHttp((ScenarioHttp)this.m_scenario, this.runs);
		}

		// start the clients
		try
		{
			// start the threads
			for (int i = 0; i < m_clientCnt; ++i)
			{
				m_httpClients[i].start();
			}
			
			if (m_clientCnt == 2)
			{
				//int[] seqNumbers = {2, 5, 6, 8, 10, 14, 18, 30, 31, 20, 29, 18, 17, 15, 7, 8, 8, 9, 9, 10, 12, 15, 20, 22, 23, 24, 25, 25, 24, 25, 22, 20, 15, 14, 14, 13, 12, 10, 12, 15, 20};
				SequenceNumberGenerator theUsersGenerator = new SequenceNumberGenerator("d:/Work/Software Development/Cloud Economics/input/fifa98.workload");
				//int intervalLength = 300000; // 5 minutes
				//int intervalLength = 600000; // 10 minutes
				int intervalLength = 120000; // 2 minute
				//int intervalLength = 240000; // 4 minute

				int idx = 0;
				//Thread.sleep(intervalLength);
				while(true)
				{
					//int cntUsers = 3 * seqNumbers[idx++ % seqNumbers.length];
					//int cntUsers = (int)((theUsersGenerator.GetNext() - 300) / 9);
					int cntUsers = ++idx;

					int cntUsersDelta = cntUsers - this.m_clientCnt;
					if (cntUsersDelta > 0)
					{
						// add
						int intervalStep = intervalLength / cntUsersDelta;
						for (int i = 0; i < cntUsersDelta; ++i)
						{
							this.SetClientsCnt(this.m_clientCnt + 1);
							Thread.sleep(intervalStep);
						}
						Thread.sleep(intervalLength - intervalStep * cntUsersDelta);
					}
					else if (cntUsersDelta < 0)
					{
						// remove
						cntUsersDelta *= -1;
						int intervalStep = intervalLength / cntUsersDelta;
						for (int i = 0; i < cntUsersDelta; ++i)
						{
							this.SetClientsCnt(this.m_clientCnt - 1);
							Thread.sleep(intervalStep);
						}
						Thread.sleep(intervalLength - intervalStep * cntUsersDelta);
					}
				}
			}
			// wait for clients to finish
			for (int i = 0; i < m_clientCnt; i++)
			{
				m_httpClients[i].join();
			}
        }
		catch (Exception ex)
		{
			Trace.WriteException(ex);
        }
	}
	
	public static void main(String... args)
	{
		for (int j = 0; j < 1000; ++j)
		{
			long interval = 60;
			long granularity = 6;
			int usr = -j;

			double dWork = 0;
			int iWork = 0;
			int interestPoints = (int) (interval / granularity);
			int[] work = new int[interestPoints];
	
			double usrPerSec = 1.0 * usr / interestPoints;
			
			for (int i = 0; i < interestPoints; ++i)
			{
				dWork += usrPerSec;
				work[i] = (int) Math.round(dWork - iWork);
				iWork += work[i];
			}
			System.out.println(iWork == usr);
			System.out.println(Arrays.toString(work));
			if (iWork != usr)
				System.out.println("Wrong");
		}
	}

}
