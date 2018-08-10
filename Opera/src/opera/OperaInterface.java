package opera;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import opera.KalmanFilter.EstimationResults;
import opera.KalmanFilter.KalmanConfiguration;
import opera.KalmanFilter.KalmanEstimator;
import opera.util.MeasuresUtil;
import opera.util.MetricCollection;
import opera.util.SshClient;

/**
 * Created by Nasim on 2016-08-18.
 */
public class OperaInterface {

	public static OperaModel operaModel = null;
	private String inputModel;
	private String kalmanConfigFile;
	private String kalmanIterationCount;
	private String finalModel;
	private String clientIP="54.210.58.220";
	MeasuresUtil mu = new MeasuresUtil();
	private double bwStep = 0.07; // we manipulate the bw with this step
	private double acceptedErrRT = 10; // this is the accepted err when
										// targeting a desired response
										// time. this error has to be adaptive,
										// sometimes it does not work for higher
										// desired bw, e.g., 120
	private static double minBW = 0.1; // min bw (kilo bit per milli second)
										// that we don't want to go lower
	private double maxBW = 15; // max bw (kbpms) that we don't want to
								// go over, because not going to make
								// any change
	private static double absMaxBW = 360; // absolute max bw that we don't want
											// to go over (kbpms)
	private static HashMap<String, Double> scenarioThinkTime= new HashMap<String, Double> ();{	
	scenarioThinkTime.put("select 0", 500.0);
	scenarioThinkTime.put("select 1", 500.0);
	}

