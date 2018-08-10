package opera.KalmanFilter;


import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import opera.Core.Matrix;
import opera.OperaModel;

public class KalmanEstimator implements Estimator
{

	MeasuredMetric[] measuredMetrics;

	EstimatedMetric[] estimatedMetrics;

	ModelParameter[] modelParameters;
	
	OperaModel theModel = null;
	KalmanConfiguration theConfig = null;
	
	// temporary check for "Error Stopping Criteria"
	private static double errorAccepted = 0.05;

	
	// some settings for the filter
	private String  modelResultsFile = null;
	private boolean modelUpdate = true;
	private int cntIterationsMax = 1;
	private String traceFile = null;

	
	// assume the tracking parameters may change 20% for a step
	double qFactor = 0.2;

	// assume 90% confidence interval is within the 10% of the measurement
	double rFactor = 0.1;

	// the number of measurements used in the filter
	int NUM_OF_MEASUREMENTS = 4;

	// the number of parameters to be
	int NUM_OF_PARAMETERS = 3;

	// the real measurements
	double[][] zMat;

	// the R matrix
	double[][] rMat;

	// the Q matrix
	double[][] qMat;

	// the P matrix
	double[][] pMat; 

	// the tracking parameters
	double[][] xMat;

	// the H matrix
	double[][] hMat;

	// the predictions for the measurements
	double[][] hxMat;

	// the strings of the x values
	String[] parameters;

	// the thresohod values of the tracking parameters (to avoid negative values)
	double[][] xThresholdMat;

	// the parameter used to calculate sensitivity data
	double DIFFERENCE = 0.01;

	// the parameter set for the
	double THRESHOLD_PARAMETER = 0.1;

	public KalmanEstimator(KalmanConfiguration config) throws FileNotFoundException, UnsupportedEncodingException
	{
		this.theConfig = config;

		// get data from config
		this.theModel = this.theConfig.GetModel();
		this.measuredMetrics = this.theConfig.GetMeasuredMetrics();
		this.estimatedMetrics = this.theConfig.GetEstimatedMetrics();
		this.modelParameters = this.theConfig.GetModelParameters();

		String setting = this.theConfig.GetSetting("iterationsMax");
		this.cntIterationsMax = (setting == null ? this.cntIterationsMax : Integer.parseInt(setting));

		setting = this.theConfig.GetSetting("modelUpdate");
		this.modelUpdate = (setting == null ? this.modelUpdate : Boolean.parseBoolean(setting));
		
		this.modelResultsFile = this.theConfig.GetSetting("modelResultsFile");
		this.traceFile = this.theConfig.GetSetting("traceFile");
		
		if (null != this.traceFile)
		{
			this.WriteTraceHeaders();
		}

		NUM_OF_PARAMETERS = this.modelParameters.length;
		NUM_OF_MEASUREMENTS = this.measuredMetrics.length;

		// the real measurements
		zMat = new double[NUM_OF_MEASUREMENTS][1];

		// the R matrix
		rMat = new double[NUM_OF_MEASUREMENTS][NUM_OF_MEASUREMENTS];

		// the Q matrix
		qMat = new double[NUM_OF_PARAMETERS][NUM_OF_PARAMETERS];

		// the P matrix
		pMat = new double[NUM_OF_PARAMETERS][NUM_OF_PARAMETERS];

		// the tracking parameters
		xMat = new double[NUM_OF_PARAMETERS][1];

		// the H matrix
		hMat = new double[NUM_OF_MEASUREMENTS][NUM_OF_PARAMETERS];

		// the predictions for the measurements
		hxMat = new double[NUM_OF_MEASUREMENTS][1];

		// the strings of the x values
		parameters = new String[NUM_OF_PARAMETERS];

		// the thresohod values of the tracking parameters (to avoid negative values)
		xThresholdMat = new double[NUM_OF_PARAMETERS][1];
		
		this.setMatrices();
	}

