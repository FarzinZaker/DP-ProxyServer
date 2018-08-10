package ceraslabs.hogna.workload;

public class ClientStats
{
	/**
	 * The ID of the client from which these statistics were extracted.
	 */
	public int clientId;
	/**
	 * The interval length (milliseconds) for which these statistics are valid.
	 */
	public long intervalLength;
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
	 * The total Response Time for the successful requests. 
	 */
	public double reqSuccessResp;
	/**
	 * The total number of bytes that were received for the successful requests.
	 */
	public double reqSuccessDataIn;
	/**
	 * The total number of bytes that were sent out for successful requests.
	 */
	public double reqSuccessDataOut;
}
