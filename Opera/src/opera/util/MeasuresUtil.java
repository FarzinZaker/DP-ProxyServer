package opera.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MeasuresUtil {
	// TYPE_1 are the outputs of the first round of experiments on York Edge
	// TYPE_2 are the outputs of the second round of experiments on Core

	private ArrayList<Double> userCountSelect0;
	private ArrayList<Double> respTimeSelect0;
	private ArrayList<Double> throughputSelect0;
	private ArrayList<Double> userCountSelect1;
	private ArrayList<Double> respTimeSelect1;
	private ArrayList<Double> throughputSelect1;

	private ArrayList<Double> cpuLBUtil;
	private ArrayList<Double> cpuWebUtil;
	private ArrayList<Double> cpuDBUtil;
	private ArrayList<Double> cpuProxyUtil;
	private ArrayList<Double> VMNoWeb;
	private ArrayList<Double> BWSelect0;
	private ArrayList<Double> BWSelect1;

	private static HashMap scenarioSize;
	private HashMap metrics;
	private HashMap aveMetrics;
	private HashMap cpuDemands;

	private int startLine = 100;
	private int noOfMeasurs = 1;

	public MeasuresUtil() {

		userCountSelect0 = new ArrayList<Double>();
		respTimeSelect0 = new ArrayList<Double>();
		throughputSelect0 = new ArrayList<Double>();

		userCountSelect1 = new ArrayList<Double>();
		respTimeSelect1 = new ArrayList<Double>();
		throughputSelect1 = new ArrayList<Double>();

		cpuProxyUtil = new ArrayList<Double>();
		cpuDBUtil = new ArrayList<Double>();
		cpuWebUtil = new ArrayList<Double>();
		cpuLBUtil = new ArrayList<Double>();
		VMNoWeb = new ArrayList<Double>();
		BWSelect0 = new ArrayList<Double>();
		BWSelect1 = new ArrayList<Double>();

		metrics = new HashMap<String, ArrayList>();
		aveMetrics = new HashMap<String, Double>();
		cpuDemands = new HashMap<String, Double>();
		scenarioSize = new HashMap<String, Integer>();

		scenarioSize.put("select 0", 64 * 8);// Kb
		scenarioSize.put("select 1", 288 * 8);//kb

	}

	// this method takes a scenario and its number of users and returns the time
	// it takes to
	// send data through a link with bandwidth BW
	// TODO: we don't know the division by 1000 in this formula for sure
	 public double calDelayDemand(Double BW, String scenario, double
	 wl){//wl=#of users in scenario
	 Double bwInkbps=BW*1000;
	 Double dataSize=(Integer)scenarioSize.get(scenario)*wl;
	 return (Double) (dataSize/BW);
	 //we have to check this one
	 //return (Double) (dataSize/BW)/1000;
	 }
	
	 public double calBW(Double rt, String scenario, double wl){//wl=#of users in scenario
//		 Double dataSize=(Integer)scenarioSize.get(scenario)*wl;
//		 return (Double) (dataSize/rt);
		 return (-0.24*rt)+501;
		 }

	//here we are using regression formual to calculate the delay of delaycenters
//	public double calDelayDemand(Double BW, String scenario, double wl) {
//		return (11.93 * wl) - (0.09738 * BW*1000) + 74;
//			}

	public int getNoOfMeasurs() {
		return this.noOfMeasurs;
	}

	public void parseFile(String file, int startLineNo, int noMeasures) {
		startLine = startLineNo;
		noOfMeasurs = noMeasures;
		try {
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int currentLine = 1;

			while (br.readLine() != null && currentLine < startLine - 1) {
				currentLine++;
			}
			// we do the multiplication by 8 and 4 because these containers
			// don't use the whole cpu.
			// we also devided the cpu utils by 100 to scale them to the range
			// of [0, 1]
			// refer to the input measure file header that describes the share
			// of cpu for each container
			// we also divide userCount and throughput by 1000 to make them for
			// millisecond
			while ((strLine = br.readLine()) != null && currentLine <= (startLine + noOfMeasurs)) {
				if (strLine.length() == 0 || strLine.toCharArray()[0] == '#')
					continue;
				String[] tokens = strLine.split(" +");

				VMNoWeb.add(Double.valueOf(tokens[3]));
				cpuDBUtil.add(Double.valueOf(tokens[4]));// cloudWatch, no need
															// to divide by 100
				cpuWebUtil.add(Double.valueOf(tokens[5]));// cloudWatch
				cpuLBUtil.add(Double.valueOf(tokens[6]));// cloudWatch
				cpuProxyUtil.add(Double.valueOf(tokens[7]) / 100);// snmp,
																	// should be
																	// devided
																	// by 100 to
																	// be in
																	// [0,1]
				userCountSelect0.add(Double.valueOf(tokens[16])); // number of
																	// users
				respTimeSelect0.add(Double.valueOf(tokens[8]));// ms
				throughputSelect0.add(Double.valueOf(tokens[10]) / 1000);// req/ms
				BWSelect0.add(Double.valueOf(tokens[14]) / 1000);// kbpms

				userCountSelect1.add(Double.valueOf(tokens[17])); // number of
																	// users
				respTimeSelect1.add(Double.valueOf(tokens[11]));// ms
				throughputSelect1.add(Double.valueOf(tokens[13]) / 1000);// req/ms
				BWSelect1.add(Double.valueOf(tokens[15]) / 1000);// kbpms

				currentLine++;
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	/* Returns metrics in the same order as Kalman config file */

	public HashMap getMetrics() {
		metrics.put("cpuLBUtil", cpuLBUtil);
		metrics.put("cpuWebUtil", cpuWebUtil);
		metrics.put("cpuDBUtil", cpuDBUtil);
		metrics.put("cpuProxyUtil", cpuProxyUtil);
		metrics.put("VMNoWeb", VMNoWeb);
		metrics.put("respTimeS0", respTimeSelect0);
		metrics.put("throughputS0", throughputSelect0);
		metrics.put("userCountS0", userCountSelect0);
		metrics.put("respTimeS1", respTimeSelect1);
		metrics.put("throughputS1", throughputSelect1);
		metrics.put("userCountS1", userCountSelect1);
		metrics.put("bwS0", BWSelect0);
		metrics.put("bwS1", BWSelect1);

		return metrics;
	}

	/*
	 * this methods calculate the cpu demands based on the U = X.D formula. -> D
	 * = U/X
	 */
	public HashMap getAvgCPUDemands() {
		cpuDemands.put("CPUDemandLB", (Double) aveMetrics.get("aveCPULBUtil") / (Double) aveMetrics.get("throughput"));
		cpuDemands.put("CPUDemandWeb",
				(Double) aveMetrics.get("aveCPUWebUtil") / (Double) aveMetrics.get("throughput"));
		cpuDemands.put("CPUDemandProxy",
				(Double) aveMetrics.get("aveCPUProxyUtil") / (Double) aveMetrics.get("throughput"));
		cpuDemands.put("CPUDemandDB", (Double) aveMetrics.get("aveCPUDBUtil") / (Double) aveMetrics.get("throughput"));
		return cpuDemands;
	}

	public static void main(String[] args) {
		MeasuresUtil rm = new MeasuresUtil();
		rm.parseFile("/Users/Nasim/Workspace/Copy of SaviVirnet/output/cloud-controller.hogna.sdo.metrics.data", 1, 2);
		System.out.println(rm.getMetrics());
	}
}
