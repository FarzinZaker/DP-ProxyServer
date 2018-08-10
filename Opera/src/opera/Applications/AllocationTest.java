package opera.Applications;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import opera.Core.LQM;
import opera.Core.LQMException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AllocationTest extends LQM {
	public static void main(String argv[]) {

		//
		if (argv.length < 2) {

			System.out.println("Usage: main  in_file_name out_file_name option");
			return;
		}
		try {
			parseAndRun(argv);
		} catch (LQMException ex) {
			ex.printStackTrace();
			System.out.flush();
			return;
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.flush();
			return;
		}

	}

	float getMaxResp(String sc) {
		float max = 0;
		NodeList list = outDoc.getElementsByTagName("Workload");
		for (int i = 0; i < list.getLength(); i++) {
			//get per class metrics
			NodeList metrics =
				((Element) list.item(i)).getElementsByTagName("PerScenarioMetrics");
			for (int j = 0; j < metrics.getLength(); j++) {
				if (((Element) metrics.item(j))
					.getAttributeNode("scenario")
					.getNodeValue()
					.equals(sc)) {
					Element el =
						(Element) ((Element) metrics.item(j)).getElementsByTagName(
							"ResponseTime").item(
							0);
					float resp = Float.valueOf(el.getFirstChild().getNodeValue()).floatValue();
					if (resp > max)
						max = resp;
				}
			}
		}
		return max;
	}

	float getSimplexMaxResp(String sc) {
		float max=0;
		NodeList list = outDoc.getElementsByTagName("ExtremeMetrics");
		for (int i = 0; i < list.getLength(); i++) {
			if (((Element) list.item(i))
				.getAttributeNode("scenario")
				.getNodeValue()
				.equals(sc)) {
				Element el =
					(Element) ((Element) list.item(i)).getElementsByTagName(
						"MaxResponseTime").item(
						0);
				try{		
				 max= Float
					.valueOf(el.getAttributeNode("value").getNodeValue())
					.floatValue();
				 return max;	
				}catch (Exception ex){};	
			}
		}
		return max;
	}

	public static void parseAndRun(String argv[]) {

		//
		AllocationTest al = new AllocationTest();
		Document doc = null;
		int noExp = 2;
		if (argv.length >= 3) {
			noExp = Integer.valueOf(argv[2]).intValue();
		}

		int N = 100;
		int N1 = 0;
		int N2 = 0;

		NodeList MixOfInterest = null;
		Element Population = null;
		Element Workload = null;
		NodeList Objs = null;
		NodeList PerClassVisits = null;
		java.util.Random rand = new java.util.Random();
		;
		float[] maxRespClass1 = new float[noExp];
		float[] maxRespClass2 = new float[noExp];
		float[] simplexMaxRespClass1 = new float[noExp];
		float[] simplexMaxRespClass2 = new float[noExp];

		//argv[0] is the input 
		doc = al.parsePXL(argv[0]);

			// generate the workloads
			//assume 2 classes and the total population known
			N =
				Integer
					.valueOf(
						((Element) doc.getElementsByTagName("Population").item(0))
							.getFirstChild()
							.getNodeValue())
					.intValue();
			Workload = (Element) doc.getElementsByTagName("Workload").item(0);

			for (N1 = 1; N1 < N; N1++) {
				N2 = N - N1;
				Element mofi = doc.createElement("MixOfInterest");
				Workload.insertBefore(
					mofi,
					Workload.getElementsByTagName("ThinkTimes").item(0));
				//first scenario
				Element mix1 = doc.createElement("Mix");
				mofi.appendChild(mix1);
				mix1.setAttribute("scenario", "1");
				mix1.setAttribute("value", String.valueOf(N1));
				// second scenario
				Element mix2 = doc.createElement("Mix");
				mofi.appendChild(mix2);
				mix2.setAttribute("scenario", "2");
				mix2.setAttribute("value", String.valueOf(N2));
			}

			// generate random demands

		for (int k = 0; k < noExp; k++) {

			try {
				Objs = doc.getElementsByTagName("Object");

				int TotalObjects = Objs.getLength();

				/*
				*reads the object information: 
				*   code, name, (per scenario cpuDemand,per scenario DISKDemand)*,
				*   #visits, assignedToHost, hostID,AssignedtoEntity, entityID
				*/
				for (int i = 0; i < TotalObjects; i++) {
					for (int j = 0; j < 2; j++) { // assume 2 scenarios
						NodeList Demands = ((Element) Objs.item(i)).getElementsByTagName("Demand");

						int cp = rand.nextInt(25);
						Demands.item(j).getAttributes().getNamedItem("CPUDemand").setNodeValue(
							String.valueOf(cp));

						int dsk = rand.nextInt(25);
						Demands.item(j).getAttributes().getNamedItem("DiskDemand").setNodeValue(
							String.valueOf(dsk));
					}
				};
			} catch (Exception e) {

			}


		al.readAndValidatePxl(doc);

		al.initialize();

		// open the output file
		al.outDoc = new org.apache.xerces.dom.DocumentImpl();
		al.findConfigurations();
		
		
		maxRespClass1[k] = ((AllocationTest) al).getMaxResp("1");
		simplexMaxRespClass1[k] = ((AllocationTest) al).getSimplexMaxResp("1");
		maxRespClass2[k] = ((AllocationTest) al).getMaxResp("2");
		simplexMaxRespClass2[k] = ((AllocationTest) al).getSimplexMaxResp("2");
		
				
		System.out.println(k+"\t"+maxRespClass1[k]+ "\t"+ simplexMaxRespClass1[k]+ "\t"+maxRespClass2[k]+ "\t"+simplexMaxRespClass2[k]);
		System.out.print("");
		
	}
	// output the results

		PrintStream f = null;
		try {
			f = new PrintStream(new FileOutputStream(new File(argv[1]+".txt")));
		} catch (Exception ex) {
			System.out.println("Cannot create output file");
			return;

		}
		
	f.println("Ex"+"\t"+"maxRespClass1[k]"+ "\t"+ "simplexMaxRespClass1[k]"+ "\t"+"maxRespClass2[k]"+ "\t"+"simplexMaxRespClass2[k]");
	for ( int k=0;k<noExp;k++){
		
		f.println(k+"\t"+maxRespClass1[k]+ "\t"+ simplexMaxRespClass1[k]+ "\t"+maxRespClass2[k]+ "\t"+simplexMaxRespClass2[k]);
	}
    f.close();		
	}

}