	private void setMatrices()
	{
		// initialize the tracking parameters. (manually setup currently)

		// initialize pMat, qMat, xMat for the Extended Kalman filter
		for (int i = 0; i < modelParameters.length; i++)
		{
			xMat[i][0] = modelParameters[i].value;
		}

		for (int i = 0; i < NUM_OF_PARAMETERS; i++)
		{
			for (int j = 0; j < NUM_OF_PARAMETERS; j++)
			{
				if (i != j)
				{
					pMat[i][j] = 0;
					qMat[i][j] = 0;
    			}
				else
				{
					pMat[i][i] = xMat[i][0] * xMat[i][0];
					qMat[i][i] = qFactor * qFactor * xMat[i][0] * xMat[i][0];
				}
			}
			xThresholdMat[i][0] = THRESHOLD_PARAMETER * xMat[i][0];
		}
		pMat = Matrix.add(pMat, qMat);

		for (int i = 0; i < NUM_OF_PARAMETERS; i++)
		{
			parameters[i] = Double.toString(xMat[i][0]);
		}

		// should initialize hxmat
		for (int i = 0; i < NUM_OF_MEASUREMENTS; i++)
		{
			hxMat[i][0] = estimatedMetrics[i].value * estimatedMetrics[i].scale;
		}
    }

	/* (non-Javadoc)
	 * @see com.ibm.apera.autonomic.Estimator#estimate(com.ibm.apera.autonomic.MeasuredMetric[], com.ibm.apera.autonomic.ModelParameter[], com.ibm.apera.autonomic.ResultParameter[])
	 */
	private void estimate()
	{
		//this is based on QEST 2005 paper

		// measure z
		for (int i = 0; i < NUM_OF_MEASUREMENTS; i++)
		{
			zMat[i][0] = measuredMetrics[i].getValue() * measuredMetrics[i].getScale();
		}

		//compute R; in the paper, R is time invariant, in reality it is not
		for (int i = 0; i < NUM_OF_MEASUREMENTS; i++)
		{
			for (int j = 0; j < NUM_OF_MEASUREMENTS; j++)
			{
				if (i == j)
				{
					rMat[i][i] = rFactor * rFactor * zMat[i][0] * zMat[i][0]
							/ 1.96 / 1.96;
				}
				else
				{
					rMat[i][j] = 0;
				}
			}
		}

		// step 1 prediction
		//  step 1.1 project the state ahead xk = xk-1, is implicit.
		//  step 1.2 project the estimated covariance ahead is done as part of the 2.3 (see below)

		//pMat=Matrix.add(pMat,qMat);

		// step 2, correction (feedback)
		//  step 2.1 compute the matrix K
		ExtendedKalmanFilter kf = new ExtendedKalmanFilter(pMat, xMat, rMat, hMat, zMat, hxMat, qMat);
		
		//  step 2.2 and step 2.3 correct the covariance matrix pmat and the state xmat
		pMat = kf.getProjectedPMatrix();
		xMat = kf.getUpdatedXMatrix();
		// adjust tracking values if those values are less than threshold values
		for (int i = 0; i < NUM_OF_PARAMETERS; i++)
		{
			if (xMat[i][0] < xThresholdMat[i][0])
			{
				xMat[i][0] = xThresholdMat[i][0];
			}

			xThresholdMat[i][0] = THRESHOLD_PARAMETER * xMat[i][0];
			this.modelParameters[i].setValue(xMat[i][0]);
			parameters[i] = Double.toString(xMat[i][0]);
		}
	}

	/**
	 * Extract from the model the estimated metrics and store them in
	 * "estimatedMetrics" array.
	 * 
	 * @param theModel
	 */
	private void UpdateEstimatedMetrics(OperaModel theModel)
	{
		for (int i = 0; i < estimatedMetrics.length; ++i)
		{
			// we have a XPath expression. We will use that to extract a value from results
			estimatedMetrics[i].setValue(theModel.GetXPathResultsDouble(estimatedMetrics[i].xPath));
		}
	}

	/**
	 * Updates the model object with specified values for the parameters.
	 * 
	 */
	private void UpdateModelParameters(OperaModel theModel, String[] parameters)
	{
		for (int i = 0; i < parameters.length; ++i)
		{
			theModel.SetXPathModelNodesValue(modelParameters[i].xPath, parameters[i]);
		}
	}

	private void UpdateModelParameters(ModelParameter[] modelParameters)
	{
		for (int i = 0; i < modelParameters.length; ++i)
		{
			this.theModel.SetXPathModelNodesValue(modelParameters[i].xPath, Double.toString(modelParameters[i].value));
		}
	}
	
	private void UpdateModelParameters()
	{
		this.UpdateModelParameters(this.modelParameters);
	}

