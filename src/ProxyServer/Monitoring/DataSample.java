package ProxyServer.Monitoring;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ProxyServer.User;

public class DataSample implements Serializable {
	private static final long serialVersionUID = 3454295439885844304L;
	public String[] scenarioNames = null;
	/**
	 * The average response time for the requests that finished this sample
	 * period.
	 */
	public double[] responseTime = null;
	/**
	 * The number of that finished processing this sample period.
	 */
	public int[] requestsProcessedCnt = null;
	/**
	 * The number of new requests that came in this sample period.
	 */
	public int[] requestsCnt = null;
	/**
	 * The number of new requests that came this sample period and were blocked.
	 */
	public int[] requestsBlockedCnt = null;
	/**
	 * The duration of this sample interval.
	 */
	public long sampleIntervalLength = 0;

	public List<Map<String, User>> users;
	
	public DataSample() {
		users = new ArrayList<Map<String, User>>();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(String.format("%8.4f", sampleIntervalLength / 1000.0));
		for (int i = 0; i < scenarioNames.length; ++i) {
			builder.append(String.format("  %10s", scenarioNames[i]));
			builder.append(String.format("  %4d", requestsCnt[i]));
			builder.append(String.format("  %4d", requestsProcessedCnt[i]));
			builder.append(String.format("  %4d", requestsBlockedCnt[i]));
			builder.append(String.format("  %9.4f\n        ", responseTime[i]));
		}

		return builder.toString();
	}
}
