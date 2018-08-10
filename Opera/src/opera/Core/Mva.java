
package opera.Core;

/**
 * This type was created by  Marin Litoiu.
*/

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

public class Mva {
	DataInputStream fpi = null;
	PrintStream fpo = null;
	//variables
	boolean trace;
	int C;
	int K;
	int O;
	static final double Step = (double) 0.5;
	static final double ERROR = (double) 0.001;
	// parameters at step i
	double Qkc[][]; // queues per device and class
	double Ukc[][]; // utiliz. per device and class
	double Rkc[][]; // response times per device and class
	double Qk[]; // que per device
	double Uk[]; // utiliz per device
	double Rc[];
	double Xc[];
	double Nc[];
	double Zc[]; //think times

	double N;
	double Dkc[][]; // demand of servers/ delays and 
	// parameters at step i-1;

	double Qkc_1[][]; // queues per device and class
	double Rkc_1[][]; // response times per device and class
	double Qk_1[]; // que per device
	double Rc_1[];
	double Xc_1[];

	int type[]; // 0 - client, 1-delay, 2 - server; 
	int multiplicity[];
	double Err;
	double Err_1;

	public Mva parseAndCreateModel(String argv[]) {
		int i;
		Mva anMva = null;
		if (argv.length != 2) {
			System.out.println("incorrect command line");
			return anMva;
		}
		int aC;
		int aK;
		int[] aType;
		int[] aMult;
		double[][] aD;
		double[] aNc;
		double[] aZc;

		try {
			if ((fpi = new DataInputStream(new FileInputStream(argv[0])))
				== null) {
				System.out.println("text file can not be opened");
				return anMva;
			}

			aC = Integer.valueOf(readString(fpi)).intValue();
			aK = Integer.valueOf(readString(fpi)).intValue();
			//initialize(aC,aK);
			aType = new int[aK];
			aMult = new int[aK];
			aD = new double[aC][aK];
			aNc = new double[aC];
			aZc = new double[aC];

			for (i = 0; i < aK; i++)
				aType[i] = Integer.valueOf(readString(fpi)).intValue();
			for (i = 0; i < aK; i++)
				aMult[i] = Integer.valueOf(readString(fpi)).intValue();

			for (i = 0; i < aC; i++)
				aNc[i] = Float.valueOf(readString(fpi)).doubleValue();

			for (i = 0; i < aC; i++)
				aZc[i] = Float.valueOf(readString(fpi)).doubleValue();

			for (i = 0; i < aK; i++)
				for (int j = 0; j < aC; j++) {
					aD[i][j] = Float.valueOf(readString(fpi)).doubleValue();
					System.out.println(" " + aD[i][j]);
				}
			anMva = new Mva(aC, aK, aNc, aZc, aD, aType, aMult);
		} catch (Exception e) {
			System.out.println("Wrong file or format:" + e.toString());
		};
		return anMva;
	}

	public Mva(
		int aC,
		int aK,
		double[] aNc,
		double[] aZ,
		double[][] aD,
		int[] aType) {
		int i;

		try {
			initialize(aC, aK);
			for (i = 0; i < K; i++)
				type[i] = aType[i]; // server/delay
			for (i = 0; i < C; i++)
				Nc[i] = aNc[i];
			N = 0;

			for (i = 0; i < C; i++)
				N += Nc[i];
			for (i = 0; i < C; i++)
				Zc[i] = aZ[i];

			for (i = 0; i < K; i++)
				for (int j = 0; j < C; j++) {
					setD(i, j, aD[i][j]);
				}

		} catch (Exception e) {
			System.out.println("Wrong input format:" + e.toString());
		};

	}
	/**
	 * 
	 */
	public Mva() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Mva(
		int aC,
		int aK,
		double[] aNc,
		double[] aZ,
		double[][] aD,
		int[] aType,
		int[] aMult) {
		int i;
		multiplicity = new int[aMult.length];

		for (i = 0; i < aMult.length; i++)
			multiplicity[i] = aMult[i];

		try {
			initialize(aC, aK * 2); // create twice as many queuing centres to 
			for (i = 0; i < aK; i++)
				type[i] = aType[i]; // server/delay
			for (i = aK; i < aK * 2; i++)
				type[i] = 2;
			// a dummy delay for each centre to use it eventually for

			for (i = 0; i < C; i++)
				Nc[i] = aNc[i];
			N = 0;

			for (i = 0; i < C; i++)
				N += Nc[i];
			if (aZ!=null) //if there is think time	
			  for (i = 0; i < C; i++)
				 Zc[i] = aZ[i];

			for (i = 0; i < aK; i++)
				for (int j = 0; j < C; j++) {
					// if multiserver use Seidmann & Schweitzer aproximation alorithm
					if (multiplicity[i] > 1) {
						setD(i, j, aD[i][j] / multiplicity[i]);
						//the queuing centre demand is scaled
						setD(
							i + aK,
							j,
							aD[i][j] * (multiplicity[i] - 1) / multiplicity[i]);
						// the coresponding delay centre keeps the measured value of the server

					} else {
						setD(i, j, aD[i][j]); // keep the same demand
						setD(i + aK, j, 0);
						// the demand of the coresponding delay centre is 0

					}
				}

		} catch (Exception e) {
			System.out.println("Wrong input format:" + e.toString());
		};

	}


