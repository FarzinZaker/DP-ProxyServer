package opera;

import opera.Core.LQM;
import opera.KalmanFilter.EstimationResults;
import opera.KalmanFilter.KalmanConfiguration;
import opera.KalmanFilter.KalmanEstimator;
import opera.KalmanFilter.ModelParameter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import opera.util.MeasuresUtil;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author marin
 */
public class OperaModel {
	private final String MODEL_THINK_TIME = "/Model/Workloads/ThinkTimes/ThinkTime/@time";
	private final String MODEL_THINK_TIME_SCENARIO = "/Model/Workloads/ThinkTimes/ThinkTime[@scenario='%s']/@time";
	private final String MODEL_POPULATION = "/Model/Workloads/Users/text()";
	private final String MODEL_POPULATION_SCENARIO = "/Model/Workloads/WorkloadMixes[%d]/Mix[@scenario='%s']/@load";
	private final String MODEL_SCENARIO_DEMAND_CPU = "/Model/Scenarios/Scenario[@name='%s']/Call[@callee='%s']/Demand/@CPUDemand";
	private final String MODEL_SCENARIO_DEMAND_DISK = "/Model/Scenarios/Scenario[@name='%s']/Call[@callee='%s']/Demand/@DiskDemand";
	private final String MODEL_CONTAINER_THREADS = "/Model/Topology/Cluster/Container[@name='%s']/@parallelism";
	private final String MODEL_NODE_CPU_MULTIPLICITY = "/Model/Topology/Node[@name='%s']/@CPUParallelism";
	private final String MODEL_NODE_DISK_MULTIPLICITY = "/Model/Topology/Node[@name='%s']/@DiskParallelism";
	private final String MODEL_NETWORK_DELAY = "/Model/Topology/Network[@name='%s']/@latency";
	private final String RESULTS_SERVICE_ALLOCATION = "/Results/Architecture/Service[@name='%s']/@allocatedToContainer";
	private final String RESULTS_RESPONSE_TIME_SCENARIO = "/Results/Architecture/Workloads[%d]/Scenario[@name='%s']/ResponseTime";
	private final String RESULTS_RESPONSE_TIME_CONTAINER = "/Results/Architecture/Workloads[%d]/Container[@name='%s']/Scenario[@scenarioName='%s']/@responseTime";
	private final String RESULTS_RESPONSE_TIME_SERVICE = "/Results/Architecture/Workloads[%d]/Service[@name='%s']/Scenario[@name='%s']/@responseTime";
	private final String RESULTS_UTILIZATION_CONTAINER = "/Results/Architecture/Workloads[%d]/Container[@name='%s']/@Utilization";
	private final String RESULTS_UTILIZATION_SERVICE = "/Results/Architecture/Workloads[%d]/Service[@name='%s']/@Utilization";
	private final String RESULTS_UTILIZATION_NODE = "/Results/Architecture/Workloads[%d]/Node[@name='%s']/%s/Utilization";
	private final String RESULTS_THROUGHPUT = "/Results/Architecture/Workloads[%d]/Scenario[@name='%s']/Throughput";
	private final double acceptedErr = 0.1;// response time and throughput error
	private final double accptedErrCPUUtil = 0.1; // cpu utilization is in [0, 1] scale.

	LQM model = null;
	Document doc = null;
	Document results = null;
	XPath m_xPath = XPathFactory.newInstance().newXPath();
	PrintWriter writerForDemand = null;
	PrintWriter writerForMeasAndEst = null;
	KalmanEstimator theEstimator = null;

	DecimalFormat formatter = new DecimalFormat("#0.000000");

	public OperaModel() throws FileNotFoundException, UnsupportedEncodingException {
		super();
		writerForDemand = new PrintWriter("./output/demands.csv", "UTF-8");
		writerForDemand.println("CPUDem_LB" + "," + "CPUDem_Web" + "," + "CPUDem_Analytic1" + "," + "CPUDem_Db" + ","
				+ "CPUDem_Analytic2");

		writerForMeasAndEst = new PrintWriter("./output/measuresAndEstimated.csv", "UTF-8");
		writerForMeasAndEst.println("User-Count" + "," + "Arrival-Rate" + "," + "MeasureWebUtil" + "," + "EstWebUtil"
				+ "," + "ErrWebUtil" + "," + "MeasAnalytic1Util" + "," + "EstAnalytic1Util" + "," + "ErrAna1Util" + ","
				+ "MeasDBUtil" + "," + "EstDBUtil" + "," + "ErrDBUtil" + "," + "EstAnalytic2Util" + "," + "MeasRT" + ","
				+ "EstRT" + "," + "ErrRespTime" + "," + "MeasThro" + "," + "EstThro" + "," + "ErrThro" + ","
				+ "No-Web-Cont" + "," + "No-Analytic-Cont" + "," + "No-DB-Cont");
	}

	public void setModel(String pxlFile) {
		model = new LQM();
		doc = model.parsePXL(pxlFile);
	}

