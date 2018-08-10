/*
 * Created on Oct 24, 2004
 *
 */
package opera.Core;

/**
 * @author marin
 *
 */
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;


public class ModelSolver
{
	Mva hMva = null;
	Mva sMva = null;
	DataInputStream fpi = null;
	PrintStream fpo = null;
	boolean openModel=false;

	// hardware layer
	int hC;
	int hK;
	int[] hType;
	int[] hMult;
	double[][] hD;
	double[] hNc;
	double[] hZc;

	//software layer

	int sC;
	int sP;
	int[] sType;
	int[] sMult;
	double[][][] sDpkc; // demand of process p at device k in class c
	double[][] sDpc; // demand of process p in class c
	double[][] sDpc_New; // demand of process p in class c

	double[] sNc;
	double[] sZc;

	public ModelSolver()
	{
		super();
	}

	public ModelSolver(int K, int C, int P, int[] ht, int[] hm, int[] st, int[] sm, double[] Nc, double[] Zc, double[][][] demand)
	{
		hC = C;
		hK = K;
		sP = P;
		int i;
		//initialize(aC,aK);
		hType = new int[hK];
		hMult = new int[hK];
		sType = new int[sP];
		sMult = new int[sP];
		sDpkc= new double[sP][hK][hC];
		sDpc= new double[sP][hC]; 	
		sDpc_New= new double[sP][hC]; 	

		hD = new double[hK][hC];
		hNc = new double[hC];
		hZc = new double[hC];

		for (i = 0; i < hK; i++)
		{
			hType[i]=ht[i];
		}
		for (i = 0; i < hK; i++)
		{
			hMult[i] = hm[i];
		}
		for (i = 0; i < sP; i++)
		{
			sType[i] = st[i];
		}
		for (i = 0; i < sP; i++)
		{
			sMult[i] = sm[i];
		}

		for (i = 0; i < hC; i++)
		{
			hNc[i] = Nc[i];
		}

		for (i = 0; i < hC; i++)
		{
			hZc[i] = Zc[i];
		}

		for (i = 0; i < sP; i++)
		{
			for (int k = 0; k < hK; k++)
			{
				for (int c = 0; c < hC; c++)
				{
					sDpkc[i][k][c] = demand[i][k][c];
				}
			}
		}
    }

	public double getSRpc(int k, int c)
	{
		return sMva.getRkc(k,c);
	}	
	
	public double getHRkc(int k, int c)
	{
		return hMva.getRkc(k,c);
	}

	public double getSUpc(int k, int c)
	{
		return sMva.getUkc(k,c);
	}

	public double getHUkc(int k, int c)
	{
		return hMva.getUkc(k,c);
	}

	public double getSUp(int k)
	{
		return sMva.getUk(k);
	}	

	public double getHUk(int k)
	{
		return hMva.getUk(k);
	}
 
	public double getSRc(int c)
	{
		return sMva.getRc(c);
	}

	public double getSXc( int c)
	{
		return sMva.getXc(c);
	}

    public void printStatus()
    {
    	// per class throughput and Response time
    	System.out.println(" Per class response time ");
    	for (int i = 0; i < hC; i++)
    	{
    		System.out.println("class "+i+"  response time " + getSRc(i));
    	}

    	System.out.println(" Per class throughput ");
    	for (int i = 0; i < hC; i++)
    	{
    		System.out.println("class "+ i + "  throughput " + getSXc(i));
    	}

    	System.out.println("\n Per process Utilization");
    	for (int i=0;i<sP;i++)
    	{
    		System.out.println("process " + i + "  Utilization " + getSUp(i));
    	}

    	System.out.println("\n Per device Utilization");
    	for (int i=0;i<sP;i++)
    	{
    		System.out.println("device " + i + "  Utilization " + getHUk(i));
    	}
    }
	public String readString(DataInputStream aDis) {

		char c;
		String s = new String();

		try {
			do {
				c = (char) aDis.readByte(); // trim the spaces and CR
			} while ((c == ' ') || (c == '\n') || (c == '\r') || (c == '\t'));

			do {
				s = s + c;
				c = (char) aDis.readByte();
			} while ((c != ' ') && (c != '\n') && (c != '\r') && (c != '\t'));

		} catch (IOException e) {
		};

		return (s);
	}

