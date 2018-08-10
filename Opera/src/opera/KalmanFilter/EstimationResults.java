package opera.KalmanFilter;

public class EstimationResults
{
	MeasuredMetric[] metricsMeasured = null;

	EstimatedMetric[] metricsEstimatedInitial = null;
	EstimatedMetric[] metricsEstimatedFinal   = null;
	
	ModelParameter[] modelParametersInitial = null;
	ModelParameter[] modelParametersFinal = null;
	
	int cntIterationsExecuted = 0;

	public ModelParameter[] getModelParametersFinal(){
		return this.modelParametersFinal;
		}
	
	
	public EstimationResults withMeasuredMetrics(MeasuredMetric[] metricsMeasured)
	{
		this.SetMeasuredMetrics(metricsMeasured);
		return this;
	}

	public EstimationResults withInitialEstimatedMetrics(EstimatedMetric[] metricsEstimated)
	{
		this.SetInitialEstimatedMetrics(metricsEstimated);
		return this;
	}
	
	public EstimationResults withFinalEstimatedMetrics(EstimatedMetric[] metricsEstimated)
	{
		this.SetFinalEstimatedMetrics(metricsEstimated);
		return this;
	}
	
	public EstimationResults withInitialModelParameters(ModelParameter[] params)
	{
		this.SetInitialModelParameters(params);
		return this;
	}
	
	public EstimationResults withFinalModelParameters(ModelParameter[] params)
	{
		this.SetFinalModelParameters(params);
		return this;
	}
	
	public EstimationResults withCountIterationsExecuted(int cntIterations)
	{
		this.SetCountIterationsExecuted(cntIterations);
		return this;
	}

	public void SetMeasuredMetrics(MeasuredMetric[] metricsMeasured)
	{
		this.metricsMeasured = new MeasuredMetric[metricsMeasured.length];
		for (int i = 0; i < metricsMeasured.length; ++i)
		{
			this.metricsMeasured[i] = new MeasuredMetric(metricsMeasured[i]);
		}
	}

	public void SetInitialEstimatedMetrics(EstimatedMetric[] metricsEstimated)
	{
		this.metricsEstimatedInitial = new EstimatedMetric[metricsEstimated.length];
		for (int i = 0; i < metricsEstimated.length; ++i)
		{
			this.metricsEstimatedInitial[i] = new EstimatedMetric(metricsEstimated[i]);
		}
	}

	public void SetFinalEstimatedMetrics(EstimatedMetric[] metricsEstimated)
	{
		this.metricsEstimatedFinal = new EstimatedMetric[metricsEstimated.length];
		for (int i = 0; i < metricsEstimated.length; ++i)
		{
			this.metricsEstimatedFinal[i] = new EstimatedMetric(metricsEstimated[i]);
		}
	}
	public void SetInitialModelParameters(ModelParameter[] params)
	{
		this.modelParametersInitial = new ModelParameter[params.length];
		for (int i = 0; i < params.length; ++i)
		{
			this.modelParametersInitial[i] = new ModelParameter(params[i]);
		}
	}

	public void SetFinalModelParameters(ModelParameter[] params)
	{
		this.modelParametersFinal = new ModelParameter[params.length];
		for (int i = 0; i < params.length; ++i)
		{
			this.modelParametersFinal[i] = new ModelParameter(params[i]);
		}
	}
	
	public void SetCountIterationsExecuted(int cntIterations)
	{
		this.cntIterationsExecuted = cntIterations;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("Estimated [%d] variables, using [%d] metric values.\n",
				this.modelParametersInitial.length,
				this.metricsMeasured.length));
		sb.append(String.format("Iteration count: [%d].\n\n", this.cntIterationsExecuted));
		
		sb.append("--------------------------------------------------------------|\n");
		sb.append("                                in|          out|         diff|\n");
		sb.append("--------------------------------------------------------------|\n");
		for (int i = 0; i < this.modelParametersInitial.length; ++i)
		{
			sb.append(String.format("%20s", this.modelParametersInitial[i].name));
			sb.append(String.format("  %12.5f", this.modelParametersInitial[i].value));
			sb.append(String.format("  %12.5f", this.modelParametersFinal[i].value));
			sb.append(String.format("  %12.5f", this.modelParametersFinal[i].value - this.modelParametersInitial[i].value));
			sb.append("|\n");
		}

		sb.append("------------------------------------------------------------------------------------------|\n");
		sb.append("                          measured|          out| out error(%)|           in|  in error(%)|\n");
		sb.append("------------------------------------------------------------------------------------------|\n");
		for (int i = 0; i < this.metricsMeasured.length; ++i)
		{
			sb.append(String.format("%20s", this.metricsMeasured[i].name));
			sb.append(String.format("  %12.5f", this.metricsMeasured[i].value));
			sb.append(String.format("  %12.5f", this.metricsEstimatedFinal[i].value));
			sb.append(String.format("  %12.2f", 100 * (1 - this.metricsEstimatedFinal[i].value / this.metricsMeasured[i].value)));
			sb.append(String.format("  %12.5f", this.metricsEstimatedInitial[i].value));
			sb.append(String.format("  %12.2f", 100 * (1 - this.metricsEstimatedInitial[i].value / this.metricsMeasured[i].value)));
			sb.append("|\n");
		}
		sb.append("------------------------------------------------------------------------------------------|\n");
		
		return sb.toString();
	}
}
