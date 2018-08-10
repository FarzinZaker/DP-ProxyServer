package ceraslabs.hogna;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import opera.OperaModel;
import opera.KalmanFilter.KalmanConfiguration;
import opera.KalmanFilter.KalmanEstimator;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;
import ceraslabs.hogna.configuration.ConfigurationManager;
import ceraslabs.hogna.configuration.DataStoreConfigurationSection;
import ceraslabs.hogna.configuration.DataStoreFileConfigurationSection;
import ceraslabs.hogna.configuration.MetricMappingsConfigurationSection;
import ceraslabs.hogna.configuration.TopologyConfigurationSection;
import ceraslabs.hogna.data.IDataStore;
import ceraslabs.hogna.executor.Executor;
import ceraslabs.hogna.monitoring.MonitorManager;
import ceraslabs.hogna.monitoring.TopologyMonitorManager;

public class HognaEngineBuilder
{
	private IAnalyzer theAnalyzer = null;
	private IPlanner  thePlanner  = null;
	private IExecutor theExecutor = null;
	private MonitorManager theMonitorManager = null;
	
	private String theModelFileName  = null;
	private String theFilterFileName = null;
	
	private IDataStore theDataStore = null;
	
	public HognaEngineBuilder withMonitorManager(TopologyMonitorManager theMonitorManager)
	{
		this.theMonitorManager = theMonitorManager;
		return this;
	}
	public HognaEngineBuilder withAnalyzer(IAnalyzer theAnalyzer)
	{
		this.theAnalyzer = theAnalyzer;
		return this;
	}
	
	public HognaEngineBuilder withPlanner(IPlanner thePlanner)
	{
		this.thePlanner = thePlanner;
		return this;
	}
	
	public HognaEngineBuilder withExecutor(IExecutor theExecutor)
	{
		this.theExecutor = theExecutor;
		return this;
	}
	
	public HognaEngineBuilder withModel(String modelFilename)
	{
		this.theModelFileName = modelFilename;
		return this;
	}
	
	public HognaEngineBuilder withKalmanFilter(String filterFilename)
	{
		this.theFilterFileName = filterFilename;
		return this;
	}
	
	public HognaEngineBuilder withDataStore(String string)
	{
		DataStoreConfigurationSection secDataStore = (DataStoreConfigurationSection)ConfigurationManager.GetSection("data-store-file");
		this.theDataStore = secDataStore.GetDataStore();
		return this;
	}

	
	
	public HognaEngine Build() throws FileNotFoundException, UnsupportedEncodingException
	{
		if (this.theAnalyzer == null)
			throw new NullPointerException("Invalid analyzer.");
		if (this.thePlanner == null)
			throw new NullPointerException("Invalid planner.");
		
		// if no executor is specified, use the default executor.
		if (this.theExecutor == null)
		{
			this.theExecutor = new Executor();
		}
		if (this.theMonitorManager == null)
		{
			this.theMonitorManager = new MonitorManager();
		}

		// put in the model. If there was a model specified using the "with...()"
		// functions, use that one. If there is none, check the configuration file.
		// If there is none there either, do not use models.
		OperaModel theModel          = null;
		KalmanEstimator theEstimator = null;
		if (this.theModelFileName == null)
		{
			// check if there is a PXL filename in the configuration file.
			this.theModelFileName = ConfigurationManager.GetSetting("HognaEngine.ModelFileName");
		}
		if (this.theModelFileName != null)
		{
			File fileModel = new File(this.theModelFileName);
			Trace.WriteLine(TraceLevel.INFO, "Using model from [%s].", this.theModelFileName);
			theModel = new OperaModel();
			theModel.setModel(fileModel.getAbsolutePath());
			
			// attempt to load a Kalman configurator.
			if (this.theFilterFileName == null)
			{
				this.theFilterFileName = ConfigurationManager.GetSetting("HognaEngine.FilterFileName");
			}
			if (this.theFilterFileName == null)
			{
				Trace.WriteLine(TraceLevel.INFO, "The model will not be calibrated, because no filter has been specified.");
			}
			else
			{
				KalmanConfiguration kalmanConfig = new KalmanConfiguration();
				kalmanConfig.withConfigFile(this.theFilterFileName)
							.withModel(theModel)
							.withSetting(KalmanConfiguration.ITERATIONS_MAX, "20");
				
				theEstimator = new KalmanEstimator(kalmanConfig);
			}
		}
		else
		{
			Trace.WriteLine(TraceLevel.INFO, "Not using a model to analyze the traffic.");
		}
		
		
		
		
		// Validation done. Everything looks good now.
		HognaEngine theEngine = new HognaEngine();
		
		// the MAPE components
		theEngine.m_theMonitorManager = this.theMonitorManager;
		theEngine.theAnalyzer = this.theAnalyzer;
		theEngine.thePlanner = this.thePlanner;
		theEngine.theExecutor = this.theExecutor;
		
		// the model components
		theEngine.theModel = theModel;
		theEngine.theEstimator = theEstimator;
		MetricMappingsConfigurationSection secMetricMappings = (MetricMappingsConfigurationSection)ConfigurationManager.GetSection("metricMappings");
		if (secMetricMappings != null)
		{
			theEngine.mapMetricsToModel = secMetricMappings.GetMetricsToModelMappings();
			theEngine.mapMetricsToFilter = secMetricMappings.GetMetricsToFilterMappings();
		}

		// the topology
		TopologyConfigurationSection secTopology = (TopologyConfigurationSection)ConfigurationManager.GetSection("topology");
		theEngine.theTopology = secTopology.GetTopology();

		theEngine.theDataStore = this.theDataStore;
		
		// some other settings
		String strInterval = ConfigurationManager.GetSetting("HognaEngine.Interval");
		if (strInterval != null)
		{
			theEngine.m_timeItInterval = Long.parseLong(strInterval);
		}

		String strWait = ConfigurationManager.GetSetting("HognaEngine.WaitAfterAction");
		if (strWait != null)
		{
			theEngine.m_timeWaitAfterAction = Long.parseLong(strWait);
		}

		theEngine.Initialize();
		return theEngine;
	}
}