	public void setModelFromString(String pxlModel) {
		model = new LQM();
		doc = model.parsePxlFromString(pxlModel);
	}

	/**
	 * Sets the total population. The total population is not related to any
	 * mix.
	 * <p>
	 * This method does not change the mixes in any way.
	 *
	 * @param value
	 *            the new value for the total population.
	 */
	public void SetPopulation(int value) {
		this.SetXPathModelNodesValue(MODEL_POPULATION, String.valueOf(value));
	}

	/**
	 * Sets the population for a scenario to the specified value. Only the first
	 * mix will be affected. Calls <code>SetPopulation(int, String, int)</code>
	 * passing 0 for the first parameter.
	 * <p>
	 * This method does not update the total population.
	 *
	 * @param scenarioName
	 *            the name of the scenario for which to change the population.
	 * @param value
	 *            the new population for the scenario.
	 */
	public void SetPopulation(String scenarioName, double value) {
		this.SetPopulation(0, scenarioName, value);
	}

	/**
	 * Sets the population for a scenario to the specified value.
	 * <p>
	 * This method does not update the total population.
	 *
	 * @param mixNo
	 *            the mix index that will be affected by the change (the indext
	 *            starts at 0).
	 * @param scenarioName
	 *            the name of the scenario for which to change the population.
	 * @param value
	 *            the new population for the scenario.
	 */
	public void SetPopulation(int mixNo, String scenarioName, double value) {
		String sXPathExpression = String.format(MODEL_POPULATION_SCENARIO, mixNo + 1, scenarioName);
		this.SetXPathModelNodesValue(sXPathExpression, String.valueOf(value));
	}

	/**
	 * Set the think time for all scenarios to the specified value.
	 *
	 * @param value
	 *            the new think time.
	 */
	public void SetThinkTime(double value) {
		this.SetXPathModelNodesValue(MODEL_THINK_TIME, String.valueOf(value));
	}

	/**
	 * The the think time for the first <code>values.length</code>, in the order
	 * they were defined.
	 * <p>
	 * If there are more values specified than scenarios defined, the extra
	 * values will be ignored.
	 *
	 * @param values
	 *            an array with the new think times.
	 */
	public void SetThinkTime(double[] values) {
		// get the defined scenarios in this model
		String[] scenarioNames = this.GetScenarioNames();

		// set values for this scenarios in the defined order
		for (int i = 0; i < values.length && i < scenarioNames.length; ++i) {
			this.SetThinkTime(scenarioNames[i], values[i]);
		}
	}

	/**
	 * Set the think time for a single scenario.
	 *
	 * @param scenarioName
	 *            the scenario for which the think time must be modified.
	 * @param value
	 *            the new think time for the scenario.
	 */
	public void SetThinkTime(String scenarioName, double value) {
		String sXPathExpression = String.format(MODEL_THINK_TIME_SCENARIO, scenarioName);
		this.SetXPathModelNodesValue(sXPathExpression, String.valueOf(value));
	}

	/**
	 * Set the think time for an array of scenario. This method will call
	 * <code>SetThinkTime(String, double)</code> for every scenario from the
	 * array.
	 * <p>
	 * If the array sizes don't match, the behaviour is undefined.
	 *
	 * @param scenarioNames
	 *            an array with scenario names for which to change the think
	 *            times.
	 * @param values
	 *            an array with the new think times. This array must have the
	 *            same size as <code>scenarioNames</code>.
	 */
	public void SetThinkTime(String[] scenarioNames, double[] values) {
		for (int i = 0; i < scenarioNames.length; ++i) {
			this.SetThinkTime(scenarioNames[i], values[i]);
		}
	}

	/**
	 * Set the CPU demand for a host when the specified service is executed, as
	 * part of a scenario.
	 * <p>
	 * A scenario is a sequence of service calls, that are executed on hosts.
	 * Each call requires CPU. The demand is the time necessary for the host CPU
	 * to execute a single call.
	 *
	 * @param scenarioName
	 * @param serviceName
	 * @param value
	 */
	public void SetCpuDemand(String scenarioName, String serviceName, double value) {
		String sXPathExpression = String.format(MODEL_SCENARIO_DEMAND_CPU, scenarioName, serviceName);
		this.SetXPathModelNodesValue(sXPathExpression, String.valueOf(value));
	}
/**
 * Set the Network delay for a network.
 * Added by Nasim.
 */
	public void SetNetworkDelay(String networkName, double value) {
		String sXPathExpression = String.format(MODEL_NETWORK_DELAY, networkName);
		this.SetXPathModelNodesValue(sXPathExpression, String.valueOf(value));
	}

