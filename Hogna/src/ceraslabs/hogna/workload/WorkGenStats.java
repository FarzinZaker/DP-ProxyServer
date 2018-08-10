package ceraslabs.hogna.workload;

public class WorkGenStats
{
	/**
	 * The ID of the workload generator that generated these statistics.
	 */
	public int wgId;
	/**
	 * The number of requests that failed on client. This includes failures as:
	 * <ul>
	 *   <li>timeout</li>
	 *   <li>broken communication</li>
	 *   <li>an exception on client.</li>
	 * </ul>
	 */
	public int reqErrorClientCount;
	/**
	 * The number of requests failed on server.
	 */
	public int reqErrorServerCount;
	/**
	 * The number of requests that were successful.
	 */
	public int reqSuccessCount;
	/**
	 * The average Response Time for successful requests. 
	 */
	public double reqSuccessResp;
	/**
	 * The average number of bytes that were received for successful requests.
	 */
	public double reqSuccessDataIn;
	/**
	 * The average number of bytes that were sent out for successful requests.
	 */
	public double reqSuccessDataOut;
}