	private void updateSensitivity(OperaModel m)
	{
		for (int j = 0; j < NUM_OF_PARAMETERS; j++)
		{
			parameters[j] = Double.toString(xMat[j][0]);
		}

		this.UpdateModelParameters(m, parameters);
		m.solve();
		this.UpdateEstimatedMetrics(m);

		// save the existing estimated model parameters...
		String [] temp = new String [parameters.length];

		for (int i = 0; i < parameters.length; i++)
		{
			temp[i] = new String(Double.toString(xMat[i][0]));
		}
	  
		// upgrade the predictions for the measurements (hxMat)
		for (int i = 0; i < NUM_OF_MEASUREMENTS; i++)
		{
			hxMat[i][0] = estimatedMetrics[i].getValue() * estimatedMetrics[i].getScale();
		}

		// get the sensitivity matrix (hMat)
		for (int i = 0; i < NUM_OF_PARAMETERS; i++)
		{
			// increase one parameter 1% a time
			for (int j = 0; j < NUM_OF_PARAMETERS; j++)
			{
				if (i == j)
				{
					parameters[j] = Double.toString(xMat[j][0] * (1 + DIFFERENCE));
				}
				else
				{
					parameters[j] = Double.toString(xMat[j][0]);
				}
			}

			// save and solve the model
			this.UpdateModelParameters(m, parameters);
			m.solve();
			this.UpdateEstimatedMetrics(m);

			for (int k = 0; k < NUM_OF_MEASUREMENTS; k++)
			{
				hMat[k][i] = (estimatedMetrics[k].getValue() - hxMat[k][0]/estimatedMetrics[k].getScale())
						/ xMat[i][0] / DIFFERENCE;
			}
		}
	
		//restore the old values...
		this.UpdateModelParameters(m, temp);
		m.solve();
		this.UpdateEstimatedMetrics(m);
	}

	/**
	 * Estimates the model parameters using the model and the values for
	 * the measured metrics already specified. 
	 * 
	 * @return
	 */
	public EstimationResults EstimateModelParameters()
	{
		this.UpdateModelParameters();
		this.theModel.solve();
		this.UpdateEstimatedMetrics(theModel);

		
		EstimationResults results = new EstimationResults();
		results.withMeasuredMetrics(this.measuredMetrics)
		       .withInitialEstimatedMetrics(this.estimatedMetrics)
		       .withInitialModelParameters(this.modelParameters);

		int it = 0;
		for (it = 0; it < this.cntIterationsMax; ++it)
		{
			this.updateSensitivity(theModel);
			this.estimate();

			this.UpdateModelParameters();
			this.theModel.solve();
			this.UpdateEstimatedMetrics(theModel);

			if (null != this.traceFile)
			{
				this.WriteTrace(it);
			}
			
			{
				// temporary check for "Error Stopping Criteria"
				double errorAvg = 0;
				boolean shouldStop = true;
				boolean forEach = true;
				for (int i = 0; i < this.measuredMetrics.length; ++i)
				{
					double error =  Math.abs(1.0 -this.measuredMetrics[i].getValue() / this.estimatedMetrics[i].getValue());
					if (forEach && error > errorAccepted)
					{
						shouldStop = false;
						break;
					}
					else if (forEach == false)
					{
						errorAvg += error;
					}
				}
				if (forEach == false && (errorAvg /= this.measuredMetrics.length) > errorAccepted)
				{
					shouldStop = false;
				}
				if (shouldStop)
					break;
			}

			System.out.println(this.GetErrorAsTable(this.measuredMetrics, this.estimatedMetrics));
		}

		// save the model if it's required
		if (null != this.modelResultsFile)
		{
			this.theModel.SaveResultsToXmlFile(this.modelResultsFile);
		}

		// the model has the latest values for the parameters added, and is solved.
		// check if we should restore it to the initial values.
		if (false == this.modelUpdate)
		{
			this.UpdateModelParameters(results.modelParametersInitial);
			this.theModel.solve();
			this.UpdateEstimatedMetrics(theModel);
		}

		results.withCountIterationsExecuted(it + 1)
		       .withFinalEstimatedMetrics(this.estimatedMetrics)
		       .withFinalModelParameters(this.modelParameters);
		
		return results;
	}
	
	public EstimationResults EstimateModelParameters(double[] measuredMetrics)
	{
		for (int i = 0; i < this.measuredMetrics.length; ++i)
		{
			this.measuredMetrics[i].setValue(measuredMetrics[i] + 0.000001);
		}
		
		return this.EstimateModelParameters();
	}
	
	public EstimationResults EstimateModelParameters(MeasuredMetric[] measuredMetrics)
	{
		for (int i = 0; i < this.measuredMetrics.length; ++i)
		{
			this.measuredMetrics[i].setValue(measuredMetrics[i].value + 0.000001);
		}

		return this.EstimateModelParameters();
	}
	
