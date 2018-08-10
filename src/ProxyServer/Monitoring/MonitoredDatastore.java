package ProxyServer.Monitoring;


public class MonitoredDatastore
{
	private static MonitoredDatastore m_theStore = null;
	
	// store samples to cover one hour, each second one sample
	private final int DATASTORE_CAPACITY = 3600;

	private DataSample[] m_samples = null;
	private Integer m_currentIdx = 0;
	private int m_datastoreSize = 0;
	
	private MonitoredDatastore()
	{
		this.m_samples = new DataSample[DATASTORE_CAPACITY];
		this.m_currentIdx = 0;
		this.m_datastoreSize = 0;
	}

	public static MonitoredDatastore GetDatastore()
	{
		if (MonitoredDatastore.m_theStore == null)
		{
			MonitoredDatastore.m_theStore = new MonitoredDatastore();
		}
		return MonitoredDatastore.m_theStore;
	}
	
	public synchronized void AddSample(DataSample theSample)
	{
		//this.m_samples.put(m_currentIdx, theSample);
		this.m_currentIdx = (this.m_currentIdx + 1) % DATASTORE_CAPACITY;
		this.m_samples[this.m_currentIdx] = theSample;
		if (this.m_datastoreSize < DATASTORE_CAPACITY)
		{
			++this.m_datastoreSize;
		}
	}
	
	public synchronized DataSample GetLastSample()
	{
		return this.m_samples[this.m_currentIdx];
	}
	
	public synchronized DataSample[] GetWindowSamples(int windowSize)
	{
		if (windowSize <= 0 || windowSize > this.m_datastoreSize)
		{
			windowSize = this.m_datastoreSize;
		}
		
		int samplesIdx = this.m_currentIdx + DATASTORE_CAPACITY;
		
		DataSample[] theSamples = new DataSample[windowSize];
		for (int i = 0; i < windowSize; ++i)
		{
			theSamples[i] = this.m_samples[samplesIdx % DATASTORE_CAPACITY];
			--samplesIdx;
		}		
		System.out.println("the sample in MonitoredDataSet:"+theSamples.length);
		return theSamples;
	}
}
