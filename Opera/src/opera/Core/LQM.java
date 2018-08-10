
package opera.Core;
import java.io.StringReader;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class LQM
{
	// these are constants
	static final int MaxNoScenarios = 20;
	static final int MaxNoObjects = 50;
	static final int MaxNoConfig = 100;
	static final double step = (double) 0.5;
	static final int MaxNoStations = 40;
	static final int infinit = 999999999;
	static final double dUt = (double) 0.01;

	// kind of global variables.
	boolean traceConfigurations = true;
	boolean verbose = false;
	boolean report = true;
	boolean loadBalancing = false;
	int TotalObjects;
	int TotalNetworks;
	int TotalMidwares;

	int TotalHosts;
	int TotalEntities;
	int TotalScenarios;
	int TotalConstraints;
	int TotalObjConfigurations;
	int NoClasses;
	public int getNoClasses(){
		return NoClasses;
	}
	//DataInputStream fpi;
	//DataInputStream fpEP;

	protected Document outDoc = null;
	//String outXmlFile = null;
	DOMParser parser = null;
	Element recomendedArch = null;
	Mva aMva = null;
	ModelSolver ms = null;
	double aDpkc[][][] = null; //demands per process/device/class
	//		Simplex aSimplex = null;
	protected int refDev=-1; // the reference device.
	protected String refDevName;
	String costFunctionType;
	int indexCostFunction;

	int indexScenarios;
	int indexConfig;

	int noServersAndDelays = 0;
	int noClients = 0;

	int NoVar = 0;

	int i, j;
	int k;
	double CPU_D;
	double DISK_D;
	int NoCustomers;
	int NoConstraints;
	String typeAlocation = new String();
	double cursor[] = new double[MaxNoScenarios];
	double LimitCursor[] = new double[MaxNoScenarios];
	double Satisfaction[] = new double[MaxNoScenarios];
	double S;
	double Rc[] = new double[MaxNoScenarios];
	double Rc_min[];
	double Rc_max[];
	Vector<double[]> Workloads = null; //FIXME: add generics to this vector
	boolean workloadType[]=null;
	double Nc[];

	int scenarioTriggers[]; //keeps the indices of the scenarioTriggers
	public int[] getScenarioTriggers(){
		return scenarioTriggers;
	}
	String scenarioNames[];
	
	public String[] getClassNames(){
		return scenarioNames;
	}
	double uVector[][];
	double bVector[];
	protected double Z[];
	double A[][];
	int maxC;
	int minC;
	ArrayList<double[]> mixes = null;

/*
	public synchronized void saveDOMTree(OutputStream outs, Document doc) throws IOException
	{
	      try
	      {
		      javax.xml.transform.TransformerFactory transfac = javax.xml.transform.TransformerFactory.newInstance();
		      javax.xml.transform.Transformer trans = transfac.newTransformer();
		      trans.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");
		      trans.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
		      trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	
		      // Print the DOM node
	
		      javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(outs);
		      javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(doc);
			trans.transform(source, result);
	      }
	      catch (Exception e)
	      {
			e.printStackTrace();
	      }

	      //org.apache.xml.serialize.XMLSerializer out =
			//new org.apache.xml.serialize.XMLSerializer(outs, null);
		//out.serialize(doc);

	}
*/
	// this is kind of structure - just for data 
	private class Config
	{
		int allocation[][] = new int[MaxNoConfig][MaxNoObjects];
		int valid[] = new int[MaxNoConfig];
		double Rc[][]  = new double[MaxNoScenarios][MaxNoConfig];
		double Uo[][]  = new double[MaxNoConfig][MaxNoObjects];
		double Sat[][] = new double[MaxNoScenarios][MaxNoConfig];
		int Number;
		double globalSat[] = new double[MaxNoConfig];

		double Utility[] = new double[MaxNoConfig];
		double Cost[][] = new double[MaxNoScenarios][MaxNoConfig];
		double betas[][] = new double[MaxNoScenarios][MaxNoConfig];
	}

	Config ObjConfig = new Config();

	int[] hostType;
	
	Host Hosts[];
	public Host[] getHosts(){
		return Hosts;
	}

	Network Networks[];
	public Network[] getNetwork(){
		return Networks;
	}

	
	public Midleware Midwares[];
	public Midleware[] getMidlewares(){
		return Midwares;
	}

	private class ObjectPairs {
		DistributedObject pObject1 = new DistributedObject(LQM.this);
		DistributedObject pObject2 = new DistributedObject(LQM.this);
		double visits;
		ObjectPairs next;
	}

	double betas[] = new double[MaxNoScenarios];
	private class ListPair {
		ObjectPairs head = new ObjectPairs();
		int number;
		ObjectPairs temppair = new ObjectPairs();
		ObjectPairs cursor = new ObjectPairs();
		ObjectPairs previous = new ObjectPairs();

		//method definition
		ListPair() {
			head = null;
			number = 0;
		}

		int getNumber() {
			return (number);
		}

		ObjectPairs deletePair() {
			ObjectPairs temp = new ObjectPairs();
			number--;
			temppair = head;
			temp = temppair;
			head = head.next;
			return temp;
		}

		void addPair(
			DistributedObject o1,
			DistributedObject o2,
			double cvisits,
			int type) {
			temppair = new ObjectPairs();
			temppair.pObject1 = o1;
			temppair.pObject2 = o2;
			temppair.visits = cvisits;
			number++;
			if (head == null) {
				head = temppair;
				temppair.next = null;
				return;
			}

			if (type == 1) { // decreasing order of visits
				previous = head;
				for (cursor = head; cursor != null; cursor = cursor.next) {
					if (cursor.visits < cvisits) { // is time to insert;
						temppair.next = cursor;
						if (cursor == head)
							head = temppair;
						else {
							previous.next = temppair;
						}
						break;
					} else {
						previous = cursor;
					} //endiff
				} //endfor
				if (cursor == null) {
					previous.next = temppair;
					temppair.next = null;
				}

			} //endif;
		}

	}

	ListPair L = new ListPair();
	public double VisitTable[][];
	//  a suppoints an array of visits between objects for current Scenario
	
	double ScenarioVisits[][][];
	public double[][][] getScenarioVisits(){
		return ScenarioVisits;
	}
	
	double ScenarioMessages[][][];
	
	String ScenarioVisitType[][][]; // s, a, f
	public String[][][] getScenarioVisitType(){
		return ScenarioVisitType;
	}
	
	Entity Entities[];
		public Entity[] getEntities(){
		return Entities;
	}
	
	Entity EntitiesBackup[];
	
	public Entity[] getEntitiesBackup(){
		return EntitiesBackup;
	}
	
	int NoVariables;
	double TVisits[]; // Total Visits
	int indexO[];
	
	DistributedObject[] Objects;
	public DistributedObject[] getObjects(){
		return Objects;
	}
	private void alocateToEntities() {
		int i, j;
		for (i = 0; i < TotalObjects; i++) {
			TVisits[i] = 0;
			for (j = 0; j < TotalObjects; j++) {
				if (i != j) {
					TVisits[i] += VisitTable[j][i] + VisitTable[i][j];
				}
			}
		}
		// order objects by the total visits
		ordvector(TVisits, indexO, TotalObjects);

		// temp variables
		int iE = 0, foundsomething;
		double C = 0;
		double tmin = 999999;

		if (verbose)
			System.out.println("alocating objects to entities....");
		for (i = 0; i < TotalObjects; i++) {
			if (!(Objects[indexO[i]].AssignedE > 0)) {
				// not assigned to an Entity
				//find Entity
				foundsomething = 0;
				tmin = 999999;
				for (j = 0; j < TotalEntities; j++) {
					C = 0; // initialize the cost
					if (Entities[j].AssignedToH
						== Objects[indexO[i]].AssignedTo)
						// valid Entity
						{
						for (k = 0;
							k < TotalObjects;
							k++) { // find assigned objects on the same Host and
							if ((Objects[k].AssignedE > 0)
								&& (Objects[k].AssignedTo
									== Objects[indexO[i]].AssignedTo)
								&& Objects[k].AssignedToE != j) {
								C += VisitTable[i][k] + VisitTable[k][i];
								foundsomething = 1;
							}
						}

						if ((tmin = min(C, tmin)) == C) {
							iE = j; // memorize this Entity
						}
					}
				}
				if (foundsomething != 1) { //find something arbitrary
					for (j = 0; j < TotalEntities; j++) {

						if (verbose)
							System.out.println(
								" Allocate arb..  ent, Host,hose "
									+ j
									+ " "
									+ Entities[j].AssignedToH
									+ " "
									+ Objects[indexO[i]].AssignedTo);
						if (Entities[j].AssignedToH
							== Objects[indexO[i]].AssignedTo) {
							iE = j;
							break;
						}
					}
				}
				if (verbose)
					System.out.println(
						"  alocated object: "
							+ Objects[indexO[i]].name
							+ "to Entity: "
							+ iE);
				if (verbose)
					System.out.println(
						"  alocated object: "
							+ Objects[indexO[i]].name
							+ "to Host: "
							+ Objects[indexO[i]].AssignedTo);

				Objects[indexO[i]].AssignedE = 1;
				Objects[indexO[i]].AssignedToE = iE;

			} //end if
		} //end for
	}

	protected void recordConfiguration()
	{
		if (verbose)
			System.out.println("writting down the conf...");

		int foundConfig = 0;
		int differ;
		for (int i = 0; i < ObjConfig.Number; i++)
		{
			// for each configuration in ObjConfig
			differ = 0;
			for (int j = 0; j < TotalObjects; j++)
			{
				// for each Object
				if (ObjConfig.allocation[i][j] != Objects[j].AssignedToE)
				{
					differ = 1;
					break;
				}
			}

			if (differ < 1)
			{
				foundConfig = 1;
				break;
			}
		}

		if (foundConfig != 1)
		{
			for (j = 0; j < TotalObjects; j++)
			{
				ObjConfig.allocation[ObjConfig.Number][j] = Objects[j].AssignedToE;
				ObjConfig.Uo[ObjConfig.Number][j] = Objects[j].Uo;
			}
			// for (j=0;j<TotalScenarios;j++)
			// ObjConfig.betas[ObjConfig.Number][j]=betas[j];

			for (j = 0; j < NoClasses; j++)
			{
				ObjConfig.Rc[j][ObjConfig.Number] = Rc[j];
				ObjConfig.Sat[j][ObjConfig.Number] = Satisfaction[j];
			}
			ObjConfig.globalSat[ObjConfig.Number] = S; // satisfaction
			ObjConfig.Number++;

		}
		else
		{
			if (verbose)
				System.out.println("Found duplicate conf...Not RECORDED");
		}
	}

	private int getBestConfIndex() {

		int max = 0;
		for (int i = 0; i < ObjConfig.Number; i++)
			if (ObjConfig.globalSat[i] > ObjConfig.globalSat[max])
				max = i;
		return max;
	}

	private int betasC(int N) {
		int i, k;
		double cursorSum;
		cursorSum = 0;
		i = N - 2;
		if (i < 0)
			return 0;
		try {
			while ((i >= 0) && !(cursor[i] < LimitCursor[i]))
				// find first cursor to increment;
				{
				i--;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}

		if (i < 0)
			return 0; // no more configurations;
		else {
			cursor[i] += step; // increment the first cursor found;

			for (k = 0;
				k <= i;
				k++) // establish the upper limit for the next cursor;
				cursorSum += cursor[k];
			for (k = i + 1; k < N; k++)
				LimitCursor[k] = 1 - cursorSum;
			for (k = i + 1; k < N - 1; k++) // reset the next cursor
				{
				cursor[k] = 0;
			}
			cursorSum = 0;
			for (k = 0; k < N - 1; k++)
				cursorSum += cursor[k];
			cursor[N - 1] = 1 - cursorSum;
			return 1; //succes;
		} // end else
	}

	public void updateLQMfromMva() {
		
		//response time for each scenario
		for (int c = 0; c < NoClasses; c++) {
			double af = computeRoc(scenarioTriggers[c], c);
			//        if (verbose) System.out.println("   response time "+af);	 
		}

	
        // throughput of each object
		for (int c = 0; c < NoClasses; c++) {
			for (int o =0; o < TotalObjects; o++)
				Objects[o].Xoc[c] = ms.getSXc(c);
		}

		// throughput of each scenario
		for (int c = 0; c < NoClasses; c++) {
			Objects[scenarioTriggers[c]].Xoc[c] = ms.getSXc(c);
			if (verbose)
				System.out.println(
					" Throughput..., scenario " + c + " " + Objects[c].Xoc[c]);
		}
		
		// object Utilization
		
		for (int c = 0; c < NoClasses; c++) {
			for (int o = 0; o < TotalObjects; o++)
				Objects[o].Uoc[c] = Objects[o].Xoc[c] * Objects[o].Roc[c];
		}

		// total utilization;
		for (int o = 0; o < TotalObjects; o++) {
			Objects[o].Uo = 0;
			for (int c = 0; c < NoClasses; c++) {
				Objects[o].Uo += Objects[o].Uoc[c];
			}
		}
	}

	public double computeRoc(int o, int c) {
		if (verbose)
			System.out.println("  Computing response times...");
		Objects[o].Roc[c] = 0;
		// add the local response time, directly from demand or from the mva 
		//System.out.println("Object" +" " +o +"name "  +Objects[o].name +", Class  " + c +", AssignedTo "+Objects[o].AssignedTo);

		if (Hosts[Objects[o].AssignedTo].server == 0) { // if it is client
			//if (Objects[o].ScenarioDemand[c].CPUdemand > 0)
				Objects[o].Roc[c] += Objects[o].ScenarioDemand[c].CPUdemand;
			//if (Objects[o].ScenarioDemand[c].DISKdemand > 0)
				Objects[o].Roc[c] += Objects[o].ScenarioDemand[c].DISKdemand;
		} else { // if it is server, its index is greater than 1
			if (verbose)
				System.out.println(
					"    Sc CPU demands and Host demands"
						+ Objects[o].ScenarioDemand[c].CPUdemand
						+ "  "
						+ Hosts[Objects[o].AssignedTo].scCPUdemand[c]);
			if (verbose)
				System.out.println(
					"    Sc CPU demands and Host demands"
						+ Objects[o].ScenarioDemand[c].DISKdemand
						+ "  "
						+ Hosts[Objects[o].AssignedTo].scDISKdemand[c]);
			//computes the fraction of object o response time from total process response time
			
			if ((Objects[o].ScenarioDemand[c].CPUdemand+Objects[o].ScenarioDemand[c].DISKdemand) > 0)
				Objects[o].Roc[c]
					+= ((Objects[o].ScenarioDemand[c].CPUdemand * Hosts[Objects[o].AssignedTo].CPURatio +
						  Objects[o].ScenarioDemand[c].DISKdemand * Hosts[Objects[o].AssignedTo].DISKRatio) /
						(aDpkc[Objects[o].AssignedToE][Objects[o].AssignedTo* 2][c]+
								 aDpkc[Objects[o].AssignedToE][Objects[o].AssignedTo * 2+1][c]))*
					ms.getSRpc(Objects[o].AssignedToE, c);
		}
			
		// now look for the response time from other objects
		for (int j = 0; j < TotalObjects; j++) {
			if (ScenarioVisits[c][o][j] > 0) {
				// consider the visited object response time
				/*if (networkOf(o, j) > -1) {
					// there is a Network between the objects
					Objects[o].Roc[c] += ScenarioVisits[c][o][j]
						* (ScenarioMessages[c][o][j]
							* Networks[networkOf(o, j)].msPerByte
							+ Networks[networkOf(o, j)].latency);
				}*/
				double tv = 0;
				for (int k = 0; k < TotalObjects; k++)
					tv += ScenarioVisits[c][k][j];
				double temp = computeRoc(j, c);
				if (ScenarioVisitType[c][o][j].equals("s"))
					Objects[o].Roc[c] += (ScenarioVisits[c][o][j] / tv) * temp;
			}
		}
		if (verbose)
			System.out.println("   object " + o + "Rc " + Objects[o].Roc[c]);
		return Objects[o].Roc[c];
	}

	protected void buildMatrixA() {

		//find the reference device
		int i, j;

		int offs = 0;
		if (A == null)
			A = new double[noServersAndDelays][NoClasses];

		for (i = 0; i < TotalHosts; i++) {
			if (Hosts[i].server != 0) { // we have a delay or a server
				for (j = 0; j < NoClasses; j++) {
					A[offs][j] = Hosts[i].scCPUdemand[j];
					A[offs + 1][j] = Hosts[i].scDISKdemand[j];
				}
				offs = offs + 2;
			}
		}
		if (verbose) {

			System.out.print(" This is the matrix A....\n");
			for (i = 0; i < noServersAndDelays; i++) {
				System.out.print("\n     ");
				for (j = 0; j < NoClasses; j++) {
					System.out.print("  " + A[i][j]);
				}
			}
			System.out.print("\n");
		}
		refDev = -1;
		for (i = 0; i < noServersAndDelays; i++) {
			refDev = i; // loooking for CPUs
			for (j = 0; j < NoClasses; j++) {
				if (A[i][j] == 0)
					refDev = -1;
			}
			if (refDev >= 0)
				break;
		}
	}

	public void changeConfiguration() {

		if (typeAlocation.equals("VL")) {
			vhlChangeConfiguration();
		} //end if "VL"

		if (typeAlocation.equals("HL")) {
			hlChangeConfiguration();

		} // end if "HL"  

		if (typeAlocation.equals("ML")) {
			mlChangeConfiguration();
		} // end if "ML"

	}
	// cost function
	// returns the maximum deviation of demand
	private double cost(Host h, DistributedObject o) {
		if (!(h.server > 0))
			return (infinit);
		if (DISK_D > 0)
			return max(
				-CPU_D + h.CPUdemand + o.CPUdemand * h.CPURatio,
				-DISK_D + h.DISKdemand + o.DISKdemand * h.DISKRatio);
		else
			return (-CPU_D + h.CPUdemand + o.CPUdemand * h.CPURatio);

	}
	// cost function
	// returns the maximum deviation of demand

	private double cost2(Host h, ObjectPairs p) {
		if (!(h.server > 0))
			return (infinit);
		if (DISK_D > 0)
			return max(
				-CPU_D
					+ h.CPUdemand
					+ (p.pObject1.CPUdemand + p.pObject2.CPUdemand) * h.CPURatio,
				-DISK_D
					+ h.DISKdemand
					+ (p.pObject1.DISKdemand + p.pObject2.DISKdemand)
						* h.DISKRatio);

		else
			return (
				-CPU_D
					+ h.CPUdemand
					+ (p.pObject1.CPUdemand + p.pObject2.CPUdemand));
	}
	//end allocation for high load

	protected void setCostFunction(String aS, int anInd) {
		costFunctionType = aS;
		indexCostFunction = anInd;
	}

	protected double getCostFunction() {
		updateLQMfromMva();
		if (costFunctionType.equals("ResponseTime")) {
			if (verbose)
				System.out.println(
					"  response time of scenario..."
						+ indexCostFunction
						+ ",  "
						+ Objects[indexCostFunction].Roc[indexCostFunction]);
			return Objects[indexCostFunction].Roc[indexCostFunction];
		} else if (costFunctionType.equals("ObjectUtilization")) {
			return Objects[indexCostFunction].Uo;
		}

		return (double) 0.0;

	}
	/*
			public void asymptoticUo() {
	
				for (int i = NoClasses; i < TotalObjects; i++) {
					if (verbose)
							System.out.println("computing Utilization of object.." + i);
					setCostFunction("ObjectUtilization", i);
					aSimplex.initializeFromAllocation();
					Objects[i].Uo = aSimplex.solve();
					//            System.out.println("Object "+i+ " utilization..."+Objects[i].Uo);
					//            if(verbose) System.out.println("Utilization of object " +i+" " +Objects[i].Uo);
	
					// store the coordonates maximums.
					//            for (int j=0;j<NoVar;j++)
					//              uVector[i][j]=aSimplex.currentU[j];
					// the response time is already stored by simplex          			
	
				}
	
			}
	*/
	public void solveLQM(double[] Nc) {

		initializeModelSolver();
		ms.solveModel(Nc);
		updateLQMfromMva();

	}
	public void solveOpenLQM(double[] Nc) {

		 initializeModelSolver();
		 ms.solveOpenModel(Nc);
		 updateLQMfromMva();

	}
	public void maximumUo() {

		double[] Nc = new double[NoClasses];
		double[] tempUo = new double[TotalObjects];

		Iterator<double[]> iter = mixes.iterator();
		while (iter.hasNext()) {
			double[] point = iter.next();
			for (int i = 0; i < point.length; i++) {
				Nc[i] = point[i] * NoCustomers;
				if (Nc[i] == 0)
					Nc[i] = 1;
			}
			solveLQM(Nc);
			for (int i = 0; i < TotalObjects; i++) {
				//find the maximum repsonse time per scenario i
				if (Objects[i].Uo > tempUo[i]) {
					tempUo[i] = Objects[i].Uo;
					// store the coordinates of the maximum
					//										for (int j = 0; j < NoClasses; j++)
					//											uVector[i][j] = Nc[j];			        			
				}
			}
		}
		//store the maximum in the object
		for (int i = 0; i < TotalObjects; i++)
			Objects[i].Uo = tempUo[i];

	}

	public void maximumRc()
	{
		double[] Nc = new double[NoClasses];
		Iterator<double[]> iter = mixes.iterator();
		while (iter.hasNext())
		{
			double[] point = iter.next();
			for (int i = 0; i < point.length; i++)
			{
				Nc[i] = point[i] * NoCustomers;
				if (Nc[i] == 0)
					Nc[i] = 1;
			}
			solveLQM(Nc);
			for (int i = 0; i < NoClasses; i++)
			{
				//find the maximum response time per scenario i
				if (Objects[scenarioTriggers[i]].Roc[i] > Rc[i])
				{
					// store the coordinates of the maximum
					Rc[i] = Objects[scenarioTriggers[i]].Roc[i];
					for (int j = 0; j < NoClasses; j++)
						uVector[i][j] = Nc[j];
				}
			}
		}
	}
	
	/*
			public void asymptoticRc() {
	
				for (int i = 0; i < NoClasses; i++) {
					if (verbose)
							System.out.println("computing response time of scenario.." + i);
					setCostFunction("ResponseTime", i);
					aSimplex.initializeFromAllocation();
					//find the maximum repsonse time per scenario i
					Rc[i] = aSimplex.solve();
					// store the coordinates of the maximum
					for (int j = 0; j < NoVar; j++)
							uVector[i][j] = aSimplex.currentU[j];
					// the response time is already stored by simplex          			
				}
			}
	*/

	public void findConfigurations()
	{
		double S_1 = 0;
		try
		{
			updateHostsDemands();
			//initializeMva();
			if (NoCustomers > 0) {
				SystemOfEquations soe = new SystemOfEquations(A, bVector);
				soe.solve();
				mixes = soe.getAllExtremePoints();
				maximumRc();
				S = overAllSatisfaction();
				recordConfiguration();

				if ((loadBalancing == true) && (refDev >= 0))
				{
					// if there are objects that can be moved around and there is a shared device(limitation)....

					double attempts = 0;
					do
					{
						//if(traceConfigurations) printConfigurations();
						S_1 = S;
						changeConfiguration();
						updateHostsDemands();
						//initializeMva();
						//					asymptoticRc();
						soe = new SystemOfEquations(A, bVector);
						soe.solve();
						mixes = soe.getAllExtremePoints();
						maximumRc();

						S = overAllSatisfaction();
						if (S == S_1)
							attempts++;
						if (S > S_1)
							attempts = 0;
						recordConfiguration();
						//get the asymptotic response time
					}
					while ((S_1 <= S)
						&& (attempts < 10)
						&& (ObjConfig.Number < 100));

					if (verbose)
					{
						System.out.printf("Stoped because: [%8.5f] >= [%8.5f], attempts = [%8.5f], config = [%8d]", S_1, S, attempts, ObjConfig.Number);
					}
				}

				int maxIndex = getBestConfIndex();
				for (int j = 0; j < TotalObjects; j++)
				{
					Objects[j].AssignedToE = ObjConfig.allocation[maxIndex][j];
				}
				updateHostsDemands();
				soe = new SystemOfEquations(A, bVector);
				soe.solve();
				mixes = soe.getAllExtremePoints();
				maximumUo();
				//				initializeMva();
				//				asymptoticUo();
				for (int j = 0; j < TotalObjects; j++)
				{
					ObjConfig.Uo[maxIndex][j] = Objects[j].Uo;
				}
			}
			else
			{
				recordConfiguration();
			}
			//  printConfigurations();
			createResultsDocument();
			//  printHostsDemands();
			solveForWorkloadMixes();
			//  fpoConf.close();

		} catch (Exception e) {
			throw new LQMException(e.getMessage());
		}
	}


	/*
	public void writeResultsDocument(String outXmlFile)
	{
		try
		{
			File xmlFile = new File(outXmlFile);
			PrintStream xmlps = new PrintStream(new FileOutputStream(xmlFile));
			xmlps.close(); // this is a fix
			saveDOMTree(new FileOutputStream(xmlFile), outDoc);
		}
		catch (Exception e)
		{
			System.out.println("An exception occured finding \n a configuration " + e.toString());
		}
	}*/

	public void solveForWorkloadMixes()
	{
		int size = Workloads.size();

		for (int k = 0; k < size; k++)
		{
			double[] Nc = new double[NoClasses];
			for (int i = 0; i < NoClasses; i++)
			{
				Nc[i] = Workloads.elementAt(k)[i];
				//Nc[i] = (((Float[]) (Workloads.elementAt(k)))[i]).doubleValue();
			}
			if (workloadType[k])
			{
				solveOpenLQM(Nc);// if open model
			}
			else
			{
				solveLQM(Nc);
			}
			addMixesToResults(Nc, k);
		}
	}

	public Document getResultsDocument() {
		return outDoc;
	}

	public void addMixesToResults(double[] n, int index) {

		double users = 0;
		for (int i = 0; i < NoClasses; i++)
			users += n[i];
		Element wk = outDoc.createElement("Workloads");
		recomendedArch.appendChild(wk);
		if (!workloadType[index])// if it is not open
		  wk.setAttribute("users", String.valueOf(users));

		for (int i = 0; i < NoClasses; i++) {
			Element sc = outDoc.createElement("Scenario");
			wk.appendChild(sc);
			sc.setAttribute("name", scenarioNames[i]);
			if (workloadType[index])sc.setAttribute("arrivalRate", String.valueOf(n[i]));
			else sc.setAttribute("users", String.valueOf(n[i]));
			Element rt = outDoc.createElement("ResponseTime");
			sc.appendChild(rt);
			Node tx = outDoc.createTextNode(String.valueOf(Objects[scenarioTriggers[i]].Roc[i]));
			rt.appendChild(tx);
			Element thr = outDoc.createElement("Throughput");
			sc.appendChild(thr);
			Node thrtx =
				outDoc.createTextNode(String.valueOf(Objects[scenarioTriggers[i]].Xoc[i]));
			thr.appendChild(thrtx);
			// Host utilization

		}

		for (i = 0; i < TotalEntities; i++) {
			Element cont = outDoc.createElement("Container");
			wk.appendChild(cont);
			cont.setAttribute("name", Entities[i].name);
			cont.setAttribute("Utilization", String.valueOf(ms.getSUp(i)));
			cont.setAttribute(
				"multiplicity",
				String.valueOf(Entities[i].multiplicity));

			for (int j = 0; j < NoClasses; j++) {
				Element sc = outDoc.createElement("Scenario");
				cont.appendChild(sc);
				sc.setAttribute("scenarioName", scenarioNames[j]);
				sc.setAttribute("responseTime", String.valueOf(ms.getSRpc(i, j)));

			}
		}

		for (i = 0; i < TotalObjects; i++) {
			Element obj = outDoc.createElement("Service");
			wk.appendChild(obj);
			obj.setAttribute("name", Objects[i].name);
			obj.setAttribute("Utilization", String.valueOf(Objects[i].Uo));
			for (int j = 0; j < NoClasses; j++)
			{
				Element sc = outDoc.createElement("Scenario");
				obj.appendChild(sc);
				sc.setAttribute("name", scenarioNames[j]);
				sc.setAttribute("responseTime", String.valueOf(Objects[i].Roc[j]));
			}
		}

		int offs = 0;
		for (int k = 0; k < TotalHosts; k++) {
			Element h = outDoc.createElement("Node");
			wk.appendChild(h);
			h.setAttribute("name", Hosts[k].name);
			Element cpu = outDoc.createElement("CPU");
			h.appendChild(cpu);
			Element cpuUtil = outDoc.createElement("Utilization");
			cpu.appendChild(cpuUtil);
			Node cpuUtilTx =
				outDoc.createTextNode(String.valueOf(ms.getHUk(offs)));
			cpuUtil.appendChild(cpuUtilTx);

			Element disk = outDoc.createElement("DISK");
			h.appendChild(disk);
			Element diskUtil = outDoc.createElement("Utilization");
			disk.appendChild(diskUtil);
			Node diskUtilTx =
				outDoc.createTextNode(String.valueOf(ms.getHUk(offs + 1)));
			diskUtil.appendChild(diskUtilTx);
			offs = offs + 2;
		}
	}

	public void hlChangeConfiguration() {

		double U[][] = new double[NoClasses][noServersAndDelays];
		double Uij[][][] = new double[NoClasses][noServersAndDelays][NoClasses];
		double slackC[][] = new double[NoClasses][noServersAndDelays];
		int Sa[] = new int[noServersAndDelays];
		// the set of hosts that make the source point A(minimum satisfaction).
		int S_Sa[] = new int[noServersAndDelays];
		// the complement set of hosts that make the point A.
		int Sa_Sb[] = new int[noServersAndDelays];
		// the complement set of hosts that make the point B.
		int Sb[] = new int[noServersAndDelays];
		// the set of hosts that make the destination point B(maximum satisfaction).
		int SaSize = 0;
		int Sa_SbSize = 0;
		int S_SaSize = 0;
		int SbSize = 0;
		//restore the objects status:biff=0 means not allocated
		for (i = 0; i < TotalObjects; i++)
			Objects[i].biff = Objects[i].biff_Backup;

		if (verbose)
			System.out.println("Changing configuration..... ");

		if (verbose)
			System.out.println("  Compute per scenario utilizations... ");

		// compute per scenario device utilization
		for (k = 0; k < NoClasses; k++) { // the index is for each extreme point
			for (i = 0; i < noServersAndDelays; i++) {
				for (j = 0; j < NoClasses; j++)
					Uij[k][i][j] = (A[i][j] / A[refDev][j]) * uVector[k][j];
			}
		}
		if (verbose)
			System.out.println(
				"  Compute total utiliz, for scenarios and devices=... "
					+ NoClasses
					+ " "
					+ noServersAndDelays);

		//compute the total device utilization
		for (k = 0; k < NoClasses; k++) { // the index is for each extreme point
			for (i = 0; i < noServersAndDelays; i++) {
				U[k][i] = 0;
				if (verbose)
					System.out.print("Utilization in scenario" + k + " of device " + i + "\n      ");
				for (j = 0; j < NoClasses; j++)
					U[k][i] += Uij[k][i][j];
				if (verbose)
					System.out.print(U[k][i] + " ");
			}
		}

		if (verbose)
			System.out.println("  Compute slack values... ");

		// compute the slack coefficients
		for (k = 0;
			k < NoClasses;
			k++) { // the index is for each extreme point
			for (i = 0; i < noServersAndDelays; i++)
				slackC[k][i] = U[k][i];
		}

		maxC = 0;
		minC = 0;

		for (i = 0; i < NoClasses; i++) {
			if ((1 - Satisfaction[i]) > (1 - Satisfaction[maxC])) {
				maxC = i;
			};
			if ((1 - Satisfaction[i]) < (1 - Satisfaction[minC])) {
				minC = i;
			};
		}

		if (verbose)
			System.out.println("  Compute saturated devices... ");

		// find the devices that make the two extreme points A and B
		// below, dUt is used to count for computational errors 
		for (i = 0; i < noServersAndDelays; i++) {
			if (Math.abs(slackC[minC][i] - 1) < dUt) { // is i making B?
				Sb[SbSize] = i;
				SbSize++;
			}
			if (Math.abs(slackC[maxC][i] - 1) < dUt) { // is i making A?
				Sa[SaSize] = i;
				SaSize++;
			} else {
				S_Sa[S_SaSize] = i;
				S_SaSize++;
			}

			if ((Math.abs(slackC[minC][i] - 1) > dUt)
				&& Math.abs(slackC[maxC][i] - 1) < dUt) {
				Sa_Sb[Sa_SbSize] = i;
				Sa_SbSize++;
			}
		}

		if (verbose)
			System.out.println("  Sb,Sa... " + SbSize + " " + SaSize);

		int candidateObject = -1;
		int maxHost = 0;
		double aDemand = 0;
		double maxDemand = 0;
		int aHost;
		Random rand = new Random();

		if (verbose)
			System.out.println(
				"  Finding the target Object...for maxC and minC "
					+ maxC
					+ "  "
					+ minC);
		for (i = 0; i < TotalObjects; i++) {
			for (j = 0; j < SaSize; j++) { // is the object assigned to set Sa??
				//get the Host;
				aHost = (int) Sa[j] / 2 + 1;
				// because the first Host is a client
				if (verbose)
					System.out.println(
						"a Host: "
							+ aHost
							+ " an Object: "
							+ Objects[i].code
							+ " a biff: "
							+ Objects[i].biff
							+ " assTo: "
							+ Objects[i].AssignedTo);
				//get the demand
				if ((Objects[i].biff == 0)
					&& (Objects[i].AssignedTo == aHost)) {
					if (j % 2 > 1) {
						aDemand = Objects[i].ScenarioDemand[maxC].CPUdemand;
					} else {
						aDemand = Objects[i].ScenarioDemand[maxC].DISKdemand;
					};
					//			      if (verbose) System.out.println("   Test object "+Objects[i].code +" on Host "+Hosts[aHost].code);
					if (maxDemand < aDemand) {
						maxDemand = aDemand;
						candidateObject = i;
						maxHost = aHost;
					}
				}
			}
		}

		if (candidateObject == -1) {
			// try to grab something though

			for (i = 0; i < TotalObjects; i++) {
				for (j = 0;
					j < S_SaSize;
					j++) { // is the object assigned to set Sa??
					//get the Host;
					aHost = (int) S_Sa[j] / 2 + 1;
					// because the first Host is a client
					if (verbose)
						System.out.println(
							"a Host: "
								+ aHost
								+ " an Object: "
								+ Objects[i].code
								+ " a biff: "
								+ Objects[i].biff
								+ " assTo: "
								+ Objects[i].AssignedTo);
					//get the demand
					if ((Objects[i].biff == 0)
						&& (Objects[i].AssignedTo == aHost)) {
						if (j % 2 > 1) {
							aDemand = Objects[i].ScenarioDemand[maxC].CPUdemand;
						} else {
							aDemand =
								Objects[i].ScenarioDemand[maxC].DISKdemand;
						};
						//			      if (verbose) System.out.println("   Test object "+Objects[i].code +" on Host "+Hosts[aHost].code);
						if (maxDemand < aDemand) {
							maxDemand = aDemand;
							candidateObject = i;
							maxHost = aHost;
						}
					}
				}
			}
		}

		if (candidateObject == -1) {
			if (verbose)
				System.out.println("!!!No Object available...!!! ");
			for (i = 0;
				i < TotalObjects;
				i++) { // return the same configuration
				Objects[i].AssignedE = 1;
				Objects[i].biff = 1;
			}
		} else {

			int targetHost = -1;
			if (verbose)
				System.out.println(
					"  Finding the target Host...for "
						+ Objects[candidateObject].name
						+ "from Host  "
						+ Hosts[maxHost].code);
			if (S_SaSize > 0) {
				// get the target Host
				double minSlack = 2;
				if (verbose)
					System.out.println(
						"  Finding the target Host...among non saturated dev ");
				int flag;
				int d;
				int tempHost = 0;
				for (k = 0;
					k < S_SaSize;
					k++) { // try the hosts nor involved in A
					flag = 1;
					if (verbose)
						System.out.println(
							"   dev..."
								+ S_Sa[k]
								+ " with utilization "
								+ slackC[maxC][S_Sa[k]]);
					if (slackC[maxC][S_Sa[k]] < minSlack) {
						// find the most unloaded 
						tempHost = (int) S_Sa[k] / 2 + 1;
						for (d = 0; d < SaSize; d++) {
							if ((Sa[d] / 2 + 1) == tempHost) {
								flag = 0;
								break;
							};
						};
						if (flag == 1) {
							minSlack = slackC[maxC][S_Sa[k]];
							targetHost = tempHost;
						};
					};
				}
			} else {
				if ((S_SaSize == 0) && (Sa_SbSize > 0)) {
					// get the target Host
					if (verbose)
						System.out.println(
							"   Finding the target Host...among less saturated dev ");
					targetHost =
						(int) Sa_Sb[(rand.nextInt() % Sa_SbSize)] / 2 + 1;
				} else {
					int tried = 0;
					if (verbose)
						System.out.println(
							"   Finding the target Host...among less saturated dev ");

					do {
						targetHost =
							(int) Sa[(rand.nextInt() % SaSize)] / 2 + 1;
						tried++;
					} while (
						(Objects[candidateObject].AssignedTo == targetHost)
							&& (tried < SaSize));
				}
			}; //end else 

			if (targetHost > -1) {
				Objects[candidateObject].AssignedTo = targetHost;
				Objects[candidateObject].biff = 1;
				if (verbose)
					System.out.println(
						"   Assigned Object: "
							+ Objects[candidateObject].name
							+ "  toHost: "
							+ targetHost);
			}

			//make sure that all objects, except the target object are allocated 
			for (i = 0; i < TotalObjects; i++) {
				Objects[i].AssignedE = 1;
				Objects[i].biff = 1;
			};
			Objects[candidateObject].AssignedE = 0;
			// object not assigned yet to an Entity;
		}

		// asssign the objects to entities;
		alocateToEntities();
	}

	public void initialize() {

		//System.out.println("Total Scenarios..."+TotalScenarios);

		for (i = 0; i < TotalScenarios; i++) {
			Rc[i] = 0;
			cursor[i] = 0;
			LimitCursor[i] = 1;
		}

		cursor[TotalScenarios - 1] = 1;
		for (i = 0; i < TotalScenarios; i++) {
			betas[i] = cursor[i]; // update betas;
			// System.out.println(betas[i]);
		}

		//System.out.println("\n");
		// restore the initial values from the backup copy
		for (i = 0; i < TotalEntities; i++) {
			Entities[i].code = EntitiesBackup[i].code;
			//Entities[i].name=EntitiesBackup[i].name;
			Entities[i].Assigned = EntitiesBackup[i].Assigned;
			Entities[i].AssignedToH = EntitiesBackup[i].AssignedToH;
		}

		for (i = 0; i < TotalObjects; i++) {
			Objects[i].biff = Objects[i].biff_Backup;
			Objects[i].AssignedTo = Objects[i].AssignedTo_Backup;
			Objects[i].AssignedE = Objects[i].AssignedE_Backup;
			Objects[i].AssignedToE = Objects[i].AssignedToE_Backup;
			Objects[i].CPUdemand = 0;
			Objects[i].DISKdemand = 0;
			for (j = 0; j < TotalScenarios; j++) {
				Objects[i].CPUdemand += Objects[i].ScenarioDemand[j].CPUdemand
					* betas[j];
				Objects[i].DISKdemand
					+= Objects[i].ScenarioDemand[j].DISKdemand
					* betas[j];
			}
		}

		//System.out.println ("inaninte de visit table\n");
		for (i = 0; i < TotalObjects; i++) {
			for (k = 0; k < TotalObjects; k++) {
				VisitTable[i][k] = 0;
				for (j = 0; j < TotalScenarios; j++) {
					VisitTable[i][k] = (double) ScenarioVisits[j][i][k];
					//*betas[j];
				}
			}
		}

		//System.out.println("dupa de visit table\n");
		for (i = 0; i < TotalObjects; i++)
			for (j = 0; j < TotalObjects; j++)
				if ((VisitTable[i][j] > 0) || (VisitTable[j][i]) > 0)
					L.addPair(
						Objects[i],
						Objects[j],
						VisitTable[i][j] + VisitTable[j][i],
						1);
		CPU_D = 0;
		DISK_D = 0;
		double AvCPURatio = 0;
		double AvDISKRatio = 0;

		for (i = 0; i < TotalHosts; i++) {
			Hosts[i].CPUdemand = 0;
			Hosts[i].DISKdemand = 0;
			for (j = 0; j < TotalScenarios; j++) {
				Hosts[i].scDISKdemand[j] = 0;
				Hosts[i].scCPUdemand[j] = 0;
			}

		}

		for (i = 0; i < TotalObjects; i++) {
			Objects[i].AssignedTo =
				Entities[Objects[i].AssignedToE].AssignedToH;
			if (Objects[i].biff > 0) {
				Hosts[Entities[Objects[i].AssignedToE].AssignedToH].CPUdemand
					+= Objects[i].CPUdemand
					* Hosts[Entities[Objects[i]
						.AssignedToE]
						.AssignedToH]
						.CPURatio;
				Hosts[Entities[Objects[i].AssignedToE].AssignedToH].DISKdemand
					+= Objects[i].DISKdemand
					* Hosts[Entities[Objects[i]
						.AssignedToE]
						.AssignedToH]
						.DISKRatio;
				// cmpute the demand per Host and per scenario;
				for (j = 0; j < TotalScenarios; j++) {

					Hosts[Entities[Objects[i]
						.AssignedToE]
						.AssignedToH]
						.scCPUdemand[j]
						+= Objects[i].ScenarioDemand[j].CPUdemand
						* Hosts[Entities[Objects[i]
							.AssignedToE]
							.AssignedToH]
							.CPURatio;
					Hosts[Entities[Objects[i]
						.AssignedToE]
						.AssignedToH]
						.scDISKdemand[j]
						+= Objects[i].ScenarioDemand[j].DISKdemand
						* Hosts[Entities[Objects[i]
							.AssignedToE]
							.AssignedToH]
							.DISKRatio;
				}

			}
		}

		for (i = 0; i < TotalHosts; i++) {
			if (Hosts[i].server > 0) {
				AvCPURatio += Hosts[i].CPURatio;
				AvDISKRatio += Hosts[i].DISKRatio;
				CPU_D += Hosts[i].CPUdemand;
				DISK_D += Hosts[i].DISKdemand;
			}
		}

		AvCPURatio /= TotalHosts - 1;
		AvDISKRatio /= TotalHosts - 1;
		for (i = 0; i < TotalObjects; i++) {
			if (!(Objects[i].biff > 0)) {
				CPU_D += Objects[i].CPUdemand * AvCPURatio;
				DISK_D += Objects[i].DISKdemand * AvDISKRatio;
			}
		}

		CPU_D = CPU_D / (TotalHosts - 1);
		DISK_D = DISK_D / (TotalHosts - 1);

		buildMatrixA();
		uVector = new double[NoClasses][NoVar];
		//System.out.println( "\nAverage done %f %f %f %f\n ",AvCPURatio,AvDISKRatio,CPU_D,DISK_D);
		//getchar();
		if ((typeAlocation.equals("VL")) || (typeAlocation.equals("HL"))) {
			bVector = new double[noServersAndDelays];
			for (i = 0; i < noServersAndDelays; i++)
				bVector[i] = 1;
		}

	}

	protected void updateHostsDemands() {

		for (i = 0; i < TotalHosts; i++) {
			Hosts[i].CPUdemand = 0;
			Hosts[i].DISKdemand = 0;
			for (j = 0; j < TotalScenarios; j++) {
				Hosts[i].scDISKdemand[j] = 0;
				Hosts[i].scCPUdemand[j] = 0;

			}

		}

		for (i = 0; i < TotalObjects; i++) {
			Hosts[Entities[Objects[i].AssignedToE].AssignedToH].CPUdemand
				+= Objects[i].CPUdemand
				* Hosts[Entities[Objects[i].AssignedToE].AssignedToH].CPURatio;
			Hosts[Entities[Objects[i].AssignedToE].AssignedToH].DISKdemand
				+= Objects[i].DISKdemand
				* Hosts[Entities[Objects[i].AssignedToE].AssignedToH].DISKRatio;
			// cmpute the demand per Host and per scenario;
			for (j = 0; j < TotalScenarios; j++) {
				Hosts[Entities[Objects[i]
					.AssignedToE]
					.AssignedToH]
					.scCPUdemand[j]
					+= Objects[i].ScenarioDemand[j].CPUdemand
					* Hosts[Entities[Objects[i]
						.AssignedToE]
						.AssignedToH]
						.CPURatio;
				Hosts[Entities[Objects[i]
					.AssignedToE]
					.AssignedToH]
					.scDISKdemand[j]
					+= Objects[i].ScenarioDemand[j].DISKdemand
					* Hosts[Entities[Objects[i]
						.AssignedToE]
						.AssignedToH]
						.DISKRatio;
			}

		}

		// build the entity/host/class matrix

		updateNetDemands();
		buildMatrixA();

	}

	protected void initializeMva() {

		double aD[][] = new double[TotalHosts * 2 + TotalNetworks][NoClasses];
		int type[] = new int[TotalHosts * 2 + TotalNetworks];

		int offs = 0;

		for (i = 0; i < TotalHosts; i++) {
			type[offs] = Hosts[i].server;
			type[offs + 1] = Hosts[i].server;
			for (j = 0; j < NoClasses; j++) {
				aD[offs][j] = Hosts[i].scCPUdemand[j];
				aD[offs + 1][j] = Hosts[i].scDISKdemand[j];

			}
			offs = offs + 2;

		}

		for (i = 0; i < TotalNetworks; i++) {
			for (j = 0; j < NoClasses; j++) {
				aD[TotalHosts * 2 + i][j] = Networks[i].Rkc[j];
			}
		}
		for (i = 0; i < TotalNetworks; i++)
			type[TotalHosts * 2 + i] = 2; // delay centers 

		aMva =
			new Mva(NoClasses, TotalHosts * 2 + TotalNetworks, Nc, Z, aD, type);

	}

	protected void initializeModelSolver() {

		double aD[][] = new double[TotalHosts * 2 + TotalNetworks][NoClasses];
		int htype[] = new int[TotalHosts * 2 + TotalNetworks];
		int stype[] = new int[TotalEntities];
		aDpkc =
			new double[TotalEntities][TotalHosts * 2
				+ TotalNetworks][NoClasses];

		for (int i = 0; i < TotalEntities; i++)
			for (int j = 0; j < TotalHosts; j++)
				for (int c = 0; c < NoClasses; c++)
					aDpkc[i][j][c] = 0;

		for (i = 0; i < TotalObjects; i++) {
			int p = Objects[i].AssignedToE;
			int k = Entities[Objects[i].AssignedToE].AssignedToH;
			for (int c = 0; c < NoClasses; c++) {

				aDpkc[p][k
					* 2][c] += Objects[i].ScenarioDemand[c].CPUdemand
					* Hosts[Entities[Objects[i]
						.AssignedToE]
						.AssignedToH]
						.CPURatio;
				aDpkc[p][k * 2
					+ 1][c] += Objects[i].ScenarioDemand[c].DISKdemand
						* Hosts[Entities[Objects[i]
							.AssignedToE]
							.AssignedToH]
							.DISKRatio;
			}

		}

		for (i = 0; i < TotalNetworks; i++) {
			for (j = 0; j < NoClasses; j++) {
				for (int p = 0; p < TotalEntities; p++)
					aDpkc[p][2 * TotalHosts + i][j] =
						Networks[i].Rkc[j] / TotalEntities;
			}
		}
		int[] smult = new int[TotalEntities];
		int[] hmult = new int[TotalHosts * 2 + TotalNetworks];

		for (i = 0; i < TotalHosts; i++) {
			htype[2 * i] = Hosts[i].server;
			hmult[2 * i] = Hosts[i].CPUMultiplicity;
			htype[2 * i + 1] = Hosts[i].server;
			hmult[2 * i + 1] = Hosts[i].diskMultiplicity;
		}

		for (i = 0; i < TotalNetworks; i++) {

			htype[TotalHosts * 2 + i] = 2; // delay centers 
			hmult[TotalHosts * 2 + i] = 1;
		}
		for (i = 0; i < TotalEntities; i++) {

			stype[i] = Entities[i].server; // delay centers 
			smult[i] = Entities[i].multiplicity;
		}

		ms =
			new ModelSolver(
				TotalHosts * 2 + TotalNetworks,
				NoClasses,
				TotalEntities,
				htype,
				hmult,
				stype,
				smult,
				Nc,
				Z,
				aDpkc);

	}

	DOMParser getParser()
	{
		if (parser != null)
			return parser;
		else
		{
			try
			{
				parser = new DOMParser();
				
				parser.setFeature ("http://apache.org/xml/features/dom/defer-node-expansion", true);
				parser.setFeature ("http://xml.org/sax/features/validation", true);
				parser.setFeature ("http://xml.org/sax/features/namespaces", true);
				parser.setFeature ("http://apache.org/xml/features/validation/schema", true);
				parser.setErrorHandler(new opera.Core.dom.OperaErrorHandler());
				parser.setEntityResolver(new opera.Core.dom.PxlEntityResolver());
			}
			catch (org.xml.sax.SAXNotRecognizedException ex) { }
			catch (org.xml.sax.SAXNotSupportedException  ex) { }
		}
		return parser;
	}

	public Document parsePXL(String uri)
	{
		Document doc = null;

		try
		{	getParser().parse(uri);
			doc = getParser().getDocument();
		}
		catch (org.xml.sax.SAXException se)
		{
			throw new LQMException(se.getMessage());
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}

		return doc;
	}
	
	public Document parsePxlFromString(String pxlModel)
	{
		Document doc = null;
		
		try
		{
			InputSource is = new InputSource(new StringReader(pxlModel));
			getParser().parse(is);
			doc = getParser().getDocument();
		}
		catch (org.xml.sax.SAXException se)
		{
			throw new LQMException(se.getMessage());
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
		
		return doc;
	}

	/*
	public static void main(String argv[])
	{
		if (argv.length < 2)
		{
			System.out.println("Usage: main in_file_name out_file_name option");
			return;
		}
		try
		{
			solvePXL(argv);
		}
		catch (LQMException ex)
		{
			ex.printStackTrace();
			System.out.flush();
			return;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.out.flush();
			return;
		}
	}

	public static void solvePXL(String argv[])
	{
		LQM al = new LQM();
		Document doc = null;

		if (argv.length == 3)
		{
			if (argv[2].equals("verbose"))
			{
				al.verbose = true;
			}
		}

		doc = al.parsePXL(argv[0]);

		al.readAndValidatePxl(doc);

		if (argv.length == 3)
		{
			if (argv[2].equals("parse"))
			{
				return;
			}
		}

		al.initialize();

		// open the output file
		al.setOutDoc(new org.apache.xerces.dom.DocumentImpl());
		al.findConfigurations();
		al.writeResultsDocument(argv[1]);
	}
	*/

	public ArrayList<double[]> getWorstWorkloadMixes()
	{
		return mixes;
	}
	
	// returns the maximum between x and y
	double max(double x, double y)
	{
		if (x < y)
			return y;
		else
			return x;
	}

	public void mediumLoadChangeAlocation() {

	}
	// return minimum between x and y

	double min(double x, double y) {
		if (x < y)
			return x;
		else
			return y;
	}

	public void mlChangeConfiguration() {

	}
	// ordonate a vector
	void ordvector(double vect[], int index[], int size) {
		int i, j;
		int maximum;
		double temp;
		int tempindex;
		for (i = 0; i < size; i++)
			index[i] = i;
		for (i = 0; i < size; i++) {
			maximum = i;
			for (j = i; j < size; j++)
				if (vect[maximum] < vect[j])
					maximum = j;

			temp = vect[i];
			tempindex = index[i];
			vect[i] = vect[maximum];
			index[i] = index[maximum];
			vect[maximum] = temp;
			index[maximum] = tempindex;
		}
	}

	public double overAllSatisfaction() {

		double S = 2;
		for (int j = 0;
			j < NoClasses;
			j++) { // first NoClasses objects are clients
			if (Rc[j] > Rc_max[j])
				Satisfaction[j] = 0;
			else if (Rc[j] <= Rc_min[j])
				Satisfaction[j] = 1;
			else
				Satisfaction[j] =
					1 - ((Rc[j] - Rc_min[j]) / (Rc_max[j] - Rc_min[j]));
			if (Satisfaction[j] < S)
				S = Satisfaction[j];

		};

		if (verbose)
			System.out.println("Satisfaction...." + S);
		return S;

	}

	/*
	public void printConfigurations()
	{
		int max = getBestConfIndex();

		try
		{
			fpoConf.print("\n");
			fpoConf.print("PerScenarioSatisfaction\n");
			fpoConf.print("IndexConf");
			for (i = 0; i < NoClasses; i++)
			{
				fpoConf.print("\t Scenario " + scenarioNames[i]);
			}
			fpoConf.print("\n");

			for (indexConfig = 0; indexConfig <= max; indexConfig++)
			{
				fpoConf.print(indexConfig + "\t  ");
				for (int k = 0; k < NoClasses; k++)
				{
					fpoConf.print(ObjConfig.Sat[k][indexConfig] + "\t");
				}
				fpoConf.print("\n");
			}

			fpoConf.print("\n");
			fpoConf.print("\n");

			fpoConf.print("PerScenarioLargestResponseTimes\n");
			fpoConf.print("IndexConf");
			for (i = 0; i < NoClasses; i++)
			{
				fpoConf.print("\t Scenario " + scenarioNames[k]);
			}
			fpoConf.print("\n");

			for (indexConfig = 0; indexConfig <= max; indexConfig++)
			{
				fpoConf.print(indexConfig + "\t");
				for (int k = 0; k < NoClasses; k++)
				{
					fpoConf.print(ObjConfig.Rc[k][indexConfig] + "\t");
				}
				fpoConf.print("\n");
			}
			fpoConf.print("\n");
			fpoConf.print("\n");

			fpoConf.print("Allocations\n");

			for (indexConfig = 0; indexConfig <= max; indexConfig++)
			{
				fpoConf.print("LQM " + indexConfig + "\n");
				fpoConf.print("ServiceName \tAssignedToEntity\t ServiceMaximumUtilization \n");

				for (i = 0; i < TotalObjects; i++)
				{
					fpoConf.print(Objects[i].name);
					fpoConf.print(" \t");
					fpoConf.print(Entities[ObjConfig.allocation[indexConfig][i]].name);
					if (indexConfig == max)
					{
						fpoConf.print("\t");
						fpoConf.print(ObjConfig.Uo[indexConfig][i]);
					}
					fpoConf.print("\n");
				}
			}
			fpoConf.print("\n");
			fpoConf.print("\n");
		}
		catch (Exception ex) { }
	}
	*/

	public void createResultsDocument() {
		recomendedArch = null;
		int max = getBestConfIndex();
		try {

			Element root = outDoc.createElement("Results");
			outDoc.appendChild(root);
			for (indexConfig = 0; indexConfig <= max; indexConfig++) {
				Element arch = outDoc.createElement("Architecture");
				root.appendChild(arch);
				if (indexConfig == max) {
					//arch.setAttribute("recomend", "true");
					recomendedArch = arch;
					/*	
						for (i = 0; i < TotalEntities; i++) {
									Element cont = outDoc.createElement("Container");
									arch.appendChild(cont);
									cont.setAttribute("name", Entities[i].name);
									cont.setAttribute("Utilization", String.valueOf(ms.getSUp(i)));
									cont.setAttribute("multiplicity", String.valueOf(Entities[i].multiplicity));
					
									for (int j = 0; j < NoClasses; j++) {
										Element sc = outDoc.createElement("PerScenarioResponseTime");
										cont.appendChild(sc);
										sc.setAttribute("scenario", scenarioNames[j]);
										sc.setAttribute("value", String.valueOf(ms.getSRpc(i,j)));
					
									}
								}	
					*/
				}
				arch.setAttribute("name", String.valueOf(indexConfig));
				for (i = 0; i < TotalObjects; i++) {
					Element obj = outDoc.createElement("Service");
					arch.appendChild(obj);
					obj.setAttribute("name", Objects[i].name);
					obj.setAttribute(
						"allocatedToContainer",
						Entities[ObjConfig.allocation[indexConfig][i]].name);
					if (indexConfig == max) {
						obj.setAttribute(
							"MaxUtilization",
							String.valueOf(ObjConfig.Uo[indexConfig][i]));

					}
				}
				for (int k = 0; k < NoClasses; k++) {
					Element met = outDoc.createElement("ExtremeMetrics");
					arch.appendChild(met);
					met.setAttribute("scenario", scenarioNames[k]);
					Element rt = outDoc.createElement("MaxResponseTime");
					met.appendChild(rt);
					rt.setAttribute(
						"value",
						String.valueOf(ObjConfig.Rc[k][indexConfig]));
					Element sat = outDoc.createElement("MinSatisfaction");
					met.appendChild(sat);
					sat.setAttribute(
						"value",
						String.valueOf(ObjConfig.Sat[k][indexConfig]));
				}
			}

		} catch (Exception ex) {
			System.out.println("Error in writting:");
			ex.printStackTrace();
		}

	}

	public void readAndValidatePxl(Document doc)
	{
		Workloads = new Vector<double[]>();
		NodeList Objs = null;
		NodeList PerClassVisits = null;
		NodeList Hsts = null;
		NodeList Containers = null;
		NodeList Middlware = null;
		NodeList Nets = null;
		NodeList Workload = null;
		NodeList ThinkTimes = null;
		NodeList ResponseTimes = null;
		NodeList MixOfInterest = null;
		Element Population = null;

		//1. Get the Objects node
		try {
			Objs = doc.getElementsByTagName("Service");
			PerClassVisits = doc.getElementsByTagName("Scenario");
			Hsts = doc.getElementsByTagName("Node");
			Containers = doc.getElementsByTagName("Container");
			Middlware = doc.getElementsByTagName("Middlware");
			Nets = doc.getElementsByTagName("Network");
			Workload = doc.getElementsByTagName("Workloads");
			ThinkTimes = doc.getElementsByTagName("ThinkTime");
			ResponseTimes = doc.getElementsByTagName("ResponseTime");
			MixOfInterest = doc.getElementsByTagName("WorkloadMixes");
			Population =
				(Element) doc.getElementsByTagName("Users").item(0);

			TotalObjects = Objs.getLength();
			TotalScenarios = PerClassVisits.getLength();
			TotalHosts = Hsts.getLength();
			TotalEntities = Containers.getLength();
			TotalMidwares = Middlware.getLength();
			TotalNetworks = Nets.getLength();

			NoClasses = TotalScenarios;
			scenarioNames = new String[TotalScenarios];
			scenarioTriggers= new int[TotalScenarios];
			for (j = 0; j < TotalScenarios; j++) {
				scenarioNames[j] = new String();
				scenarioNames[j] =
					PerClassVisits
						.item(j)
						.getAttributes()
						.getNamedItem("name")
						.getNodeValue();
			}
			
			try {
				Objects = new DistributedObject[TotalObjects];
			} catch (NullPointerException ex) {
				System.out.println("null pointer - creating objects...");
				return;
			}

			// initialize the objects
			for (i = 0; i < TotalObjects; i++)
				Objects[i] = new DistributedObject(this);
			//  allocate memory for scenario Demands

			if ((TVisits = new double[TotalObjects]) == null) {
				System.out.println("not enough memory");
				return;
			}

			if ((indexO = new int[TotalObjects]) == null) {
				System.out.println("not enough memory");
				return;
			}

			if ((ScenarioVisits =
				new double[TotalScenarios][TotalObjects][TotalObjects])
				== null) {
				System.out.println("not enough memory");
				return;
			}
			if ((ScenarioVisitType =
				new String[TotalScenarios][TotalObjects][TotalObjects])
				== null) {
				System.out.println("not enough memory");
				return;
			}
			if ((ScenarioMessages =
				new double[TotalScenarios][TotalObjects][TotalObjects])
				== null) {
				System.out.println("not enough memory");
				return;
			}

			if ((VisitTable = new double[TotalObjects][TotalObjects])
				== null) {
				System.out.println("not enough memory");
				return;
			}

			//initialize visit table per scenario
			for (k = 0; k < TotalScenarios; k++) {
				for (i = 0; i < TotalObjects; i++)
					for (j = 0; j < TotalObjects; j++) {
						ScenarioVisits[k][i][j] = 0;
						ScenarioMessages[k][i][j] = 0;
						ScenarioVisitType[k][i][j] = "s";
					}

			}

			// initialize total visit table 
			for (i = 0; i < TotalObjects; i++)
				for (j = 0; j < TotalObjects; j++)
					VisitTable[i][j] = (double) 0.0;

			//read the object's demands

			for (i = 0; i < TotalObjects; i++)
				try {
					Objects[i].ScenarioDemand =
						new ScenarioDemands[TotalScenarios];
					for (j = 0; j < TotalScenarios; j++) {
						Objects[i].ScenarioDemand[j] =
							new ScenarioDemands(this);
						Objects[i].Roc = new double[TotalScenarios];
						Objects[i].Uoc = new double[TotalScenarios];
						Objects[i].Xoc = new double[TotalScenarios];

					}
				} catch (NullPointerException ex) {
					System.out.println(
						" null pointer exception" + i + " - not enough memory");
					return;
				}
			/*
			*reads the object information: 
			*   code, name, (per scenario cpuDemand,per scenario DISKDemand)*,
			*   #visits, assignedToHost, hostID,AssignedtoEntity, entityID
			*/

			for (i = 0; i < TotalObjects; i++) {
				Objects[i].code = i;
				//          System.out.println(" Object, code "+ Objects[i].code);
				Objects[i].name =
					Objs
						.item(i)
						.getAttributes()
						.getNamedItem("name")
						.getNodeValue();
				//          System.out.println(" Object, code "+ Objects[i].name+' ' +Objects[i].code);
				Objects[i].CPUdemand = 0;
				Objects[i].DISKdemand = 0;

				// preassigned objects???
				if (Objs
					.item(i)
					.getAttributes()
					.getNamedItem("canMigrate")
					.getNodeValue()
					.equals("true"))
					Objects[i].biff_Backup = 0;
				else
					Objects[i].biff_Backup = 1;

				if (Objects[i].biff_Backup > 0)
					Objects[i].AssignedE_Backup = 1;
				else
					Objects[i].AssignedE_Backup = 0;

				
			};
			// is there any objects that can be moved around?
			for (i = 0; i < TotalObjects; i++) {
				if (Objects[i].biff_Backup == 0) {
					loadBalancing = true;
					break;
				};

			}

			//decide what are the client Objects
			
			for (int j = 0; j < TotalScenarios; j++) {
				String triggerObject=
					PerClassVisits
						.item(j)
						.getAttributes()
						.getNamedItem("triggeredByService")
						.getNodeValue();
				//find if the object is defined
				boolean found= false;
				int objectIndex=99999;
				
				for (int i = 0; i < TotalObjects; i++) {
					if (Objects[i].name.equals(triggerObject)){
						found=true;
						objectIndex=i;
						break;
					}
				}
				
				if (!found){
						throw new LQMException(
								"Incorrect Specification: triggerObject:"+triggerObject+" is not defined");
					}
				else scenarioTriggers[j]=objectIndex;
				

			}
			
			// read visits per scenario

			for (k = 0; k < TotalScenarios; k++) {
				int classIndex =
					scenarioIndexOf(
						PerClassVisits
							.item(k)
							.getAttributes()
							.getNamedItem("name")
							.getNodeValue());
				NodeList visits =
					((Element) PerClassVisits.item(k)).getElementsByTagName(
						"Call");

				for (int m = 0; m < visits.getLength(); m++) {
					int caller =
						objectIndexOf(
							visits
								.item(m)
								.getAttributes()
								.getNamedItem("caller")
								.getNodeValue());
					int callee =
						objectIndexOf(
							visits
								.item(m)
								.getAttributes()
								.getNamedItem("callee")
								.getNodeValue());

					if ((caller > TotalObjects) | (callee > TotalObjects)) {
						throw new LQMException(
							"Incorrect Specification: caller and callee("
								+ visits
									.item(m)
									.getAttributes()
									.getNamedItem("caller")
									.getNodeValue()
								+ " "
								+ visits
									.item(m)
									.getAttributes()
									.getNamedItem("callee")
									.getNodeValue()
								+ " ) should be object names");

					}
					ScenarioVisits[classIndex][caller][callee] =
						Float
							.valueOf(
								visits
									.item(m)
									.getAttributes()
									.getNamedItem("invocations")
									.getNodeValue())
							.doubleValue();
//read the demand...
					
					Node Demands =((Element) visits.item(m)).getElementsByTagName("Demand").item(0);
	                             
	                // assume that k is in the right range
	                    try {
	                        Objects[callee].ScenarioDemand[k].CPUdemand +=
	                            Float
	                                .valueOf(
	                                    Demands
	                                        .getAttributes()
	                                        .getNamedItem("CPUDemand")
	                                        .getNodeValue())
	                                .doubleValue();
	                    } catch (Exception ex) {
	                    };

	                    try {
	                        Objects[callee].ScenarioDemand[k].DISKdemand +=
	                            Float
	                                .valueOf(
	                                    Demands
	                                        .getAttributes()
	                                        .getNamedItem("DiskDemand")
	                                        .getNodeValue())
	                                .doubleValue();
	                    } catch (Exception ex) {
	                    };
	                

//finished with the demand
					
					
					// the messages are optional...the default value is 8
					try {
						ScenarioMessages[classIndex][caller][callee] =
							Float
								.valueOf(
									visits
										.item(m)
										.getAttributes()
										.getNamedItem("bytesSent")
										.getNodeValue())
								.doubleValue();

					} catch (Exception ex) {
						ScenarioMessages[classIndex][caller][callee] = 8;
					}

					try {
						ScenarioMessages[classIndex][caller][callee]
							+= Float
								.valueOf(
									visits
										.item(m)
										.getAttributes()
										.getNamedItem("bytesReceived")
										.getNodeValue())
								.doubleValue();

					} catch (Exception ex) {
						ScenarioMessages[classIndex][caller][callee] += 8;
					}

					try {
						ScenarioVisitType[classIndex][caller][callee] =
							visits
								.item(m)
								.getAttributes()
								.getNamedItem("type")
								.getNodeValue();
					} catch (Exception ex) { /* do nothing*/
					};

				}
			}

			if ((Hosts = new Host[TotalHosts]) == null) {
				System.out.println("not enough memory");
				return;
			}

			for (i = 0; i < TotalHosts; i++)
				Hosts[i] = new Host();

			if ((Entities = new Entity[TotalEntities]) == null) {
				System.out.println("not enough memory");
				return;
			}

			Networks = new Network[TotalNetworks];
			Midwares = new Midleware[TotalMidwares];
			for (i = 0; i < TotalNetworks; i++)
				Networks[i] = new Network();
			for (i = 0; i < TotalMidwares; i++)
				Midwares[i] = new Midleware();

			for (i = 0; i < TotalEntities; i++)
				Entities[i] = new Entity();

			if ((EntitiesBackup = new Entity[TotalEntities]) == null) {
				System.out.println("not enough memory");
				return;
			}

			if (verbose)
				System.out.println(
					"Hosts and Containers: "
						+ TotalHosts
						+ " "
						+ TotalEntities);

			for (i = 0; i < TotalEntities; i++)
				EntitiesBackup[i] = new Entity();

			/*read the Host information and the demand placed on each resource (disk + CPU )
				  id, name, cpudemand, cpuratio, diskDemand, diskRatio, server/client
			*/
			noClients = 0;
			for (i = 0; i < TotalHosts; i++) {
				Hosts[i].code = i;
				Hosts[i].name =
					Hsts
						.item(i)
						.getAttributes()
						.getNamedItem("name")
						.getNodeValue();
				;
				Hosts[i].CPUdemand = 0;
				Hosts[i].CPURatio =
					Float
						.valueOf(
							Hsts
								.item(i)
								.getAttributes()
								.getNamedItem("CPURatio")
								.getNodeValue())
						.doubleValue();
				Hosts[i].DISKdemand = 0;
				Hosts[i].DISKRatio =
					Float
						.valueOf(
							Hsts
								.item(i)
								.getAttributes()
								.getNamedItem("DiskRatio")
								.getNodeValue())
						.doubleValue();
				Hosts[i].server = 1; // by default the host is server;
				try {
					Hosts[i].CPUMultiplicity =
						Integer
							.valueOf(
								Hsts
									.item(i)
									.getAttributes()
									.getNamedItem("CPUParallelism")
									.getNodeValue())
							.intValue();
				} catch (Exception ex) { /*do nothing*/
				};
				try {
					Hosts[i].diskMultiplicity =
						Integer
							.valueOf(
								Hsts
									.item(i)
									.getAttributes()
									.getNamedItem("DiskParallelism")
									.getNodeValue())
							.intValue();
				} catch (Exception ex) { /*do nothing*/
				};

				try {
					if (Hsts
						.item(i)
						.getAttributes()
						.getNamedItem("type")
						.getNodeValue()
						.equals("client")) {
						Hosts[i].server = 0;
						noClients++;
					} else if (
						Hsts
							.item(i)
							.getAttributes()
							.getNamedItem("type")
							.getNodeValue()
							.equals(
							"delay")) {
						Hosts[i].server = 2;
					}
				} catch (Exception ex) {
				};
				// the "type" is optional, so we can get an exception here; do nothing

				// for each Host, create a list to hold all the objects		 
				Hosts[i].List = new int[TotalObjects];
				// there are no objects assigned yet 
				Hosts[i].ObjAssigned = 0;

			}
			if (noClients == 0) {
				throw new LQMException("The model should have at least one node of type 'client' ");

			}

			/*read the Entity information, refered as Container in the XML file
				  id, name, cpudemand, cpuratio, diskDemand, diskRatio, server/client
			*/

			for (i = 0; i < TotalEntities; i++) {
				EntitiesBackup[i].code = i;
				Entities[i].code = i;
				Entities[i].name =
					Containers
						.item(i)
						.getAttributes()
						.getNamedItem("name")
						.getNodeValue();
				if (Containers
					.item(i)
					.getAttributes()
					.getNamedItem("server")
					.getNodeValue()
					.equals("false"))
					Entities[i].server = 0;
				else
					Entities[i].server = 1;
				try {
					Entities[i].multiplicity =
						Integer
							.valueOf(
								Containers
									.item(i)
									.getAttributes()
									.getNamedItem("parallelism")
									.getNodeValue())
							.intValue();
				} catch (Exception ex) { /*do nothing*/
				};

				if (verbose)
					System.out.println(
						"Container : " + (i) + " name is: " + Entities[i].name);

			}

			/*read the Net information, 
				  id, name, latency, overheadperByte, the hosts it connects
			*/

			for (i = 0; i < TotalNetworks; i++) {
				Networks[i].code = i;
				Networks[i].name =
					Nets
						.item(i)
						.getAttributes()
						.getNamedItem("name")
						.getNodeValue();
				Networks[i].latency =
					Float
						.valueOf(
							Nets
								.item(i)
								.getAttributes()
								.getNamedItem("latency")
								.getNodeValue())
						.doubleValue();
				Networks[i].msPerByte =
					Float
						.valueOf(
							Nets
								.item(i)
								.getAttributes()
								.getNamedItem("overheadPerByte")
								.getNodeValue())
						.doubleValue();
				String allHosts =
					Nets
						.item(i)
						.getAttributes()
						.getNamedItem("connectsNodes")
						.getNodeValue();
				if (verbose)
					System.out.println(allHosts);
				Networks[i].hosts = parseLine(allHosts);
			}

			/*read the Middlware information, 
				  id, name, latency, ...
			*/
			for (i = 0; i < TotalMidwares; i++) {
				Midwares[i].code = i;
				Midwares[i].name =
					Middlware
						.item(i)
						.getAttributes()
						.getNamedItem("name")
						.getNodeValue();
				Midwares[i].latencySend =
					Float
						.valueOf(
							Middlware
								.item(i)
								.getAttributes()
								.getNamedItem("fixedOverheadSend")
								.getNodeValue())
						.doubleValue();
				Midwares[i].latencyReceive =
					Float
						.valueOf(
							Middlware
								.item(i)
								.getAttributes()
								.getNamedItem("fixedOverheadReceive")
								.getNodeValue())
						.doubleValue();
				Midwares[i].msPerByteSent =
					Float
						.valueOf(
							Middlware
								.item(i)
								.getAttributes()
								.getNamedItem("overheadPerByteSent")
								.getNodeValue())
						.doubleValue();
				Midwares[i].msPerByteReceived =
					Float
						.valueOf(
							Middlware
								.item(i)
								.getAttributes()
								.getNamedItem("overheadPerByteReceived")
								.getNodeValue())
						.doubleValue();
			}

			TotalObjConfigurations = TotalObjects - 1;
			ObjConfig.Number = 0;
			indexConfig = 0;
			NoClasses = TotalScenarios;
			noServersAndDelays = (TotalHosts - noClients) * 2;
			NoVar = noServersAndDelays + NoClasses;
			if ((hostType = new int[noServersAndDelays]) == null) {
				System.out.println("not enough memory");
				return;
			}

			/* read the workload*/

			typeAlocation =
				Workload
					.item(0)
					.getAttributes()
					.getNamedItem("kind")
					.getNodeValue();
			;

			//this shoudn't happen
			if ((!typeAlocation.equals("VL"))
				&& (!typeAlocation.equals("HL"))
				&& (!typeAlocation.equals("ML"))) {
				throw new LQMException(
					" Incorect Specification:"
						+ typeAlocation
						+ " Workloads 'kind' should be HL, VL or ML");

			}

			if ((typeAlocation.equals("VL")) || (typeAlocation.equals("HL")))
				NoCustomers =
					Integer
						.valueOf(Population.getFirstChild().getNodeValue())
						.intValue();

			// assume that the customers are evenly distributed accross the scenarioes
			Nc = new double[NoClasses];
			for (i = 0; i < NoClasses; i++) {
				Nc[i] = NoCustomers / NoClasses;
			}

			if (ThinkTimes.getLength() != TotalScenarios) {
				throw new LQMException(
					" Incorect Specification:there are "
						+ ThinkTimes.getLength()
						+ " think times and "
						+ TotalScenarios
						+ "scenarios. They should be one think time for each scenario");

			}

			Z = new double[NoClasses];
			for (i = 0; i < NoClasses; i++) {
				int k =
					scenarioIndexOf(
						ThinkTimes
							.item(i)
							.getAttributes()
							.getNamedItem("scenario")
							.getNodeValue());
				if (k >= TotalScenarios) {
					throw new LQMException(
						" Incorect Specification: scenario \""
							+ ThinkTimes
								.item(i)
								.getAttributes()
								.getNamedItem("scenario")
								.getNodeValue()
							+ "\" is used in ThinkTimes but it is not used in Scenarios");

				}

				// assume that k is in the right range
				Z[k] =
					Float
						.valueOf(
							ThinkTimes
								.item(i)
								.getAttributes()
								.getNamedItem("time")
								.getNodeValue())
						.doubleValue();
			}

			//read the requirements		  
			Rc_min = new double[NoClasses];
			Rc_max = new double[NoClasses];

			for (i = 0; i < NoClasses; i++) {
				int k =
					scenarioIndexOf(
						ResponseTimes
							.item(i)
							.getAttributes()
							.getNamedItem("scenario")
							.getNodeValue());

				if (k >= TotalScenarios) {
					throw new LQMException(
						" Incorect Specification: scenario \""
							+ ResponseTimes
								.item(i)
								.getAttributes()
								.getNamedItem("scenario")
								.getNodeValue()
							+ "\" is used in ResponseTimes but it is not used in Scenarios");

				}

				Rc_min[k] =
					Float
						.valueOf(
							ResponseTimes
								.item(k)
								.getAttributes()
								.getNamedItem("minResponseTime")
								.getNodeValue())
						.doubleValue();
				Rc_max[k] =
					Float
						.valueOf(
							ResponseTimes
								.item(k)
								.getAttributes()
								.getNamedItem("maxResponseTime")
								.getNodeValue())
						.doubleValue();
			}

			workloadType= new boolean [MixOfInterest.getLength()];
			// read the mixes of interest
            
			for (int i = 0; i < MixOfInterest.getLength(); i++)
			{
				double Nc[] = new double[NoClasses];
				boolean openModel = false;
				NodeList mixes =
					((Element) MixOfInterest.item(i)).getElementsByTagName(
						"Mix");
						
				try {
					openModel =
						Boolean
							.valueOf(
								MixOfInterest
									.item(i)
									.getAttributes()
									.getNamedItem("openModel")
									.getNodeValue())
							.booleanValue();
				} catch (Exception ex) {
				};
				workloadType[i]=openModel;

				if (mixes.getLength() != NoClasses) {
					throw new LQMException(
						" Incorect Specification:there are "
							+ mixes.getLength()
							+ " scenarios in Mixes"
							+ NoClasses
							+ "scenarios in Scenario element. They should be in the same number");

				}

				for (j = 0; j < NoClasses; j++) {
					int k =
						scenarioIndexOf(
							mixes
								.item(j)
								.getAttributes()
								.getNamedItem("scenario")
								.getNodeValue());

					if (k >= TotalScenarios) {
						throw new LQMException(
							" Incorect Specification: scenario \""
								+ mixes
									.item(i)
									.getAttributes()
									.getNamedItem("scenario")
									.getNodeValue()
								+ "\" is used in Mix but it is not used in Scenarios");

					}
					// assume that k is in the right range
					Nc[k] =
						Double.valueOf(
							mixes
								.item(j)
								.getAttributes()
								.getNamedItem("load")
								.getNodeValue());
					;
				}
				Workloads.add(Nc);
			}

			/*******************************allocation********************/
			for (i = 0; i < TotalObjects; i++) {

				int k =
					entityIndexOf(
						Objs
							.item(i)
							.getAttributes()
							.getNamedItem("runsInContainer")
							.getNodeValue());
				if (k > TotalEntities) {
					throw new LQMException(
						" Incorect Specification:Object "
							+ Objs
								.item(i)
								.getAttributes()
								.getNamedItem("name")
								.getNodeValue()
							+ " should be assigned to a defined container; "
							+ Objs
								.item(i)
								.getAttributes()
								.getNamedItem("runsInContainer")
								.getNodeValue()
							+ " was not defined as a container name");
				}

				Objects[i].AssignedToE_Backup = k;
			}

			for (i = 0; i < TotalEntities; i++) {
				// preassign containers???
				if (Containers
					.item(i)
					.getAttributes()
					.getNamedItem("canMigrate")
					.getNodeValue()
					.equals("true"))
					EntitiesBackup[i].Assigned = 0;
				else
					EntitiesBackup[i].Assigned = 1;

				int k =
					hostIndexOf(
						Containers
							.item(i)
							.getAttributes()
							.getNamedItem("runsOnNode")
							.getNodeValue());

				if (k > TotalHosts) {
					throw new LQMException(
						" Incorect Specification:Object "
							+ Containers
								.item(i)
								.getAttributes()
								.getNamedItem("name")
								.getNodeValue()
							+ " should be assigned to a defined Node; "
							+ Containers
								.item(i)
								.getAttributes()
								.getNamedItem("assignedToHost")
								.getNodeValue()
							+ " was not defined as a Node name");
				}

				EntitiesBackup[i].AssignedToH = k;
			}
			// check if the entities should be allocated 
			for (i = 0; i < TotalEntities; i++) {
				if (EntitiesBackup[i].Assigned == 0) {
					loadBalancing = true;
					break;
				}

			}

			for (i = 0; i < TotalObjects; i++)
				Objects[i].AssignedTo_Backup =
					EntitiesBackup[Objects[i].AssignedToE_Backup].AssignedToH;

			/**********************************************************************************************/

		} catch (LQMException e) {
			throw new LQMException(e.getMessage());
		}

	}

	protected int networkOf(int o1, int o2) {
		//returns the Network shared by o1 and o2);
		int host1 = Objects[o1].AssignedTo;
		int host2 = Objects[o2].AssignedTo;
		if (host1 == host2)
			return -1; // do not communicate through a net
		int net = -2;
		for (int i = 0; i < TotalNetworks; i++) {
			for (j = 0; j < Networks[i].hosts.length; j++) {
				if (Networks[i].hosts[j] == host1) {
					// host1 is connected to net i
					for (k = 0; k < Networks[i].hosts.length; k++) {
						if (Networks[i].hosts[k] == host2) {
							// Host 2 is connected to net k
							net = i; // mark this event in your calendar
						}
					}
				}
			}
			if (net > -1)
				break; //stop at the first occurence
		}
		return net;
	}

	void updateNetDemands() {

		for (int i = 0; i < TotalNetworks; i++) {
			for (int j = 0; j < TotalScenarios; j++) {
				Networks[i].Rkc[j] = Networks[i].latency;
			}
		}

		for (int i = 0; i < TotalNetworks; i++) {
			for (int j = 0; j < TotalScenarios; j++)
				for (int k = 0; k < TotalObjects; k++)
					for (int l = 0; l < TotalObjects; l++)
						if ((ScenarioVisits[j][k][l] > 0)
							&& (networkOf(k, l) == i))
							Networks[i].Rkc[j]
								+= (ScenarioVisits[j][k][l]
									* ScenarioMessages[j][k][l])
								* Networks[i].msPerByte;

		}
	}

	private void recovery() {
		for (i = 0; i < TotalEntities; i++) {
			Entities[i].code = EntitiesBackup[i].code;
			//         Entities[i].name=EntitiesBackup[i].name;
			Entities[i].Assigned = EntitiesBackup[i].Assigned;
			Entities[i].AssignedToH = EntitiesBackup[i].AssignedToH;
		}

		for (i = 0; i < TotalObjects; i++) {
			Objects[i].biff = Objects[i].biff_Backup;
			Objects[i].AssignedTo = Objects[i].AssignedTo_Backup;
			Objects[i].AssignedE = Objects[i].AssignedE_Backup;
			Objects[i].AssignedToE = Objects[i].AssignedToE_Backup;
			Objects[i].CPUdemand = 0;
			Objects[i].DISKdemand = 0;
			for (j = 0; j < TotalScenarios; j++) {
				Objects[i].CPUdemand += Objects[i].ScenarioDemand[j].CPUdemand;
				Objects[i].DISKdemand
					+= Objects[i].ScenarioDemand[j].DISKdemand;
			}
		}

		//      System.out.println ("inaninte de visit table\n");
		for (i = 0; i < TotalObjects; i++) {
			for (k = 0; k < TotalObjects; k++) {
				VisitTable[i][k] = 0;
				for (j = 0; j < TotalScenarios; j++) {
					VisitTable[i][k] = (double) ScenarioVisits[j][i][k];
				}
			}
		}

		// System.out.println("dupa de visit table\n");
		for (i = 0; i < TotalObjects; i++)
			for (j = 0; j < TotalObjects; j++)
				if ((VisitTable[i][j] > 0) || (VisitTable[j][i]) > 0)
					L.addPair(
						Objects[i],
						Objects[j],
						VisitTable[i][j] + VisitTable[j][i],
						1);
	}

	int[] parseLine(String aLine) {

		StringTokenizer aTok = new StringTokenizer(aLine, " ,\t");
		int[] result = new int[aTok.countTokens()];
		int i = 0;
		while (aTok.hasMoreElements()) {
			result[i] = hostIndexOf(aTok.nextToken());
			i++;
		}
		return result;
	}

	int hostIndexOf(String aName) {

		for (int i = 0; i < TotalHosts; i++) {
			if (Hosts[i].name.equals(aName))
				return Hosts[i].code;
		}
		return 999999; /// that is not supposed to happen
	}

	int objectIndexOf(String aName) {

		for (int i = 0; i < TotalObjects; i++) {
			if (Objects[i].name.equals(aName))
				return Objects[i].code;
		}
		return 999999; /// that is not supposed to happen
	}

	int entityIndexOf(String aName) {

		for (int i = 0; i < TotalEntities; i++) {
			if (Entities[i].name.equals(aName))
				return Entities[i].code;
		}

		return 999999; /// that is not supposed to happen
	}

	int scenarioIndexOf(String aName) {

		for (int i = 0; i < TotalScenarios; i++) {
			if (scenarioNames[i].equals(aName))
				return i;
		}

		return infinit; /// that is not supposed to happen
	}

	public void solveAllocation(String argv[]) {

		LQM o = new LQM();
		o.readAndValidatePxl(o.parsePXL(argv[0]));
		o.initialize();
		o.findConfigurations();
		//o.printConfigurations();
	}

	// returns 1 if the maximum demand of the hosts overpass the average demand
	int threashold(Host h, DistributedObject o) {
		if (min(CPU_D - (h.CPUdemand + o.CPUdemand * h.CPURatio),
			DISK_D - (h.DISKdemand + o.DISKdemand * h.DISKRatio))
			>= 0)
			return ((int) 0);
		else
			return ((int) 1);
	}

	public void vhlChangeConfiguration() {

		/**********************************************************************/
		/**** find the configurations for very high population levels**********/
		/**********************************************************************/

		TotalObjConfigurations = TotalScenarios - 1;

		int NoIteration = 0;
		int evricaaa = 0;

		for (i = 0; i < TotalScenarios; i++) {
			cursor[i] = 0;
			LimitCursor[i] = 1;
		}

		cursor[TotalScenarios - 1] = 1;

		do { // iterate

			for (i = 0; i < TotalScenarios; i++) {
				betas[i] = cursor[i]; // update betas;
				// System.out.println(betas[i]);
			}
			//System.out.println("\n");

			for (i = 0; i < TotalEntities; i++) {
				Entities[i].code = EntitiesBackup[i].code;
				//Entities[i].name=EntitiesBackup[i].name;
				Entities[i].Assigned = EntitiesBackup[i].Assigned;
				Entities[i].AssignedToH = EntitiesBackup[i].AssignedToH;
			}

			for (i = 0; i < TotalObjects; i++) {
				Objects[i].biff = Objects[i].biff_Backup;
				Objects[i].AssignedTo = Objects[i].AssignedTo_Backup;
				Objects[i].AssignedE = Objects[i].AssignedE_Backup;
				Objects[i].AssignedToE = Objects[i].AssignedToE_Backup;
				Objects[i].CPUdemand = 0;
				Objects[i].DISKdemand = 0;
				for (j = 0; j < TotalScenarios; j++) {
					Objects[i].CPUdemand
						+= Objects[i].ScenarioDemand[j].CPUdemand
						* betas[j];
					Objects[i].DISKdemand
						+= Objects[i].ScenarioDemand[j].DISKdemand
						* betas[j];
				}
			}

			//System.out.println ("before  tackling the visit table\n");
			for (i = 0; i < TotalObjects; i++) {
				for (k = 0; k < TotalObjects; k++) {
					VisitTable[i][k] = 0;
					for (j = 0; j < TotalScenarios; j++) {
						VisitTable[i][k] = (double) ScenarioVisits[j][i][k];
						//*betas[j];
					}
				}
			}

			//System.out.println("dupa de visit table\n");
			for (i = 0; i < TotalObjects; i++)
				for (j = 0; j < TotalObjects; j++)
					if ((VisitTable[i][j] > 0) || (VisitTable[j][i]) > 0)
						L.addPair(
							Objects[i],
							Objects[j],
							VisitTable[i][j] + VisitTable[j][i],
							1);
			//
			//System.out.println("uraaaaah ");

			CPU_D = 0;
			DISK_D = 0;
			double AvCPURatio = 0;
			double AvDISKRatio = 0;

			for (i = 0; i < TotalHosts; i++) {
				Hosts[i].CPUdemand = 0;
				Hosts[i].DISKdemand = 0;
			}

			for (i = 0; i < TotalObjects; i++) {
				if (Objects[i].biff > 0) {
					Hosts[Entities[Objects[i]
						.AssignedToE]
						.AssignedToH]
						.CPUdemand
						+= Objects[i].CPUdemand
						* Hosts[Entities[Objects[i]
							.AssignedToE]
							.AssignedToH]
							.CPURatio;
					Hosts[Entities[Objects[i]
						.AssignedToE]
						.AssignedToH]
						.DISKdemand
						+= Objects[i].DISKdemand
						* Hosts[Entities[Objects[i]
							.AssignedToE]
							.AssignedToH]
							.DISKRatio;
				}
			}
			for (i = 0; i < TotalHosts; i++) {
				if (Hosts[i].server > 0) {
					AvCPURatio += Hosts[i].CPURatio;
					AvDISKRatio += Hosts[i].DISKRatio;
					CPU_D += Hosts[i].CPUdemand;
					DISK_D += Hosts[i].DISKdemand;
				}
			}

			AvCPURatio /= TotalHosts - 1;
			AvDISKRatio /= TotalHosts - 1;

			for (i = 0; i < TotalObjects; i++) {
				if (!(Objects[i].biff > 0)) {
					CPU_D += Objects[i].CPUdemand * AvCPURatio;
					DISK_D += Objects[i].DISKdemand * AvDISKRatio;
				}
			}

			CPU_D = CPU_D / (TotalHosts - 1);
			DISK_D = DISK_D / (TotalHosts - 1);

			//System.out.println( "\nAverage done %f %f %f %f\n ",AvCPURatio,AvDISKRatio,CPU_D,DISK_D);
			//getchar();

			ObjectPairs cursor;
			DistributedObject assigned, notassigned;
			double temp, minimum;
			int target;
			while (L.getNumber() > 0) { //there are ObjectPairs

				//System.out.println("\n %d \n",L.getNumber());
				//getchar();

				minimum = 0;
				target = 0;
				cursor = L.deletePair();
				if (!(cursor.pObject1.biff > 0)
					&& !(cursor.pObject2.biff > 0)) {

					//System.out.println("\n11111");
					// no object in pair is already assigned
					i = 0;
					target = i;
					minimum = cost2(Hosts[i], cursor);
					for (i = 1; i < TotalHosts; i++) {
						if ((temp = cost2(Hosts[i], cursor)) < minimum) {
							minimum = temp;
							target = i;
						}
					}
					cursor.pObject1.biff = 1;
					cursor.pObject2.biff = 1;
					cursor.pObject1.AssignedTo = target;
					cursor.pObject2.AssignedTo = target;
					Hosts[target].CPUdemand += cursor.pObject1.CPUdemand
						* Hosts[target].CPURatio;
					Hosts[target].CPUdemand += cursor.pObject2.CPUdemand
						* Hosts[target].CPURatio;
					Hosts[target].DISKdemand += cursor.pObject1.DISKdemand
						* Hosts[target].DISKRatio;
					Hosts[target].DISKdemand += cursor.pObject2.DISKdemand
						* Hosts[target].DISKRatio;
				} else {

					if ((!(cursor.pObject1.biff > 0))
						|| (!(cursor.pObject2.biff > 0))) {
						if (cursor.pObject1.biff > 0) {
							assigned = cursor.pObject1;
							notassigned = cursor.pObject2;
							//System.out.println("\n222");
						} else {
							assigned = cursor.pObject2;
							notassigned = cursor.pObject1;
							//System.out.println("\n333");

						} //endiff
						target = assigned.AssignedTo;
						if (threashold(Hosts[assigned.AssignedTo], notassigned)
							> 0) {
							//System.out.println("\n %s %s %d %d 444", assigned->name,notassigned->name,assigned->biff,notassigned->biff);

							i = 0;
							//   target=i;
							minimum = cost(Hosts[i], notassigned);
							//System.out.println("\n%d %f",i,minimum,TotalHosts);
							for (i = 1; i < TotalHosts; i++) {
								//System.out.println("\n%d %f",i,minimum);
								if ((temp = cost(Hosts[i], notassigned))
									< minimum) {
									minimum = temp;
									target = i;
								}

							}
							notassigned.biff = 1;
							notassigned.AssignedTo = target;
							Hosts[target].CPUdemand += notassigned.CPUdemand
								* Hosts[target].CPURatio;
							Hosts[target].DISKdemand += notassigned.DISKdemand
								* Hosts[target].DISKRatio;

						} else {
							//                   System.out.println("\n555");
							notassigned.biff = 1;
							notassigned.AssignedTo = assigned.AssignedTo;
							Hosts[assigned.AssignedTo].CPUdemand
								+= notassigned.CPUdemand
								* Hosts[assigned.AssignedTo].CPURatio;
							Hosts[assigned.AssignedTo].DISKdemand
								+= notassigned.DISKdemand
								* Hosts[assigned.AssignedTo].DISKRatio;
						} //endif
					} //endif
				} //endif
			} //end while
			//    System.out.println("\printing results....\n");
			//    getchar();
			//    print results;
			//    place the object on the entities on the same hosts

			for (i = 0; i < TotalObjects; i++) {
				TVisits[i] = 0;
				for (j = 0; j < TotalObjects; j++) {
					if (i != j) {
						TVisits[i] += VisitTable[j][i] + VisitTable[i][j];
					}
				}
			}

			// order objects by the total visits
			ordvector(TVisits, indexO, TotalObjects);
			// temp variables

			int iE = 0, foundsomething;
			double C = 0;
			double tmin = 0;

			for (i = 0; i < TotalObjects; i++) {
				if (!(Objects[indexO[i]].AssignedE > 0)) {
					// not assigned to an Entity
					//find Entity
					foundsomething = 0;
					tmin = 0;
					for (j = 0; j < TotalEntities; j++) {
						C = 0; // initialize the cost
						if (Entities[j].AssignedToH
							== Objects[indexO[i]].AssignedTo)
							// valid Entity
							{
							for (k = 0;
								k < TotalObjects;
								k++) { // find assigned objects on the same Host and
								if ((Objects[k].AssignedE > 0)
									&& (Objects[k].AssignedTo
										== Objects[indexO[i]].AssignedTo)
									&& Objects[k].AssignedE != j) {
									C += VisitTable[i][k] + VisitTable[k][i];
									foundsomething = 1;
								}
							}
						}
						if ((tmin = min(C, tmin)) == C) {
							iE = j; // memorize this Entity
						}
					}
					if (foundsomething != 1) { //find something arbitrary
						for (j = 0; j < TotalEntities; j++) {
							if (Entities[j].AssignedToH
								== Objects[indexO[i]].AssignedTo) {
								iE = j;
								break;
							}
						}
					}
					Objects[indexO[i]].AssignedE = 1;
					Objects[indexO[i]].AssignedToE = iE;

				} //end if
			} //end for

			// test if the new config is already defined in ObjConfig
			// but not for extreme betas
			int foundConfig = 0;
			int differ;
			for (i = 0;
				i < ObjConfig.Number;
				i++) { // for each configuration in ObjConfig
				differ = 0;
				for (j = 0; j < TotalObjects; j++) {
					// for each Object
					if (ObjConfig.allocation[i][j] != Objects[j].AssignedToE) {
						differ = 1;
						break;
					}
				};
				if (differ < 1) {
					foundConfig = 1;
					break;
				}
			}
			//System.out.println ("Total Obj Config %d \n", TotalObjConfigurations);
			if (foundConfig < 1) {
				for (j = 0; j < TotalObjects; j++)
					ObjConfig.allocation[ObjConfig.Number][j] =
						Objects[j].AssignedToE;
				for (j = 0; j < TotalScenarios; j++)
					ObjConfig.betas[ObjConfig.Number][j] = betas[j];
				ObjConfig.Number++;
			}
			foundConfig = 0;

			// if(NoIteration<TotalObjConfigurations)
			NoIteration++;
			// else
			//     evricaaa=1;

		}
		while (betasC(TotalScenarios) > 0); //end for index

	}
	/**
	 * @return
	 */
	public Document getOutDoc() {
		return outDoc;
	}

	/**
	 * @return
	 */
//	public String getOutXmlFile() {
//		return outXmlFile;
//	}

	/**
	 * @param document
	 */
	public void setOutDoc(Document document) {
		outDoc = document;
	}

	/**
	 * @param string
	 */
//	public void setOutXmlFile(String string)
//	{
//		outXmlFile = string;
//	}

}