	public double getQc(int c) {
        double Q=0;
        for (int k=0;k<K/2;k++)Q+=this.getQkc(k,c);
        return Q;
		//includes the que length to the associated delay centre
	}
	public double[] getQc() {
		double[] Q= new double[C];
		for (int c=0;c<C;c++)Q[c]=this.getQc(c);
		return Q;
		//includes the que length to the associated delay centre
	}
	

	public double getQk(int k) {

		return Qk[k] + Qk[k + K / 2];
		//includes the que length to the associated delay centre
	}
	public double getQkc(int k, int c) {

		return Qkc[k][c] + Qkc[k + K / 2][c];
		// includes the corresponding delay
	}
	public double getRkc(int k, int c) {

		return Rkc[k][c] + Rkc[k + K / 2][c];
		// includes the corresponding delay
	}

	public double getRc(int c) {

		return Rc[c];
	}
	public double getUk(int k) {
		return Uk[k];
	}
	public double getUkc(int k, int c) {

		return Ukc[k][c];
	}
	public double getXc(int c) {

		return Xc[c];
	}
	public void initialize(int c, int k) {

		C = c;
		K = k;
		Nc = new double[C];
		//System.out.println (" C " + C +"K " +K);
		Qkc = new double[K][C]; // queues per device and class
		Qkc_1 = new double[K][C]; // queues per device and class
		Qk = new double[K]; // queues per device and class
		Ukc = new double[K][C]; // utiliz. per device and class
		Uk = new double[K]; // utiliz. per device and class

		Qk_1 = new double[K]; // queues per device and class
		Rkc = new double[K][C]; // response per device and class
		Rkc_1 = new double[K][C]; // response per device and class
		Rc = new double[C]; // response per class 
		Rc_1 = new double[C]; // 
		Xc = new double[C]; // thruput per class 
		Xc_1 = new double[C]; // 
		Dkc = new double[K][C];
		Zc = new double[C]; // think times per class 
		type = new int[K];
		Err = 0;
		N = 0;
		trace = false;

	}