	public void parseModel(String[] argv) {
		int i;
		try {
			if ((fpi = new DataInputStream(new FileInputStream(argv[0])))
				== null) {
				System.out.println("text file can not be opened");
				return;
			}

			hC = Integer.valueOf(readString(fpi)).intValue();
			hK = Integer.valueOf(readString(fpi)).intValue();
			sP = Integer.valueOf(readString(fpi)).intValue();
			//initialize(aC,aK);
			hType = new int[hK];
			hMult = new int[hK];
			sType = new int[sP];
			sMult = new int[sP];
            sDpkc= new double[sP][hK][hC];		
            sDpc= new double[sP][hC]; 	
    		sDpc_New= new double[sP][hC]; 	
			hD = new double[hK][hC];
			hNc = new double[hC];
			hZc = new double[hC];

			for (i = 0; i < hK; i++)
				hType[i] = Integer.valueOf(readString(fpi)).intValue();
			for (i = 0; i < hK; i++)
				hMult[i] = Integer.valueOf(readString(fpi)).intValue();
			for (i = 0; i < sP; i++)
				sType[i] = Integer.valueOf(readString(fpi)).intValue();
			for (i = 0; i < sP; i++)
				sMult[i] = Integer.valueOf(readString(fpi)).intValue();

			for (i = 0; i < hC; i++)
				hNc[i] = Float.valueOf(readString(fpi)).doubleValue();

			for (i = 0; i < hC; i++)
				hZc[i] = Float.valueOf(readString(fpi)).doubleValue();


			for (i = 0; i < sP; i++)
				for (int k = 0; k < hK; k++)
					for (int c = 0; c < hC; c++)
						sDpkc[i][k][c] =
							Float.valueOf(readString(fpi)).doubleValue();

		} catch (Exception e) {
			System.out.println("Wrong file or format:" + e.toString());
		};
		return;
	}

	public void solveModel(double[] Nc)
	{
		for (int i=0; i<Nc.length; i++)
		{
			hNc[i]= Nc[i];
		}
	    solveModel();
	}
	
	private void SaveModel()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(this.hC);
		builder.append('\n');
		builder.append(this.hK);
		builder.append('\n');
		builder.append(this.sP);
		builder.append('\n');

		for (int i = 0; i < this.hType.length; ++i)
			builder.append(this.hType[i] + "\t");
		builder.append('\n');
		for (int i = 0; i < this.hMult.length; ++i)
			builder.append(this.hMult[i] + "\t");
		builder.append('\n');
		for (int i = 0; i < this.sType.length; ++i)
			builder.append(this.sType[i] + "\t");
		builder.append('\n');
		for (int i = 0; i < this.sMult.length; ++i)
			builder.append(this.sMult[i] + "\t");
		builder.append('\n');

		for (int i = 0; i < this.hNc.length; ++i)
			builder.append(this.hNc[i] + "\t");
		builder.append('\n');
		for (int i = 0; i < this.hZc.length; ++i)
			builder.append(this.hZc[i] + "\t");
		builder.append('\n');
		builder.append('\n');


		for (int i = 0; i < this.sDpkc.length; ++i)
		{
			for (int j = 0; j < this.sDpkc[i].length; ++j)
			{
				for (int k = 0; k < this.sDpkc[i][j].length; ++k)
					builder.append(String.format("%8.6f\t", this.sDpkc[i][j][k]));
				builder.append('\n');
			}
			builder.append('\n');
		}