	/**
	 * Set the DISK demand for a host when the specified service is executed, as
	 * part of a scenario.
	 * <p>
	 * A scenario is a sequence of service calls, that are executed on hosts.
	 * Each call requires DISK. The demand is the time necessary for the host
	 * DISK to execute a single call.
	 *
	 * @param scenarioName
	 * @param serviceName
	 * @param value
	 */
	public void SetDiskDemand(String scenarioName, String serviceName, double value) {
		String sXPathExpression = String.format(MODEL_SCENARIO_DEMAND_DISK, scenarioName, serviceName);
		this.SetXPathModelNodesValue(sXPathExpression, String.valueOf(value));
	}

	/**
	 * Sets the number of threads in a container.
	 *
	 * @param containerName
	 *            the container for which to change the number of threads.
	 * @param value
	 *            the new number of threads.
	 */
	public void SetContainerThreads(String containerName, int value) {
		String sXPathExpression = String.format(MODEL_CONTAINER_THREADS, containerName);
		this.SetXPathModelNodesValue(sXPathExpression, String.valueOf(value));
	}

	/**
	 * Sets the multiplicity of a node (i.e. the number of nodes of this type).
	 * This method assumes that a node has exactly one CPU and exactly one DISK,
	 * so changing multiplicity of the node means changing multiplicity of CPU
	 * and DISK for the node to the same value.
	 *
	 * @param nodeName
	 *            the node for which to change the multiplicity.
	 * @param value
	 *            the new number of nodes.
	 */
	public void SetNodeMultiplicity(String nodeName, int value) {
		String sXPathExpression = String.format(MODEL_NODE_CPU_MULTIPLICITY, nodeName);
		this.SetXPathModelNodesValue(sXPathExpression, String.valueOf(value));

		sXPathExpression = String.format(MODEL_NODE_DISK_MULTIPLICITY, nodeName);
		this.SetXPathModelNodesValue(sXPathExpression, String.valueOf(value));
	}