	public static void main(String argv[]) {

		try {

			Mva aMVA = new Mva().parseAndCreateModel(argv);
			aMVA.PE();
			//			aMVA.fpi.close();
			aMVA.printStatus(argv[1]);

		} catch (Exception ex) {

		}
		return;
	}
	// returns the maximum between x and y
	double max(double x, double y) {
		if (x < y)
			return y;
		else
			return x;
	}
	// return minimum between x and y
	double min(double x, double y) {
		if (x < y)
			return x;
		else
			return y;
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
	public void PE(double[] aNc) {
		//update the population;
		Nc = aNc;
		PE();
	}
	public void PE()
	{
		int iteration = 0;

		int i, j;
		N = 0;
		Err = 0;
		for (i = 0; i < C; i++)
		{
			N += Nc[i];
		}
		//start the computing;
		for (i = 0; i < K; i++)
		{
			Qk[i] = N / K;
			for (j = 0; j < C; j++)
			{
				Qkc[i][j] = 0;
			}
		}

		do
		{
			++iteration;
			//        printStatus();
			for (i = 0; i < K; i++) // record the old queues
			{
				Qk_1[i] = Qk[i];
			}
			Err_1 = Err;
			//response times
			for (i = 0; i < K; i++)
			{
				for (j = 0; j < C; j++)
				{
					if (Nc[j] == 0)
					{
						Rkc[i][j] = 0;
					}
					else
					{
						if (type[i] == 1) // if server
						{
							Rkc[i][j] = Dkc[i][j] * (1 + Qk[i] - Qkc[i][j] / Nc[j]);//Schweitzer algorithm
						}
						else
						{
							Rkc[i][j] = Dkc[i][j]; // client or delay
						}
					}
				}
			}

			for (j = 0; j < C; j++)
			{
				Rc[j] = 0;
			}
			for (i = 0; i < C; i++)
			{
				for (j = 0; j < K; j++)
				{
					Rc[i] += Rkc[j][i];
				}
			}
			for (i = 0; i < C; i++)
			{
				Xc[i] = Nc[i] / (Zc[i] + Rc[i]);
			}
			for (i = 0; i < C; i++)
			{
				for (j = 0; j < K; j++)
				{
					Qkc[j][i] = Rkc[j][i] * Xc[i];
					Ukc[j][i] = Dkc[j][i] * Xc[i];
				}
			}

			for (j = 0; j < K; j++)
			{
				Qk[j] = 0;
				Uk[j] = 0;
			}

			for (i = 0; i < C; i++)
			{
				for (j = 0; j < K; j++)
				{
					Qk[j] += Qkc[j][i];
					Uk[j] += Ukc[j][i];
				}
			}

			Err = 0;
			for (i = 0; i < K; i++)
			{
				Err = max(Err, Math.abs(Qk_1[i] - Qk[i]));
			}
			if (trace)
			{
				printStatus();
			}
		}
		while (Math.abs(Err_1 - Err) > ERROR || iteration < 10);
	}

	public void openPE() {
		int i, j;
		Err = 0;
		for (j = 0; j < K; j++) {
			Uk[j] = 0;
			for (i = 0; i < C; i++) {
				Ukc[j][i] = Dkc[j][i] * Nc[i]; // Nc is the arrival rate...
				Uk[j] += Ukc[j][i];
			
			}
			if ((Uk[j]>=1)&&(type[j]==1)) {// if server and saturated
							   String errString =new String();
							   for (int s = 0; s < C; s++)
								  errString+=Nc[s]+ " ";
							   throw new OpenSystemArrivalRateException("Arrival rates: " +errString + "are too high");
						}
			
		}

		
		//response times
			for (i = 0; i < K; i++)
				for (j = 0; j < C; j++) {
					if (Nc[j] == 0)
						Rkc[i][j] = 0;
					else {
						if (type[i] == 1) // if server
							Rkc[i][j] = Dkc[i][j] / (1 - Uk[i]);
						else
							Rkc[i][j] = Dkc[i][j]; // client or delay
					}
				}

			for (j = 0; j < C; j++)
				Rc[j] = 0;
			for (i = 0; i < C; i++)
				for (j = 0; j < K; j++)
					Rc[i] += Rkc[j][i];
			if (trace)
				printStatus();
		
	    for (i = 0; i < C; i++) Xc[i]=Nc[i];
	    
	    // queue lengths

		for (j = 0; j < K; j++) {
			Qk[j] = 0;
			for (i = 0; i < C; i++) {
				Qkc[j][i] = Xc[i]*Rkc[j][i];
				Qk[j] += Qkc[j][i];
			}
		}
	    
		
}
		public void printStatus(String arg) {
			int i;

			try {
				if ((fpo = new PrintStream(new FileOutputStream(arg)))
					== null) {
					System.out.println("text file can not be opened");
					return;
				};
				fpo.println(" Per device Queues ");
				for (i = 0; i < K; i++)
					fpo.print(" " + Qk[i]);
				fpo.println("\n Per device Utilization");
				for (i = 0; i < K; i++)
					fpo.println(" " + Uk[i]);
				fpo.println("\n Per Class Response Times");
				for (i = 0; i < C; i++)
					fpo.println(" " + Rc[i]);
				fpo.println("\n Max Err=" + Err);
			} catch (Exception ex) {

			}
		}

		public void printStatus() {
			int i;

			try {
				System.out.println(" Per device Queues ");
				for (i = 0; i < K; i++)
					System.out.print(" " + Qk[i]);
				System.out.println("\n Per device Utilization");
				for (i = 0; i < K; i++)
					System.out.println(" " + Uk[i]);
				System.out.println("\n Per Class Response Times");
				for (i = 0; i < C; i++)
					System.out.println(" " + Rc[i]);
				System.out.println("\n Max Err=" + Err);
			} catch (Exception ex) {

			}
		}

		public String readString(DataInputStream aDis) {

			char c;
			String s = new String();

			try {
				do {
					c = (char) aDis.readByte(); // trim the spaces and CR
				} while (
					(c == ' ') || (c == '\n') || (c == '\r') || (c == '\t'));

				do {
					s = s + c;
					c = (char) aDis.readByte();
				} while (
					(c != ' ') && (c != '\n') && (c != '\r') && (c != '\t'));

			} catch (IOException e) {
			};

			return (s);
		}
		public void setD(int k, int c, double dm) {

			Dkc[k][c] = dm;
		}
		public void setDkc() {
			int i;
			int j;
			// just random
			Random rand = new Random();
			for (i = 0; i < K; i++) {
				for (j = 0; j < C; j++) {
					Dkc[i][j] = Math.abs(rand.nextInt() % 100) + 1;
					//                        System.out.println(" " + Dkc[i][j]);
				}

			}

			// think times are all equal to 10s;
			for (j = 0; j < C; j++)
				Zc[j] = 10000;
		}
		public void setTrace(boolean aBoolean) {

			trace = aBoolean;
		}
		public void setType(int k, int i) {

			type[k] = i;
		}
		public void setZ(int c, double z) {

			Zc[c] = z;
		}
	}