		System.out.print(builder.toString());
	}

	public void solveModel()
	{
//		SaveModel();
		int i;
		for (i = 0; i < hK; i++)
		{
			for (int j = 0; j < hC; j++)
			{
				hD[i][j] = 0;
				for (int p = 0; p < sP; p++)
				{
					hD[i][j] += sDpkc[p][i][j];
				}
			}
		}

		for (i = 0; i < sP; i++)
		{
			for (int j = 0; j < hC; j++)
			{
				sDpc[i][j] = 0;
				for (int k = 0; k < hK; k++)
				{
					sDpc[i][j] += sDpkc[i][k][j];
				}
			}
		}

		int iteration = 0;
		double[] Bc   = new double[hC];
		double[] Bc_1 = new double[hC];

		double[] Lc   = new double[hC];
        double[] Lc_2 = new double[hC];
        double[] Lc_1 = new double[hC];
        
		do
		{
			sMva = new Mva(hC, sP, hNc, hZc, sDpc, sType, sMult);
			sMva.PE();
			for (int c = 0; c < hC; c++)
			{
				Bc[c] = 0;
				for (i = 0; i < sP; i++)
				{
					if (sType[i] == 1) // if server
					{
						Bc[c] += sMva.Qkc[i][c] - sMva.Ukc[i][c];//the number of blocked requests
																 //is the difference between the queue lengths and the utilization						
					}
				}
				Lc[c] = hNc[c] - Bc[c];
			}

			/*
			System.out.printf("Iter: \t%4d\n", iteration);
			System.out.printf("Nc:   \t%4.0f\n", hNc[0]);
			System.out.printf("sDpc: ");
			for (int ii = 0; ii < sDpc.length; ++ii)
			{
				for (int j = 0; j < sDpc[ii].length; ++j)
					System.out.printf("\t%9.4f", sDpc[ii][j]);
			}
			System.out.println();

			System.out.printf("sBc: ");
			for (int ii = 0; ii < Bc.length; ++ii)
			{
				System.out.printf("\t%9.4f", Bc[ii]);
			}
			System.out.println();

			System.out.printf("sBc1: ");
			for (int ii = 0; ii < Bc_1.length; ++ii)
			{
				System.out.printf("\t%9.4f", Bc_1[ii]);
			}
			System.out.println();

			System.out.printf("sDkc: ");
			for (int ii = 0; ii < sMva.Dkc.length; ++ii)
			{
				for (int j = 0; j < sMva.Dkc[ii].length; ++j)
					System.out.printf("\t%9.4f", sMva.Dkc[ii][j]);
			}
			System.out.println();

			System.out.printf("sQkc: ");
			for (int ii = 0; ii < sMva.Qkc.length; ++ii)
			{
				for (int j = 0; j < sMva.Qkc[ii].length; ++j)
					System.out.printf("\t%9.4f", sMva.Qkc[ii][j]);
			}
			System.out.println();

			System.out.printf("sUkc: ");
			for (int ii = 0; ii < sMva.Ukc.length; ++ii)
			{
				for (int j = 0; j < sMva.Ukc[ii].length; ++j)
					System.out.printf("\t%9.4f", sMva.Ukc[ii][j]);
			}
			System.out.println();

			System.out.printf("sRkc: ");
			for (int ii = 0; ii < sMva.Rkc.length; ++ii)
			{
				for (int j = 0; j < sMva.Rkc[ii].length; ++j)
					System.out.printf("\t%9.4f", sMva.Rkc[ii][j]);
			}
			System.out.println();

			if (hMva != null)
			{
				System.out.printf("hDkc: ");
				for (int ii = 0; ii < hMva.Dkc.length; ++ii)
				{
					for (int j = 0; j < hMva.Dkc[ii].length; ++j)
						System.out.printf("\t%9.4f", hMva.Dkc[ii][j]);
				}
				System.out.println();
	
				System.out.printf("hQkc: ");
				for (int ii = 0; ii < hMva.Qkc.length; ++ii)
				{
					for (int j = 0; j < hMva.Qkc[ii].length; ++j)
						System.out.printf("\t%9.4f", hMva.Qkc[ii][j]);
				}
				System.out.println();
	
				System.out.printf("hUkc: ");
				for (int ii = 0; ii < hMva.Ukc.length; ++ii)
				{
					for (int j = 0; j < hMva.Ukc[ii].length; ++j)
						System.out.printf("\t%9.4f", hMva.Ukc[ii][j]);
				}
				System.out.println();
	
				System.out.printf("hRkc: ");
				for (int ii = 0; ii < hMva.Rkc.length; ++ii)
				{
					for (int j = 0; j < hMva.Rkc[ii].length; ++j)
						System.out.printf("\t%9.4f", hMva.Rkc[ii][j]);
				}
				System.out.println();
	
				
				System.out.println();
			}
			//*/

			/*
			if ((converge(Bc, Bc_1)) && (iteration > 0)) // need to solve both layers at least once...
			{
				break;
			}
			*/
			
			for (int s = 0; s < hC; s++)
			{
				Bc_1[s] = Bc[s];// save the queue lengths
			}
			/*
			if (iteration>3)
				for (int s=0;s<hC;s++) if ((Lc_2[s]==Lc[s])&&(Lc_2[s]>Lc_1[s]))
					 Lc[s]=Lc[s]-0.5*(Lc[s]-Lc_1[s]);		
			*/	
			for (int s = 0; s < hC; s++)
			{
				Lc_2[s] = Lc_1[s];
				Lc_1[s] = Lc[s];
			}

			hMva = new Mva(hC, hK, Lc, hZc, hD, hType, hMult);
			hMva.PE();
			
			// update the demands at the software processes
			for (i = 0; i < sP; i++)
			{
				for (int j = 0; j < hC; j++)
				{
					sDpc_New[i][j] = 0;
					for (int k = 0; k < hK; k++)
					{
					    if(hD[k][j] > 0)
					    {
					    	sDpc_New[i][j] += (sDpkc[i][k][j] / hD[k][j]) * hMva.getRkc(k,j);// the new service time for a software component
					    																	//is a fraction of the hardware queue response time
					    }
					}
				}
			}
			for (i = 0; i < sP; i++)
			{
				for (int j = 0; j < hC; j++)
				{
					if (sDpc_New[i][j] > sDpc[i][j])
					{
						sDpc[i][j] = sDpc[i][j] + 0.001 * (sDpc_New[i][j] - sDpc[i][j]);
						//sDpc[i][j] = sDpc_New[i][j];
					}
				}
			}

			iteration++;

/*			System.out.printf("%4.0f\t%6d\t%9.4f\t%9.4f", this.hNc[0], iteration, hMva.Rc[0], sMva.Rc[0]);
			
			for (int ii = 0; ii < hMva.Rkc.length / 2; ++ii)
			{
				System.out.printf("\t%9.4f", hMva.Rkc[ii][0]);
			}

			for (int ii = 0; ii < sMva.Dkc.length / 2; ++ii)
			{
				for (int j = 0; j < sMva.Dkc[ii].length; ++j)
					System.out.printf("\t%9.4f", sMva.Dkc[ii][j]);
			}
			
			for (int ii = 0; ii < sMva.Rkc.length / 2; ++ii)
			{
				for (int j = 0; j < sMva.Rkc[ii].length; ++j)
					System.out.printf("\t%9.4f", sMva.Rkc[ii][j]);
			}
			
			System.out.printf("\t%9.4f", Lc[0]);

			System.out.println();
			*/
		}
		while (iteration < 3000);//was 3500

		return;
	}
	

	public void solveOpenModel(double[] l) {
		for (int i=0;i<l.length;i++)hNc[i]= l[i];
		int i;
		for (i = 0; i < hK; i++)
			for (int j = 0; j < hC; j++) {
				hD[i][j] = 0;
				for (int p = 0; p < sP; p++)
					hD[i][j] += sDpkc[p][i][j];
			}

		for (i = 0; i < sP; i++)
			for (int j = 0; j < hC; j++) {
				sDpc[i][j] = 0;
				for (int k = 0; k < hK; k++)
					sDpc[i][j] += sDpkc[i][k][j];
			}
			
			
		int iteration = 0;
		double[] Bc = new double[hC];
		double[] Bc_1 = new double[hC];

		double[] Lc = new double[hC];
		double[] Zc = new double[hC];
		double[] sNc = new double[hC];
		double[] hThr = new double[hC];
		
		do {

			sMva = new Mva(hC, sP, hNc, null, sDpc, sType, sMult);
			sMva.openPE();
			for (int c = 0; c < hC; c++) {
				Bc[c] = 0;
				for (i = 0; i < sP; i++) {
					Bc[c] += (sMva.Qkc[i][c]- sMva.Ukc[i][c]);// ..requests queued at software entities..
				}
				Lc[c] = ((sMva.getQc(c) - Bc[c]));   // Lc is the number of requests that are not queued at software resources..
				                                 //.. that is, those requests which are not in software queues are being processed by hardware...  
                sNc[c]=Math.ceil(Lc[c]);// Lc[c] is a real number, given by the fact that an arriving request ca see "a fraction 
                                                                         //of users being served. We have to round up that fraction 
                /*
                hThr[c]=hNc[c]*(Lc[c]/(Bc[c]+Lc[c]));
                sNc[c]=Lc[c]*1000;// make the number of overall users a lot bigger than those wainting in queues.. 
				Zc[c]=sNc[c]/hThr[c]-sMva.getRc(c);if (Zc[c]<0) Zc[c]=0;// think time is computed with MVA formulas
                //if ((Bc[c]<1)&&(Zc[c]>0))Bc[c]=1;//in an open system there is at least 1 user in the system
			  */
			}
			
			if (converge(Bc, Bc_1))
				break;
				
			for (int s=0;s<hC;s++) Bc_1[s]=Bc[s];
				
			if (queuing(Bc)){	
			
			hMva = new Mva(hC, hK, sNc, null, hD, hType, hMult);
			hMva.PE();
			}
			else{
				hMva = new Mva(hC, hK, hNc, null, hD, hType, hMult);
							hMva.openPE();
			}
			// update the demands at the software processes

			for (i = 0; i < sP; i++)
				for (int j = 0; j < hC; j++) {
					sDpc[i][j] = 0;
					for (int k = 0; k < hK; k++)
						if(hD[k][j]>0)
						  sDpc[i][j] += sDpkc[i][k][j]*hMva.getRkc(k,j)/hD[k][j];
				}
					
			iteration++;
		} while (iteration < 100);
		return;

	}
	
	private boolean converge(double[] a, double[] b)
	{
		for (int i = 0; i < a.length; i++)
		{
			if (Math.abs((a[i] - b[i])/a[i]) > 0.00001)
			{
				// System.out.println((a[i] - b[i])/a[i]);
				return false;
			}
		}
		return true;
	}

	private boolean queuing(double[] a)
	{
		for (int i = 0; i < a.length; i++)
		{
			if (a[i]> 0.1)
			{
				// System.out.println((a[i] - b[i])/a[i]);
				return true;
			}
		}
		return false;
	}

	
	public static void main(String argv[])
	{
		try
		{
			ModelSolver ms = new ModelSolver();
			ms.parseModel(argv);
			
			for (int i = 0; i < 2000; ++i)
			{
				ms.hNc[0] = i;
				ms.solveModel();
//				System.out.println(ms.hMva.Rc[0]);
			}
			// ms.printStatus();
		}
		catch (Exception ex) { ex.printStackTrace();}

		return;
	}
	
}
