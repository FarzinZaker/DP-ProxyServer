package opera;

public class OperaKalmanFilter
{
	// when doing a calibration, if the model does not become synchronized after "CNT_ITERATIONS_MAX", give up;
	private final int CNT_ITERATIONS_MAX = 20;

	public void EstimateModelParameters(OperaModel theModel)
	{
		for (int i = 0; i < CNT_ITERATIONS_MAX; ++i)
		{
			
		}
	}
}