	private String GetErrorAsTable(MeasuredMetric[] measuredData, EstimatedMetric[] modeledData)
	{
		StringBuilder sb = new StringBuilder();
		
		
		sb.append("    Measured       Modeled         Error\n");
		for (int i = 0; i < measuredData.length; ++i)
		{
			sb.append(String.format("%12.4f", measuredData[i].getValue()));
			sb.append(String.format("  %12.4f", modeledData[i].getValue()));
			sb.append(String.format("        %6.2f\n", 100 * Math.abs(1 - (modeledData[i].getValue() + 0.000001) / measuredData[i].getValue())));
		}
		
		return sb.toString();
	}
	
	private void WriteTraceHeaders()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("# " + this.ResizeString("ID", 20));
		sb.append("| " + this.ResizeString("iteration", 20));

		for (int i = 0; i < this.measuredMetrics.length; ++i)
		{
			sb.append("| " + this.ResizeString("M_" + this.measuredMetrics[i].name, 20));
			sb.append("| " + this.ResizeString("E_" + this.estimatedMetrics[i].name, 20));
		}
		
		for (int i = 0; i < this.modelParameters.length; ++i)
		{
			sb.append("| " + this.ResizeString(this.modelParameters[i].name, 20));
		}

		try
		{
    		BufferedWriter out;
			out = new BufferedWriter(new FileWriter(this.traceFile, true));
    		out.write(sb.toString());
    		out.write("\n");
    		out.close();
		}
		catch (IOException e)
		{
			// we failed to write; Disable trace.
			this.traceFile = null;
		}

	}

	private void WriteTrace(int iteration)
	{
		String strOutput = "";

		strOutput = String.format("%s    %18d", strOutput, System.currentTimeMillis());
		strOutput = String.format("%s    %18d", strOutput, iteration);

		for (int i = 0; i < this.measuredMetrics.length; ++i)
		{
			strOutput = String.format("%s    %18.8f", strOutput, this.measuredMetrics[i].value);
			strOutput = String.format("%s    %18.8f", strOutput, this.estimatedMetrics[i].value);
		}
		
		for (int i = 0; i < this.modelParameters.length; ++i)
		{
			strOutput = String.format("%s    %18.8f", strOutput, this.modelParameters[i].value);
		}
		
		try
		{
    		BufferedWriter out;
			out = new BufferedWriter(new FileWriter(this.traceFile, true));
    		out.write(strOutput + "\n");
    		out.close();
		}
		catch (IOException e)
		{
			// we failed to write; Disable trace.
			this.traceFile = null;
		}
	}
	
	private String ResizeString(String str, int size)
	{
		if (size < str.length())
		{
			str = str.substring(0, size - 3) + "...";
		}
		return String.format("%" + size + "s", str);
	}

	
	public static void main (String... args) throws FileNotFoundException, UnsupportedEncodingException
	{
		//OperaModel theModel = new OperaModel();
		//theModel.setModel("./input/Simple DB Operations.model.pxl");

		KalmanConfiguration config = new KalmanConfiguration();
		config.withConfigFile("./input/Simple DB Operations.kalman.config")
		      .withSetting(KalmanConfiguration.ITERATIONS_MAX, "50")
//		      .withSetting(KalmanConfiguration.FILE_TRACE, null)
		      .withSetting(KalmanConfiguration.MODEL_UPDATE, "true")
		      .withSetting(KalmanConfiguration.FILE_MODEL_RESULTS, null)
//		      .withModel(theModel)
		      .withMetricFilters(new String[] {"CpuUtil_Web", "CpuUtil_Db", "ResponseTimeInsert", "ThroughputInsert"})
		      .withModelParamFilters(new String[] {"CPUDemand_Proxy", "CPUDemand_Web_Insert", "CPUDemand_Db_Insert"})
		      ;

		      
		KalmanEstimator theEstimator = new KalmanEstimator(config);
		
//		EstimationResults results = theEstimator.EstimateModelParameters(new double[]{0.6320, 0.0958, 43.0763, 41.7298, 73.8675, 237.6452, 684.9125, 1846.9524, 0.0137524, 0.0136861, 0.0013827, 0.0007747, 0.0006664, 0.0001749});
		EstimationResults results = theEstimator.EstimateModelParameters(new double[]{0.6320, 0.0958, 43.0763, 0.0137524});
	
		System.out.print(results);
	}
}
