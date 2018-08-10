package opera.KalmanFilter;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import opera.OperaModel;
import opera.Core.LQMException;
import opera.Core.dom.OperaErrorHandler;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class KalmanConfiguration
{
	DOMParser parser = null;
	Document doc=null;
	MeasuredMetric [] metricsMeasured  = null;
	EstimatedMetric[] metricsEstimated = null;
	ModelParameter [] modelParameters  = null;

	private OperaModel theModel = null;
	private String[] metricFilters = null;
	private String[] paramFilters = null;
	
	private Map<String, String> mapSettings = new HashMap<String, String>();

	/*
	 * The settings for the kalman estimator.
	 */
	public static final String FILE_MODEL = "modelFile";
	public static final String FILE_MODEL_RESULTS = "modelResultsFile";
	public static final String FILE_TRACE = "traceFile";
	public static final String MODEL_UPDATE = "modelUpdate";
	public static final String ITERATIONS_MAX = "iterationsMax";

	private DOMParser getParser()
	{
		if (parser != null)
		{
			return parser;
		}
		else
		{
			try
			{
				parser = new DOMParser();
				parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", true);
				parser.setFeature("http://xml.org/sax/features/validation",                  true);
				parser.setFeature("http://xml.org/sax/features/namespaces",                  true);
				parser.setFeature("http://apache.org/xml/features/validation/schema",        true);
				parser.setErrorHandler(new OperaErrorHandler());
			}
			catch (Exception ex) { }
		}
		return parser;
	}

	private void extractVariables()
	{
		NodeList lstNodesSettings = doc.getElementsByTagName("Add");
		for (int i = 0; i < lstNodesSettings.getLength(); ++i)
		{
			Element elemSetting = (Element)lstNodesSettings.item(i);
			String key   = elemSetting.getAttribute("key");
			String value = elemSetting.getAttribute("value");
			
			this.mapSettings.put(key, value);
		}
		
		/*
		 * Extract the measured metrics that will be used by the estimator.
		 */
		NodeList lstNodesMeasured = doc.getElementsByTagName("MeasuredMetric");
		this.metricsMeasured = new MeasuredMetric[lstNodesMeasured.getLength()];
		
		for (int j = 0; j < lstNodesMeasured.getLength(); j++)
		{
			Element elemMeasuredMetric = (Element)lstNodesMeasured.item(j);

			this.metricsMeasured[j] = new MeasuredMetric();

			this.metricsMeasured[j].setName(elemMeasuredMetric.getAttribute("name"));
			this.metricsMeasured[j].setDescription(elemMeasuredMetric.getAttribute("description"));
			this.metricsMeasured[j].setValue(Double.parseDouble(elemMeasuredMetric.getAttribute("default")));
			this.metricsMeasured[j].setScale(Double.parseDouble(elemMeasuredMetric.getAttribute("scale")));
		}


		/*
		 * Get the estimated metrics that will be extracted from the model.
		 * When used in a kalman filter, the measured and estimated metrics
		 * must match (same number of metrics, same name).
		 * 
		 * Consider consolidating these two sections from the config file
		 * into a single section in a future version.
		 */
		NodeList lstNodesEstimated = doc.getElementsByTagName("EstimatedMetric");
		this.metricsEstimated = new EstimatedMetric[lstNodesEstimated.getLength()];

		for (int j = 0; j <lstNodesEstimated.getLength(); j++)
		{
			Element elemEstimatedMetric = (Element)lstNodesEstimated.item(j);

			this.metricsEstimated[j] = new EstimatedMetric();

			this.metricsEstimated[j].setName(elemEstimatedMetric.getAttribute("name"));
			this.metricsEstimated[j].setDescription(elemEstimatedMetric.getAttribute("description"));
			this.metricsEstimated[j].setXPath(elemEstimatedMetric.getAttribute("xPath"));
			this.metricsEstimated[j].setValue(Double.parseDouble(elemEstimatedMetric.getAttribute("default")));
			this.metricsEstimated[j].setScale(Double.parseDouble(elemEstimatedMetric.getAttribute("scale")));
		}

		
		/*
		 * Extract the model parameters that will be estimated by the kalman filter.
		 */
		NodeList lstNodesModelParam = doc.getElementsByTagName("ModelParameter");
		modelParameters = new ModelParameter[lstNodesModelParam.getLength()];

		for (int j = 0; j < lstNodesModelParam.getLength(); j++)
		{
			Element elemModelParam = (Element)lstNodesModelParam.item(j);

			this.modelParameters[j] = new ModelParameter();

			this.modelParameters[j].setName(elemModelParam.getAttribute("name"));
			this.modelParameters[j].setDescription(elemModelParam.getAttribute("description"));
			this.modelParameters[j].setXPath(elemModelParam.getAttribute("xPath"));
			this.modelParameters[j].setValue(Double.parseDouble(elemModelParam.getAttribute("default")));
			this.modelParameters[j].setScale(Double.parseDouble(elemModelParam.getAttribute("scale")));
		}
	}

	/**
	 * Gets the model to be used by the estimator. If an object of type <code>OperaModel</code>
	 * has been specified (by a previous call to <code>SetModel(OperaModel)</code>), then
	 * this object will be returned, regardless of a possible specification of a pxl file.
	 * If no model object has been specified, this method will attempt to load the pxl file.
	 * 
	 * @return
	 *                 a model object (either specified with <code>SetModel(OperaModel)</code>
	 *                 of loaded from a pxl file). If no model object or pxl file has been
	 *                 specified, then this method will return <code>null</code>.
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	OperaModel GetModel() throws FileNotFoundException, UnsupportedEncodingException
	{
		/*
		 *  If there is already a model stored, return it;
		 *  else, load the model from the config file.
		 *  If no model is in the config file, return null.
		 */

		if (this.theModel == null)
		{
			String modelFile = this.mapSettings.get(KalmanConfiguration.FILE_MODEL);
			if (null != modelFile)
			{
				this.theModel = new OperaModel();
				this.theModel.setModel(modelFile);
			}
		}
		return this.theModel;
	}

	String GetSetting(String name)
	{
		return this.mapSettings.get(name);
	}

	MeasuredMetric[] GetMeasuredMetrics()
	{
		ArrayList<MeasuredMetric> metrics = new ArrayList<MeasuredMetric>();

		for (MeasuredMetric param : this.metricsMeasured)
		{
			if (this.metricFilters == null)
			{
				metrics.add(param);
			}
			else
			{
				for (String paramName : this.metricFilters)
				{
					if (param.name.equals(paramName))
					{
						metrics.add(param);
						break;
					}
				}
			}
		}
		
		return metrics.toArray(new MeasuredMetric[metrics.size()]);
	}

	EstimatedMetric[] GetEstimatedMetrics()
	{
		ArrayList<EstimatedMetric> metrics = new ArrayList<EstimatedMetric>();

		for (EstimatedMetric param : this.metricsEstimated)
		{
			if (this.metricFilters == null)
			{
				metrics.add(param);
			}
			else
			{
				for (String paramName : this.metricFilters)
				{
					if (param.name.equals(paramName))
					{
						metrics.add(param);
						break;
					}
				}
			}
		}
		
		return metrics.toArray(new EstimatedMetric[metrics.size()]);
	}

	ModelParameter[] GetModelParameters()
	{
		ArrayList<ModelParameter> params = new ArrayList<ModelParameter>();

		for (ModelParameter param : this.modelParameters)
		{
			if (this.paramFilters == null)
			{
				params.add(param);
			}
			else
			{
				for (String paramName : this.paramFilters)
				{
					if (param.name.equals(paramName))
					{
						params.add(param);
						break;
					}
				}
			}
		}
		
		return params.toArray(new ModelParameter[params.size()]);
	}

	
	/**
	 * Loads the configuration file. Any setting that was already specified
	 * and is also specified in the configuration file, will be overriden by the
	 * value in the configuration file.
	 * 
	 * @param uri
	 *                 the file URI that contains the configuration for the estimator.
	 */
	public void LoadConfiguration (String uri)
	{
		try
		{
			this.getParser().parse(uri);
			this.doc = this.getParser().getDocument();
			this.extractVariables();
		}
		catch (org.xml.sax.SAXException se)
		{
			throw new LQMException(se.getMessage());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Set the metric filters. When the estimator will extract the metrics
	 * (measured and estimated), only those specified here will be selected,
	 * while the other metrics will be ignored.
	 * 
	 * @param metricNames
	 *                 the names of the metrics that will be used by the estimator.
	 */
	public void SetMetricFilters(String[] metricNames)
	{
		this.metricFilters = metricNames;
	}
	
	/**
	 * Set the model parameter filters. When the estimator will extract the model
	 * parameters, only those specified here will be selected, while the other
	 * model parameters will be ignored.
	 * 
	 * @param paramNames
	 *                 the model parameters that will be estimated by the estimator.
	 */
	public void SetModelParamFilters(String[] paramNames)
	{
		this.paramFilters = paramNames;
	}

	/**
	 * Set the model to be used by the estimator. If a model was already specified,
	 * will be overriden.
	 * 
	 * @param theModel
	 *                 the model to be used by the estimator.
	 */
	public void SetModel(OperaModel theModel)
	{
		this.theModel = theModel;
	}

	/**
	 * Set the pxl file that contains the model to be used by the estimator.
	 * If a file has already been specified, it will be overriden.
	 * The model will be loaded fron the file <b>only</b> if no object of type
	 * <code>OperaModel</code> has been specified.
	 * 
	 * @param sModelFile
	 *                 the pxl file that contains the description of the model. 
	 */
	public void SetModel(String sModelFile)
	{
		this.mapSettings.put(KalmanConfiguration.FILE_MODEL, sModelFile);
	}

	/**
	 * Sets a setting for the estimator. The available settings are:
	 * <ul>
	 *     <li>modelFile</li>
	 *     <li>modelResultsFile</li>
	 *     <li>modelUpdate</li>
	 *     <li>traceFile</li>
	 *     <li>iterationsMax</li>
	 * </ul>
	 * Any previously specified value will be overriden.
	 * 
	 * @param settingName
	 * @param settingValue
	 */
	public void SetSetting(String settingName, String settingValue)
	{
		this.mapSettings.put(settingName, settingValue);
	}

	/**
	 * Will call <code>{@link #LoadConfiguration(String) LoadConfiguration}</code> and returns this object.
	 * This method is usefull for chaining calls.
	 * 
	 * @param uri
	 *                 the file URI that contains the configuration for the estimator.
	 * @return
	 *                 this object that can be used to chain calls.
	 */
	public KalmanConfiguration withConfigFile(String uri)
	{
		this.LoadConfiguration(uri);
		return this;
	}

	/**
	 * Will call <code>{@link #SetModel(OperaModel) SetModel}</code> and returns this object.
	 * This method is usefull for chaining calls.
	 * 
	 * @param theModel
	 *                 the model to be used by the estimator.
	 * @return
	 *                 this object that can be used to chain calls.
	 */
	public KalmanConfiguration withModel(OperaModel theModel)
	{
		this.SetModel(theModel);
		return this;
	}

	/**
	 * Will call <code>{@link #SetModel(String) SetModel}</code> and returns this object.
	 * This method is usefull for chaining calls.
	 * 
	 * @param sModelFile
	 *                 the pxl file that contains the description of the model.
	 * @return
	 *                 this object that can be used to chain calls.
	 */
	public KalmanConfiguration withModel(String sModelFile)
	{
		this.SetModel(sModelFile);
		return this;
	}

	/**
	 * Will call <code>{@link #SetMetricFilters(String[]) SetMetricFilters}</code> and returns this object.
	 * This method is usefull for chaining calls.
	 * 
	 * @param metricNames
	 *                 the metric names that will be passed to <code>SetMetricFilters</code>.
	 * @return
	 *                 this object that can be used to chain calls.
	 */
	public KalmanConfiguration withMetricFilters(String[] metricNames)
	{
		this.SetMetricFilters(metricNames);
		return this;
	}

	/**
	 * Will call <code>{@link #SetModelParamFilters(String[]) SetModelParamFilters}</code>
	 * and returns this object.
	 * 
	 * @param paramNames
	 *                 the model parameter names that will be passed to <code>SetModelParamFilters</code>.
	 * @return
	 *                 this object that can be used to chain calls.
	 */
	public KalmanConfiguration withModelParamFilters(String[] paramNames)
	{
		this.SetModelParamFilters(paramNames);
		return this;
	}

	/**
	 * Will call <code>{@link #SetSetting(String, String) SetSetting}</code>
	 * and returns this object.
	 * 
	 * @param settingName
	 * @param settingValue
	 * 
	 * @return
	 *                 this object that can be used to chain calls.
	 */
	public KalmanConfiguration withSetting(String settingName, String settingValue)
	{
		this.SetSetting(settingName, settingValue);
		return this;
	}
}