	/**
	 * Updates <b>all</b> elements, from the mdoel, that match the specified
	 * XPath expression. This is a generic method to change model values that
	 * don't have a dedicated function to do so.
	 *
	 * @param sXPathExpression
	 * @param sValue
	 */
	public void SetXPathModelNodesValue(String sXPathExpression, String sValue) {
		try {
			NodeList nodes = (NodeList) this.m_xPath.evaluate(sXPathExpression, this.doc, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); ++i) {
				nodes.item(i).setNodeValue(sValue);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Get the names of the scenarios defined in this model.
	 *
	 * @return An array containing the names, or <code>null</code> if an
	 *         exception occurred.
	 */
	public String[] GetScenarioNames() {
		String sXPathExpression = "/Model/Scenarios/Scenario/@name";
		return this.GetXPathArrayString(this.doc, sXPathExpression);
	}

	/**
	 * Get the names of the nodes defined in this model.
	 *
	 * @return An array containing the names, or <code>null</code> if an
	 *         exception occurred.
	 */
	public String[] GetNodeNames() {
		String sXPathExpression = "/Model/Topology/Node/@name";
		return this.GetXPathArrayString(this.doc, sXPathExpression);
	}

	/**
	 * Get the names of the services defined in this model.
	 *
	 * @return An array containing the names, or <code>null</code> if an
	 *         exception occurred.
	 */
	public String[] GetServiceNames() {
		String sXPathExpression = "/Model/Scenarios/Services/Service/@name";
		return this.GetXPathArrayString(this.doc, sXPathExpression);
	}

	/**
	 * Get the container to which the specified service is allocated. The model
	 * must be solved first.
	 *
	 * @param serviceName
	 *            The service that must be queried.
	 * @return The name of the container to which this service is allocated.
	 */
	public String GetServiceAllocation(String serviceName) {
		String sXPathExpression = String.format(RESULTS_SERVICE_ALLOCATION, serviceName);
		return this.GetXPathValueString(this.results, sXPathExpression);
	}

	/**
	 * Get the CPU demand when a specified service is executed, as part of a
	 * scenario.
	 *
	 * @param scenarioName
	 * @param serviceName
	 * @return
	 */
	public double GetCpuDemand(String scenarioName, String serviceName) {
		String sXPathExpression = String.format(MODEL_SCENARIO_DEMAND_CPU, scenarioName, serviceName);
		return this.GetXPathModelDouble(sXPathExpression);
	}

	/**
	 * Get the DISK demand when a specified service is executed, as part of a
	 * scenario.
	 *
	 * @param scenarioName
	 * @param serviceName
	 * @return
	 */
	public double GetDiskDemand(String scenarioName, String serviceName) {
		String sXPathExpression = String.format(MODEL_SCENARIO_DEMAND_DISK, scenarioName, serviceName);
		return this.GetXPathModelDouble(sXPathExpression);
	}

	/**
	 * Get the total population specified in the model. This value is not
	 * related in any way with the workload mixes.
	 *
	 * @return the total population specified in the model.
	 */
	public double GetPopulation() {
		return this.GetXPathModelDouble(MODEL_POPULATION);
	}

	/**
	 * Get the population for a single scenario for the first workload mix.
	 *
	 * @param scenarioName
	 *            the scenario for which to get the population.
	 * @return the population for the specified scenario.
	 */
	public double GetPopulation(String scenarioName) {
		return this.GetPopulation(0, scenarioName);
	}

	/**
	 * Get the population for a scenario in a workload mix.
	 *
	 * @param mixNo
	 *            the index of the workload mix.
	 * @param scenarioName
	 *            the scenario for which to get the population.
	 * @return the population for the specified scenario, in the specified
	 *         workload mix.
	 */
	public double GetPopulation(int mixNo, String scenarioName) {
		String sXPathExpression = String.format(MODEL_POPULATION_SCENARIO, mixNo + 1, scenarioName);
		return this.GetXPathModelDouble(sXPathExpression);
	}

	/**
	 * Get the number of CPUs for a node.
	 *
	 * @param nodeName
	 *            the node for which to get the number of CPUs.
	 * @return the number of CPUs for the specified node.
	 */
	public int GetCpuMultiplicity(String nodeName) {
		String sXPathExpression = String.format(MODEL_NODE_CPU_MULTIPLICITY, nodeName);
		return this.GetXPathModelInt(sXPathExpression);
	}

	/**
	 * Get the number of DISKs for a node.
	 *
	 * @param nodeName
	 *            the node for which to get the number of DISKs.
	 * @return the number of DISKs for the specified node.
	 */
	public int GetDiskMultiplicity(String nodeName) {
		String sXPathExpression = String.format(MODEL_NODE_DISK_MULTIPLICITY, nodeName);
		return this.GetXPathModelInt(sXPathExpression);
	}

	/**
	 * Set to 0 the number of users for each scenario in the mix.
	 */
	public void ResetPerClassPopulation() {
		String sXPathExpression = "OperaModel/Workloads/WorkloadMixes/Mix/@load";
		try {
			NodeList attrLoads = (NodeList) this.m_xPath.evaluate(sXPathExpression, this.doc, XPathConstants.NODESET);
			for (int i = 0; i < attrLoads.getLength(); ++i) {
				attrLoads.item(i).setNodeValue("0");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the response time for scenario <code>scenarioName</code> for the
	 * first workload mix, as specified in the model.
	 *
	 * @param scenarioName
	 *            - The scenario for which to get the response time;
	 * @return A number which represent the response time as computed by the
	 *         model.
	 */
	public double GetResponseTimeScenario(String scenarioName) {
		return this.GetResponseTimeScenario(0, scenarioName);
	}

	/**
	 * Get the response time for scenario <code>scenarioName</code> for the
	 * workload mix <code>mixNo</code>, as specified in the model.
	 *
	 * @param mixNo
	 *            - The index of the workload mix (starts at 0).
	 * @param scenarioName
	 *            - The name of the scenario.
	 * @return A number which represent the response time as computed by the
	 *         model.
	 */
	public double GetResponseTimeScenario(int mixNo, String scenarioName) {
		String sXPathExpression = String.format(RESULTS_RESPONSE_TIME_SCENARIO, mixNo + 1, scenarioName);
		return this.GetXPathResultsDouble(sXPathExpression);
	}

	/**
	 * Get the response time for multiple scenarios, for the first workload mix,
	 * as specified in the model.
	 *
	 * @param scenarioNames
	 *            - An array with scenario names for which to get the response
	 *            time.
	 * @return An array with response times. The element at index
	 *         <code>idx</code> in this array corresponds to the scenario
	 *         <code>idx</code> from the array <code>scenarioNames</code>.
	 */
	public double[] GetResponseTimeScenario(String[] scenarioNames) {
		return this.GetResponseTimeScenario(0, scenarioNames);
	}

	/**
	 * Get the response time for multiple scenarios.
	 *
	 * @param mixNo
	 *            the workload mix for which to get the response time.
	 * @param scenarioNames
	 *            an array with scenario names for which to get the response
	 *            time.
	 * @return An array with response times. The element at index
	 *         <code>idx</code> in this array corresponds to the scenario
	 *         <code>idx</code> from the array <code>scenarioNames</code>.
	 */
	public double[] GetResponseTimeScenario(int mixNo, String[] scenarioNames) {
		double[] responseTimes = new double[scenarioNames.length];
		for (int i = 0; i < scenarioNames.length; ++i) {
			responseTimes[i] = this.GetResponseTimeScenario(mixNo, scenarioNames[i]);
		}
		return responseTimes;
	}

	public double GetResponseTimeContainer(String containerName, String scenarioName) {
		return this.GetResponseTimeContainer(0, containerName, scenarioName);
	}

	public double GetResponseTimeContainer(int mixNo, String containerName, String scenarioName) {
		String sXPathExpression = String.format(RESULTS_RESPONSE_TIME_CONTAINER, mixNo + 1, containerName,
				scenarioName);
		return this.GetXPathResultsDouble(sXPathExpression);
	}

	public double GetResponseTimeService(String serviceName, String scenarioName) {
		return this.GetResponseTimeService(0, serviceName, scenarioName);
	}

	public double GetResponseTimeService(int mixNo, String serviceName, String scenarioName) {
		String sXPathExpression = String.format(RESULTS_RESPONSE_TIME_SERVICE, mixNo + 1, serviceName, scenarioName);
		return this.GetXPathResultsDouble(sXPathExpression);
	}

	public double GetUtilizationContainer(String containerName) {
		return this.GetUtilizationContainer(0, containerName);
	}

	public double GetUtilizationContainer(int mixNo, String containerName) {
		// String sXPathExpression =
		// "/Results/Architecture/Workloads/Container[@name=\"" + containerName
		// + "\"]/@Utilization";
		String sXPathExpression = String.format(RESULTS_UTILIZATION_CONTAINER, mixNo + 1, containerName);
		return this.GetXPathResultsDouble(sXPathExpression);
	}

	public double GetUtilizationService(String serviceName) {
		return this.GetUtilizationService(0, serviceName);
	}

	public double GetUtilizationService(int mixNo, String serviceName) {
		// String sXPathExpression =
		// "/Results/Architecture/Workloads/Service[@name=\"" + serviceName +
		// "\"]/@Utilization";
		String sXPathExpression = String.format(RESULTS_UTILIZATION_SERVICE, mixNo + 1, serviceName);
		return this.GetXPathResultsDouble(sXPathExpression);
	}

	public double GetUtilizationNode(String nodeName, String deviceName) {
		return this.GetUtilizationNode(0, nodeName, deviceName);
	}

	public double GetUtilizationNode(int mixNo, String nodeName, String deviceName) {
		String sXPathExpression = String.format(RESULTS_UTILIZATION_NODE, mixNo + 1, nodeName, deviceName);
		return this.GetXPathResultsDouble(sXPathExpression);
	}

	public double GetThroughput(String scenarioName) {
		return this.GetThroughput(0, scenarioName);
	}

	public double[] GetThroughput(String[] scenarioNames) {
		double[] throughput = new double[scenarioNames.length];
		for (int i = 0; i < scenarioNames.length; ++i) {
			throughput[i] = this.GetThroughput(scenarioNames[i]);
		}
		return throughput;
	}

	public double GetThroughput(int mixNo, String scenarioName) {
		// "mixNo" starts from 0, but indices in XPath start with 1.
		// String sXPathExpression = "/Results/Architecture/Workloads[" + (mixNo
		// + 1) + "]/Scenario[@name=\"" + scenarioName +
		// "\"]/Throughput/text()";
		String sXPathExpression = String.format(RESULTS_THROUGHPUT, mixNo + 1, scenarioName);
		return this.GetXPathResultsDouble(sXPathExpression);
	}

	public double GetThinkTime(String strScenario) {
		String sXPathExpression = String.format(MODEL_THINK_TIME_SCENARIO, strScenario);
		return this.GetXPathModelDouble(sXPathExpression);
	}

	/**
	 * Get the value identified by the XPath expression as a <code>double</code>
	 * . The value is searched in the model document.
	 *
	 * @param sXPathExpression
	 *            <p>
	 *            an XPath expression identifying the desired value in the PXL
	 *            document.
	 *            </p>
	 *            <p>
	 *            This expression must start with <code>/Model/</code>.
	 *            </p>
	 * @return
	 *         <ul>
	 *         <li>if <code>sXPathExpression</code> refers to a numeric value,
	 *         will return that value as a <code>double</code>.</li>
	 *         <p>
	 *         <li><code>Double.NaN</code> otherwise.</li>
	 *         </ul>
	 */
	public double GetXPathModelDouble(String sXPathExpression) {
		Number value = this.GetXPathValueNumber(this.doc, sXPathExpression);
		return value.doubleValue();
	}

	/**
	 * <p>
	 * Get the value identified by the XPath expression as a <code>double</code>
	 * . The value is searched in the results document.
	 * <p>
	 * <p>
	 * <p>
	 * This method should be called only after calling the <code>solve()</code>
	 * method and will return the value produced by the last call to
	 * <code>solve()</code>.
	 * <p>
	 *
	 * @param sXPathExpression
	 *            <p>
	 *            an XPath expression identifying the desired value in the
	 *            results document.
	 *            </p>
	 *            <p>
	 *            This expression must start with <code>/Results/</code>.
	 *            </p>
	 * @return
	 *         <ul>
	 *         <li>if <code>sXPathExpression</code> refers to a numeric value,
	 *         will return that value as a <code>double</code>.</li>
	 *         <p>
	 *         <li><code>Double.NaN</code> otherwise.</li>
	 *         </ul>
	 */
	public double GetXPathResultsDouble(String sXPathExpression) {
		Number value = this.GetXPathValueNumber(this.results, sXPathExpression);
		return value.doubleValue();
	}

	/**
	 * Get the value identified by the XPath expression as a <code>double</code>
	 * . The XPath expression can refer to the model document (and starts with
	 * <code>/Model/</code>) or the results document (and starts with
	 * <code>/Results/</code>). If the XPath starts with anything else, this
	 * method return <code>Double.NaN</code>.
	 *
	 * @param sXPathExpression
	 *            <p>
	 *            an XPath expression identifying the desired value in the PXL
	 *            or results document.
	 *            </p>
	 * @return
	 *         <ul>
	 *         <li>if <code>sXPathExpression</code> refers to a numeric value,
	 *         will return that value as a <code>double</code>.</li>
	 *         <p>
	 *         <li><code>Double.NaN</code> otherwise.</li>
	 *         </ul>
	 */
	public double GetXPathDouble(String sXPathExpression) {
		if (sXPathExpression.startsWith("/Model/")) {
			return this.GetXPathModelDouble(sXPathExpression);
		} else if (sXPathExpression.startsWith("/Results/")) {
			return this.GetXPathResultsDouble(sXPathExpression);
		}

		return Double.NaN;
	}

	public int GetXPathModelInt(String sXPathExpression) {
		Number value = this.GetXPathValueNumber(this.doc, sXPathExpression);
		return value.intValue();
	}

	public int GetXPathResultsInt(String sXPathExpression) {
		Number value = this.GetXPathValueNumber(this.results, sXPathExpression);
		return value.intValue();
	}

	private Number GetXPathValueNumber(Node node, String sXPathExpression) {
		try {
			// We get the value as a string, because if the number is in
			// scientific notation
			// getting it as a NUMBER will fail. Also this method is slightly
			// faster than getting
			// the value as a number.
			String sVal = (String) this.m_xPath.evaluate(sXPathExpression, node, XPathConstants.STRING);
			return (Number) Double.parseDouble(sVal);
		} catch (Exception e) {
		}
		return Double.NaN;
	}

	private String GetXPathValueString(Node node, String sXPathExpression) {
		try {
			return (String) this.m_xPath.evaluate(sXPathExpression, node, XPathConstants.STRING);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Select a set of nodes that match the specified XPath expression, and get
	 * their values as strings.
	 *
	 * @param sXPathExpression
	 *            The expression used to select the nodes.
	 * @return The values of the nodes that matched the specified XPath
	 *         expression. If an exception occurred, the function returns
	 *         <code>null<code>.
	 */
	private String[] GetXPathArrayString(Node node, String sXPathExpression) {
		String[] values = null;
		try {
			NodeList lstNodes = (NodeList) this.m_xPath.evaluate(sXPathExpression, node, XPathConstants.NODESET);
			values = new String[lstNodes.getLength()];

			for (int i = 0; i < lstNodes.getLength(); ++i) {
				values[i] = lstNodes.item(i).getNodeValue();
			}
		} catch (Exception e) {
		}

		return values;
	}

	public void solve() {
		model.readAndValidatePxl(doc);
		model.initialize();

		// open the output file
		model.setOutDoc(new org.apache.xerces.dom.DocumentImpl());
		model.findConfigurations();
		results = model.getResultsDocument();
	}

	public ArrayList<double[]> getWorstCaseWorkloadMixex() {
		return model.getWorstWorkloadMixes();
	}

	private void SaveDocument(java.io.Writer writer, Document doc) {
		// Set up the output transformer
		try {
			Transformer xmlTransformer = TransformerFactory.newInstance().newTransformer();
			xmlTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
			xmlTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			xmlTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(writer);
			javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(doc);
			xmlTransformer.transform(source, result);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void SaveModel(java.io.Writer writer) {
		this.SaveDocument(writer, doc);
	}

	public void SaveResults(java.io.Writer writer) {
		this.SaveDocument(writer, results);
	}

	public void SaveModelToXmlFile(String sFileName) {
		try {
			FileWriter outputFile = new FileWriter(sFileName);
			this.SaveDocument(outputFile, doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void SaveResultsToXmlFile(String sFileName) {
		try {
			FileWriter outputFile = new FileWriter(sFileName);
			this.SaveDocument(outputFile, results);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeDemandsToFile(ModelParameter[] modelParameter) {
		StringBuilder line = new StringBuilder();
		if (modelParameter == null)
			return;
		for (ModelParameter mp : modelParameter) {
			line.append(String.valueOf(formatter.format(mp.getValue())) + ",");
		}
		writerForDemand.println(line);
	}

	/*
	 * Here we insert the measured and estimated cpu utilization in persentage
	 * %. For this reason I mulitply the values by 100 * cpu qutua The cpu qutua
	 * is from measured Cornel file.
	 */
	public void writeMeasAndEstToFile(double[] measures) {
		StringBuilder line = new StringBuilder();

		// note that I use different fomula to calculate the error rate for CPU
		// utils compared to response time.
		// for cpu util I use reletive error while for response and throughput I
		// use absolute error formula.
		line.append((String.valueOf(formatter.format(measures[0]) + "," + formatter.format(measures[1]) + "," +

				formatter.format(measures[2] * 400)) + ","
				+ formatter.format(this.GetUtilizationNode("WebHost", "CPU") * 400) + ","
				+ formatter.format(Math.abs(measures[2] - this.GetUtilizationNode("WebHost", "CPU")) * 4) + ","
				+ formatter.format(measures[3] * 800) + ","
				+ formatter.format(this.GetUtilizationNode("ProxyHost", "CPU") * 800) + ","
				+ formatter.format(Math.abs(measures[3] - this.GetUtilizationNode("ProxyHost", "CPU")) * 8) + ","
				+ formatter.format(measures[4] * 400) + ","
				+ formatter.format(this.GetUtilizationNode("DataHost", "CPU") * 400) + ","
				+ formatter.format(Math.abs(measures[4] - this.GetUtilizationNode("DataHost", "CPU")) * 4) + ","
				+ formatter.format(this.GetUtilizationNode("AnalyticHost2", "CPU") * 100) + ","
				+ formatter.format(measures[5]) + "," + formatter.format(this.GetResponseTimeScenario("select 0")) + ","
				+ formatter.format(Math.abs(measures[5] - this.GetResponseTimeScenario("select 0")) / measures[5]) + ","
				+ formatter.format(measures[6]) + "," + formatter.format(this.GetThroughput("select 0")) + ","
				+ formatter.format(Math.abs(measures[6] - this.GetThroughput("select 0")) / measures[6])));
		// + "," +
		// formatter.format(measures[7]) + "," + formatter.format(measures[8]) +
		// "," + formatter.format
		// (measures[9])));

		writerForMeasAndEst.println(line);
	}

	public boolean clibrationIsNeeded(double[] measuredMetrics) {
		// double measuredProxyCPUUtil = measuredMetrics[0];
		double measuredLBCPUUtil = measuredMetrics[0];
		double measuredWebCPUUtil = measuredMetrics[1];
		double measuredDBCPUUtil = measuredMetrics[2];
		double measuredRTS0 = measuredMetrics[3];
		double measuredThroS0 = measuredMetrics[4];
		double measuredRTS1 = measuredMetrics[5];
		double measuredThroS1 = measuredMetrics[6];

		double estimatedRTS0 = this.GetResponseTimeScenario("select 0");
		double estimatedThroS0 = this.GetThroughput("select 0");
		double estimatedRTS1 = this.GetResponseTimeScenario("select 1");
		double estimatedThroS1 = this.GetThroughput("select 1");

		double estimatedWebCPU = this.GetUtilizationNode("WebHost", "CPU");
		double estimatedLBCPU = this.GetUtilizationNode("LBHost", "CPU");
		double estimatedDBCPU = this.GetUtilizationNode("DataHost", "CPU");

		double respTimeErrS0 = Math.abs(measuredRTS0 - estimatedRTS0) / measuredRTS0;
		double throughputErrS0 = Math.abs(measuredThroS0 - estimatedThroS0) / measuredThroS0;
		double respTimeErrS1 = Math.abs(measuredRTS1 - estimatedRTS1) / measuredRTS1;
		double throughputErrS1 = Math.abs(measuredThroS1 - estimatedThroS1) / measuredThroS1;

		// here I use only difference between estimated and measured value and
		// don't divide by actual value.
		// This formula makes more sense for cpu utilization
		double cpuUtilWebErr = Math.abs(measuredWebCPUUtil - estimatedWebCPU);
		double cpuUtilLBErr = Math.abs(measuredLBCPUUtil - estimatedLBCPU);
		double cpuUtilDBErr = Math.abs(measuredDBCPUUtil - estimatedDBCPU);

		if (respTimeErrS0 >= acceptedErr || throughputErrS0 >= acceptedErr || respTimeErrS1 >= acceptedErr
				|| throughputErrS1 >= acceptedErr || cpuUtilLBErr >= accptedErrCPUUtil
				|| cpuUtilWebErr >= accptedErrCPUUtil || cpuUtilDBErr >= accptedErrCPUUtil) {
			return true;
		}
		return false;
	}

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

		OperaModel operaModel = new OperaModel();
		operaModel.setModel("./input/BW-Simple DB Operations.model.pxl");
		EstimationResults results = null;

		KalmanConfiguration kalmanConfig = new KalmanConfiguration();
		kalmanConfig.withConfigFile("./input/BW-Simple DB Operations.kalman.config").withModel(operaModel)
				.withSetting(KalmanConfiguration.ITERATIONS_MAX, "20");

		operaModel.theEstimator = new KalmanEstimator(kalmanConfig);
		MeasuresUtil rm = new MeasuresUtil();
		rm.parseFile("/Users/Nasim/Workspace/AdaptiveControlWithBW/output/With-BW-Change-cloud-controller.hogna.sdo.metrics.data", 1, 35);

		HashMap metrics = rm.getMetrics();

		ArrayList<Double> cpuLBUtil = (ArrayList) metrics.get("cpuLBUtil");
		ArrayList<Double> cpuWebUtil = (ArrayList) metrics.get("cpuWebUtil");
		ArrayList<Double> cpuProxyUtil = (ArrayList) metrics.get("cpuProxyUtil");
		ArrayList<Double> cpuDBUtil = (ArrayList) metrics.get("cpuDBUtil");
		ArrayList<Double> respTimeS0 = (ArrayList) metrics.get("respTimeS0");
		ArrayList<Double> throughputS0 = (ArrayList) metrics.get("throughputS0");
		ArrayList<Double> userCountS0 = (ArrayList) metrics.get("userCountS0");
		ArrayList<Double> vmNoWeb = (ArrayList) metrics.get("VMNoWeb");
		ArrayList<Double> bwS0 = (ArrayList) metrics.get("bwS0");

		ArrayList<Double> respTimeS1 = (ArrayList) metrics.get("respTimeS1");
		ArrayList<Double> throughputS1 = (ArrayList) metrics.get("throughputS1");
		ArrayList<Double> userCountS1 = (ArrayList) metrics.get("userCountS1");
		ArrayList<Double> bwS1 = (ArrayList) metrics.get("bwS1");

		int noOfSenarios = 2; // we have two scenarios, select 0 and select 1
		int noOfCalibaration = 0;

		// remove this loop if you don't want calibration
		for (int i = 0; i < rm.getNoOfMeasurs(); i++) {
			// put the userCount here (arrival rate: req/s); should contain
			// userCount for each scenario
			Double wlS0 = (Double) userCountS0.get(i);
			Double wlS1 = (Double) userCountS1.get(i);

			// put the values here, keep the order from the kalman config files
			// the values are: CPU utilization web, CPU Analytic, CPU
			// utilization db, response times for
			// each scenario, throughput for each scenario
			Double responseTimeS0 = (Double) respTimeS0.get(i);
			Double responseTimeS1 = (Double) respTimeS1.get(i);

			int noOfWebVMs = ((Double) vmNoWeb.get(i)).intValue();
			// send these measurements to Kalman
			// double[] measuredMetrics = {(Double) cpuProxyUtil.get(i),
			// (Double) cpuLBUtil.get(i), (Double) cpuWebUtil.get(i),
			// (Double) cpuDBUtil.get(i), responseTime, (Double)
			// throughput.get(i)};
			// here we removed proxy host due to difference between model cpu
			// and measurement
			double[] measuredMetrics = { /* (Double) cpuProxyUtil.get(i), */ (Double) cpuLBUtil.get(i),
					(Double) cpuWebUtil.get(i), (Double) cpuDBUtil.get(i), responseTimeS0, (Double) throughputS0.get(i),
					responseTimeS1, (Double) throughputS1.get(i) };
			// set userCount and no of containers in the model
			// based on formula: wlS0 = arrivals *(thinkTime + responseTime)
			operaModel.SetPopulation("select 0", wlS0);
			operaModel.SetPopulation("select 1", wlS1);
			operaModel.SetNodeMultiplicity("WebHost", noOfWebVMs);
			
//			Double demandS0 = rm.calDelayDemand((Double) bwS0.get(i), "select 0", wlS0);
//			Double demandS1 = rm.calDelayDemand((Double) bwS1.get(i), "select 1", wlS1);
//			operaModel.SetCpuDemand("select0", "DelayCentre0", demandS0);
//			operaModel.SetCpuDemand("select1", "DelayCentre1", demandS1);

			operaModel.solve();

			if (operaModel.clibrationIsNeeded(measuredMetrics)) {
				long s = System.currentTimeMillis();
				results = operaModel.theEstimator.EstimateModelParameters(measuredMetrics);
				results.getModelParametersFinal();				
				System.out.println(results.toString());
				long e = System.currentTimeMillis();
				System.out.println("Calibration time: " + String.valueOf(e - s));
				noOfCalibaration++;
			}

			// double[] measMetricsToSave = {wl, wl,
			// (Double) cpuWebUtil.get(i), (Double) cpuProxyUtil.get(i),
			// (Double) cpuDBUtil.get(i),
			// responseTime, (Double) throughput.get(i), noOfWebVMs, 1};

			// operaModel.writeMeasAndEstToFile(measMetricsToSave);
			// operaModel.writeDemandsToFile(mp);
		}

		double calibPercentage = (double) rm.getNoOfMeasurs() / noOfCalibaration;
		// System.out.println("No of calibration: " + noOfCalibaration);
		// System.out.println("Calibration Interval (minute): " +
		// calibPercentage);
		// operModel.writerForDemand.close();
		// operModel.writerForMeasAndEst.close();
		operaModel.SaveModelToXmlFile("./output/FinalSimpleDBOpModel.pxl");
	}

}