	public OperaInterface(String inpModel, String kalmanConfFile, String kalmanIterationCnt, String fModel) {
		inputModel = inpModel;
		kalmanConfigFile = kalmanConfFile;
		kalmanIterationCount = kalmanIterationCnt;
		finalModel = fModel;

		try {
			operaModel = new OperaModel();
			operaModel.setModel(inputModel);

			KalmanConfiguration kalmanConfig = new KalmanConfiguration();
			kalmanConfig.withConfigFile(kalmanConfigFile).withModel(operaModel)
					.withSetting(KalmanConfiguration.ITERATIONS_MAX, kalmanIterationCount);

			operaModel.theEstimator = new KalmanEstimator(kalmanConfig);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * This method can be used to train the model based on previous measurements
	 * in off line mode
	 *
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */

	
	
	public void setDelayOnClient(String clientIP,double delay,String scenario) {
		String configScript = "";
		String fileName=scenario.replace(" ", "");
		configScript += "echo "+delay +" >> "+fileName+"-delay.txt;";
		configScript += "exit 0;\n";
		System.out.println("Setting "+delay+" second  delay on "+ scenario);
		int exitCode = SshClient.ExecuteCommand(clientIP, configScript);

	}
	
	
	
	
	/**
	 * This method returns the new number of web and analytic containers to
	 * bring the cpu utilization to the desired range.
	 *
	 * @param smplMetrics
	 * @param webCPULowUtil
	 * @param webCPUUPUtil
	 * @param analyticCPULowUtil
	 * @param analyticCPUUPUtil
	 * @return the new topology that brings the system back to the desired area.
	 */

	/*
	 * This method returns the target bw that will set the response time to the
	 * desired one. Here we have constant increase/decrease of bandwidth. We can
	 * use adaptive step; something like TCP window size algorithm
	 */


	public double[] getTargetBW(Double candidateCurrentBW, Double inNeedCurrentBW, String candidateScenario,
			String scenarioInNeed, double candidateWl, double inNeedWl, double currentRTCandidate,
			double desiredRTCandidate, double currentRTInNeed, double desiredRTInNeed, String candidateDelayCenter,
			String inNeedDelayCenter, HashMap<String, Double> bwRate, MetricCollection metrics) {
		double[] modelOutput = { 0, 0, 0 };
		double targetDemand, candidateModelRT = 0, previousCandidateModelRT=0, candidateThinkTime=0,targetThinkTime;
		double inNeedModelRT = currentRTInNeed;
		double tempBW = candidateCurrentBW;
		double targetNetDelay=0;
		double candidateTT=scenarioThinkTime.get(candidateScenario);
		operaModel.SetThinkTime(candidateScenario, candidateTT);
		double inNeedTT=scenarioThinkTime.get(scenarioInNeed);
		operaModel.SetThinkTime(scenarioInNeed, inNeedTT);
		updateModel(metrics, bwRate);
//		double candidateDSDemand = operaModel.GetCpuDemand(candidateScenario, candidateDelayCenter);
//		targetDemand = candidateDSDemand;
		
		candidateThinkTime=operaModel.GetThinkTime(candidateScenario);
		targetThinkTime=candidateThinkTime;
		if (currentRTCandidate < desiredRTCandidate) {// we need to reduce bw of
														// candidate
			// modelOutput = findDesiredBW(candidateCurrentBW, inNeedCurrentBW,
			// candidateScenario, scenarioInNeed,
			// candidateWl, inNeedWl, currentRTCandidate, desiredRTCandidate,
			// currentRTInNeed, desiredRTInNeed,
			// candidateDelayCenter, inNeedDelayCenter);
			//double delayStep=(desiredRTCandidate-currentRTCandidate)/10;
			//int counter=1;
			while ((desiredRTCandidate - candidateModelRT) > acceptedErrRT && (desiredRTInNeed - inNeedModelRT) < acceptedErrRT) {
//					targetDemand = targetDemand + (0.05 * candidateDSDemand);
//					operaModel.SetCpuDemand(candidateScenario, candidateDelayCenter, targetDemand);
					//targetNetDelay=counter*delayStep;
//					operaModel.SetNetworkDelay("client1LAN", targetNetDelay);
//					counter++;
					targetThinkTime=targetThinkTime + (0.05 * candidateThinkTime);
					operaModel.SetThinkTime(candidateScenario, targetThinkTime);
					operaModel.solve();
//					previousCandidateModelRT=candidateModelRT;
					candidateModelRT = operaModel.GetResponseTimeScenario(candidateScenario);
					inNeedModelRT = operaModel.GetResponseTimeScenario(scenarioInNeed);					
			}
			scenarioThinkTime.put(candidateScenario, targetThinkTime);
			//double caniddateDelayCenterRT=operaModel.GetResponseTimeContainer("DelayContainer1", "select 1");
			setDelayOnClient(clientIP,(targetThinkTime-500)/1000,candidateScenario);
			//tempBW = mu.calBW(caniddateDelayCenterRT, candidateScenario, candidateWl);
			System.out.println("modelRT=" + candidateModelRT);
			modelOutput[0] = tempBW;
			modelOutput[1] = candidateModelRT;
			modelOutput[2] = inNeedModelRT;

			return modelOutput;
		}
		// if its response time SLA of candidate is violated (we want to roll
		// back)
		else {
			while ((desiredRTCandidate - candidateModelRT) < 0 && tempBW <= maxBW) {

				// if (tempBW == absMaxBW){ // this is the absolute max bw and
				// we
				// //modelOutput[0]=tempBW;
				// //modelOutput[1]=modelRT; // cannot go over it.
				// return null;// this is the maximum link capacity
				// }
				if (candidateCurrentBW == maxBW) { // this is the max bw and
					// increasing after this value
					// does not make any difference,
					// so we set to the absolute
					// value
					candidateCurrentBW = absMaxBW;
					System.out.println("modelRT=" + candidateModelRT);
					modelOutput[0] = absMaxBW;
					modelOutput[1] = candidateModelRT;
					return modelOutput;
				} else {
					tempBW = tempBW + bwStep;
					targetDemand = mu.calDelayDemand(tempBW, candidateScenario, candidateWl);
					operaModel.SetCpuDemand(candidateScenario, candidateDelayCenter, targetDemand);
					operaModel.solve();
					candidateModelRT = operaModel.GetResponseTimeScenario(candidateScenario);
					inNeedModelRT = operaModel.GetResponseTimeScenario(scenarioInNeed);

				}
			}
			System.out.println("candidateModelRT=" + candidateModelRT);
			System.out.println("inNeedModelRT=" + inNeedModelRT);

			modelOutput[0] = tempBW;
			modelOutput[1] = candidateModelRT;
			modelOutput[2] = inNeedModelRT;
			return modelOutput;
		}
	}

	public double[] findDesiredBW(Double candidateCurrentBW, Double inNeedCurrentBW, String candidateScenario,
			String scenarioInNeed, double candidateWl, double inNeedWl, double currentRTCandidate,
			double desiredRTCandidate, double currentRTInNeed, double desiredRTInNeed, String candidateDelayCenter,
			String inNeedDelayCenter) {
		double[] modelOutput = { -1, -1, -1 };
		double targetDemand, candidateModelRT, inNeedModelRT;
		double low = minBW * 1000; // to make it in kilobits
		double high = maxBW * 1000; // to make it in kilobits

		while (high >= low) {
			double middle = (low + high) / 2;

			targetDemand = mu.calDelayDemand(middle, candidateScenario, candidateWl);
			operaModel.SetCpuDemand(candidateScenario, candidateDelayCenter, targetDemand);

			targetDemand = mu.calDelayDemand(inNeedCurrentBW, scenarioInNeed, inNeedWl);
			operaModel.SetCpuDemand(scenarioInNeed, inNeedDelayCenter, targetDemand);

			operaModel.solve();
			candidateModelRT = operaModel.GetResponseTimeScenario(candidateScenario);
			inNeedModelRT = operaModel.GetResponseTimeScenario(scenarioInNeed);

			if ((desiredRTCandidate - candidateModelRT) > acceptedErrRT && (desiredRTInNeed - inNeedModelRT) > 0) {
				modelOutput[0] = middle / 1000;
				modelOutput[1] = candidateModelRT;
				modelOutput[2] = inNeedModelRT;
				return modelOutput;
			}
			if ((desiredRTCandidate - candidateModelRT) < acceptedErrRT && (desiredRTInNeed - inNeedModelRT) > 0) {
				low = middle + 1;
			} else if ((desiredRTCandidate - candidateModelRT) > acceptedErrRT
					&& (desiredRTInNeed - inNeedModelRT) < 0) {
				high = middle - 1;
			} else if ((desiredRTCandidate - candidateModelRT) < acceptedErrRT
					&& (desiredRTInNeed - inNeedModelRT) < 0) {
				return modelOutput; // if both are violated, deadlock, we cannot
									// decrease the bw of candidate anymore
			}
		}
		return modelOutput;
	}

	public void updateModel(MetricCollection metrics, HashMap<String, Double> bwRate) {
		Double cpuLBUtil = metrics.Get("cw.cpu-utilization", "cluster-web-lb/.*");
		Double cpuWebUtil = metrics.GetAverage("cw.cpu-utilization", "cluster-web/.*");
		Double cpuDBUtil = metrics.GetAverage("cw.cpu-utilization", "cluster-database/.*");
		Double responseTimeS0 = metrics.Get("user-response-time", "cluster-web/proxy/select 0/192.180.253.1");
		Double throughputS0 = metrics.Get("user-throughput", "cluster-web/proxy/select 0/192.180.253.1");
		Double responseTimeS1 = metrics.Get("user-response-time", "cluster-web/proxy/select 1/192.180.253.2");
		Double throughputS1 = metrics.Get("user-throughput", "cluster-web/proxy/select 1/192.180.253.2");
		Double select0NoUsers = metrics.Get("user-count-ip", "cluster-web/proxy/select 0/192.180.253.1");
		Double select1NoUsers = metrics.Get("user-count-ip", "cluster-web/proxy/select 1/192.180.253.2");
		int noWebVMs = (int) metrics.Get("server-count", "cluster-web");

		operaModel.SetPopulation("select 0", select0NoUsers);
		operaModel.SetPopulation("select 1", select1NoUsers);
		operaModel.SetNodeMultiplicity("WebHost", noWebVMs);
		double[] measuredMetrics = { cpuLBUtil, cpuWebUtil, cpuDBUtil, responseTimeS0, throughputS0 / 1000,
				responseTimeS1, throughputS1 / 1000 };
//		Double demandS0 = mu.calDelayDemand(bwRate.get("192.180.253.1"), "select 0", select0NoUsers);
//		Double demandS1 = mu.calDelayDemand(bwRate.get("192.180.253.2"), "select 1", select1NoUsers);
//		operaModel.SetCpuDemand("select0", "DelayCentre0", demandS0);
//		operaModel.SetCpuDemand("select1", "DelayCentre1", demandS1);
		operaModel.solve();
		if (operaModel.clibrationIsNeeded(measuredMetrics)) {
			EstimationResults res = operaModel.theEstimator.EstimateModelParameters(measuredMetrics);
			System.out.println(res);
		}
	}

	/**
	 * This method calibrate the model with the latest measured metrics from
	 * run-time system
	 *
	 * @param smplMetrics
	 *            is the new metrics that we use to calibrate model
	 */
	public void calibrateModel(MetricCollection smplMetrics) {
		Double cpuLBUtil = smplMetrics.Get("docker.cpu-utilization", "legis/legis.load-balancer") / 100;
		Double cpuWebUtil = smplMetrics.GetAverage("docker.cpu-utilization", "legis/legis.web-worker.*") / 100;
		Double cpuAnalyticUtil = smplMetrics.GetAverage("docker.cpu-utilization", "legis/legis.spark-worker.*") / 100;
		Double cpuDBUtil = smplMetrics.GetAverage("docker.cpu-utilization", "legis/cassandra.*") / 100;
		Double throughput = smplMetrics.Get("throughput", "legis/legis.load-balancer/find-routes") / 1000;
		Double respTime = smplMetrics.Get("response-time", "legis/legis.load-balancer/find-routes");
		Double cntUsers = smplMetrics.Get("count-users", "legis/legis.load-balancer/find-routes");
		Double sparkContainersCnt = smplMetrics.Get("spark-containers-cnt", "legis");
		Double webContainerCnt = smplMetrics.Get("web-containers-cnt", "legis");

		operaModel.SetNodeMultiplicity("WebHost", webContainerCnt.intValue());
		operaModel.SetNodeMultiplicity("AnalyticHost1", sparkContainersCnt.intValue());
		operaModel.SetPopulation("select 0", cntUsers);
		operaModel.solve();

		double[] measuredMetrics = { cpuLBUtil, cpuWebUtil, cpuAnalyticUtil, cpuDBUtil, respTime, throughput };
		EstimationResults res = operaModel.theEstimator.EstimateModelParameters(measuredMetrics);
		System.out.println(res);
	}

	public static void main(String[] args) throws IOException {
		double[] desiredRT = { 80, 100, 120, 140, 160, 180, 200, 220 };
		double desiredRTest = 600;

		OperaInterface oi = new OperaInterface("./input/BW-Simple DB Operations.model.pxl",
				"./input/BW-Simple DB Operations.kalman.config", "20", "./output/FinalSimpleDBOpModel.pxl");
		// you don't need offline training at runtime; instead you need to use
		// createSampleMetrics method to create the runtime measurement.
		// oi.offlineModelTraining("/Users/Nasim/Workspace/AdaptiveControlWithBW/output/cloud-controller.hogna.sdo.metrics.data",
		// 27, 1, 2);
		HashMap<String, Double> bwRate = new HashMap<String, Double>();
		bwRate.put("192.168.1.1", 360.0);
		bwRate.put("192.168.1.2", 0.55);
		// double[] targetBWS = oi.getTargetBW(0.55, "select 1", 31.5, 974,
		// desiredRTest, "DelayCenter1", bwRate);
		// System.out.println(targetBWS[0] + "," + targetBWS[1]);

		// MetricCollection smplMetrics = oi.createSampleMetrics();
		// oi.calibrateModel(smplMetrics);
		// oi.getContainersCnt(smplMetrics, 20.0, 80.0, 20.0, 80.0);
		// for (int i = 0; i < desiredRT.length; i++) {
		//
		// double[] targetBWS = oi.getTargetBW(360.0, "select 0",
		// oi.operaModel.GetPopulation("select 0"),67.71415, desiredRT[i],
		// "DelayCenter0");
		// oi.recordBW(desiredRT[i],
		// oi.operaModel.GetResponseTimeScenario("select 0"),
		// targetBWS[0]*1000,"select 0");
		// }
		// for (int i = 0; i < desiredRT.length; i++) {
		// double[] targetBWS = oi.getTargetBW(360.0, "select 1",
		// oi.operaModel.GetPopulation("select 1"),66.60572, desiredRT[i],
		// "DelayCenter1");
		// oi.recordBW(desiredRT[i],
		// oi.operaModel.GetResponseTimeScenario("select 1"),
		// targetBWS[0]*1000,"select 1");
		//
		// }

		// double targetBWS0 = oi.getTargetBW(360, "select 0",
		// oi.operaModel.GetPopulation("select
		// 0"),oi.operaModel.GetResponseTimeScenario("select 0"), desiredRTS0,
		// "DelayCenter0");
		//// System.out.println("Target BW for S0 is: " + targetBWS0 + " and the
		// RT_S0 will be: "
		//// + oi.operaModel.GetResponseTimeScenario("select 0"));
		// oi.recordBW(desiredRTS0,
		// oi.operaModel.GetResponseTimeScenario("select 0"), targetBWS0);
		// double targetBWS1 = oi.getTargetBW(360, "select 1",
		// oi.operaModel.GetPopulation("select
		// 1"),oi.operaModel.GetResponseTimeScenario("select 1"), desiredRTS1,
		// "DelayCenter1");
		// System.out.println("Target BW for S1 is: " + targetBWS1 + " and the
		// RT_S1 will be: "
		// + oi.operaModel.GetResponseTimeScenario("select 1"));
		System.out.println("end");
	}
}
