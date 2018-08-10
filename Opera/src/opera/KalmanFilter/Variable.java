package opera.KalmanFilter;


public class Variable
{
	String name=null;
	String description=null;
	double value=0;
	double scale=1;
	
	public Variable()
	{
		super();
	}
	
	public Variable (String sName, String sDescription, double dValue)
	{
		this.name = sName;
		this.value = dValue;
		this.description = sDescription;
	}
	
	public Variable (Variable other)
	{
		this.name = other.name;
		this.description = other.description;
		this.scale = other.scale;
		this.value = other.value;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String sName)
	{
		this.name = sName;
	}

	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}

	public double getValue()
	{
		return value;
	} 
	
	public void setValue(double v)
	{
		value = v;
	} 

	public double getScale()
	{
		return scale;
	}

	public void setScale(double scale)
	{
		this.scale = scale;
	}


/*	String scenario=null;
	final int bufferSize=2;
	double [] dataBuffer= new double[bufferSize];// buffer the last 2 values...
	double [] invocations= new double[bufferSize];// buffer the last 2 values...
	double averageInvocations=0;
	double [] sOfSquares= new double[bufferSize];// buffer the last 2 values...
	double averageSOfSquares=0;
	double [] timeStamps= new double[bufferSize];// buffer the last 2 values...
    double averageTimeStamps=0;
	double variance=0;
	Cluster cluster=null;

	
	public Cluster getCluster() {
		return cluster;
	}



	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}



	public double getAverageInvocations() {
		return averageInvocations;
	}



	public void setAverageInvocations(double averageInvocations) {
		this.averageInvocations = averageInvocations;
	}



	public double getAverageSOfSquares() {
		return averageSOfSquares;
	}



	public void setAverageSOfSquares(double averageSOfSquares) {
		this.averageSOfSquares = averageSOfSquares;
	}



	public double getAverageTimeStamps() {
		return averageTimeStamps;
	}



	public void setAverageTimeStamps(double averageTimeStamps) {
		this.averageTimeStamps = averageTimeStamps;
	}



	public double[] getDataBuffer() {
		return dataBuffer;
	}



	public void setDataBuffer(double[] dataBuffer) {
		this.dataBuffer = dataBuffer;
	}







	public String getScenario() {
		return scenario;
	}

	public void setScenario(String scenario) {
		this.scenario = scenario;
	}






	public double[] getBuffer() {
		return dataBuffer;
	}



	public void setBuffer(double[] buffer) {
		this.dataBuffer = buffer;
	}



	public double getVariance() {
		return variance;
	}



	public void setVariance(double variance) {
		this.variance = variance;
	}



	public int getBufferSize() {
		return bufferSize;
	}



	public double[] getInvocations() {
		return invocations;
	}



	public void setInvocations(double[] invocations) {
		this.invocations = invocations;
	}



	public double[] getSOfSquares() {
		return sOfSquares;
	}



	public void setSOfSquares(double[] ofSquares) {
		sOfSquares = ofSquares;
	}



	public double[] getTimeStamps() {
		return timeStamps;
	}



	public void setTimeStamps(double[] timeStamps) {
		this.timeStamps = timeStamps;
	} 
*/

	@Override public String toString()
	{
		// <name> = [<value>] {<description>}
		StringBuilder sb = new StringBuilder();
		sb.append(this.name);
		sb.append(" = [");
		sb.append(this.value);
		sb.append("] {");
		sb.append(this.description);
		sb.append("}");
		
		return sb.toString();
	}

	/**
	 * Returns a string that contains all the values from the array separated
	 * with commas.
	 * 
	 * @author Cornel
	 * 
	 * @param format
	 * 				the format of each value in the array.
	 * @param array
	 * 				the array to format as string.
	 * @return a string representation of the array.
	 */
	public static String ToString(String format, Variable[] array)
	{
		StringBuilder result = new StringBuilder();
		
    	// put the first value from array.
    	// this value is added separately because there is no ',' before the it
    	if (array.length > 0)
    	{
    		result.append(String.format(format, array[0].getValue()));
    	}
    	
    	for (int i = 1; i < array.length; ++i)
    	{
    		result.append(", ");
    		result.append(String.format(format, array[i].getValue()));
    	}

		return result.toString();
	}

	/**
	 * Returns a string that contains all the values from the array separated
	 * with commas. Each value will be formatted as %9.4f
	 * 
	 * @author Cornel
	 * 
	 * @param array
	 * 				the array to format as string.
	 * @return a string representation of the array.
	 * @see #ToString(String, Variable[])
	 */
	public static String ToString(Variable[] array)
	{
		return Variable.ToString("%12.9f", array);
	}
}
