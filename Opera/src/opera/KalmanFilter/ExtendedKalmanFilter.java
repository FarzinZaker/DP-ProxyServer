/*
 * Created on Aug 10, 2005
 *
 * This class implements a simple version of the extended Kalman Filter. 
 * It bases on the equations (2.14 - 2.18) in the paper "An Introduction 
 * to the Kalman Filter" by G. Welch and G. Bishop. 
 * (http://www.cs.unc.edu/~welch/media/pdf/kalman_intro.pdf).
 * In this class, matrices A, W and V are identity matrices and function 
 * f(x(k-1), u(k-1), 0) = x(k-1)
 * 
 */

package opera.KalmanFilter;

import opera.Core.Matrix;


/** 
 * @author taozheng
 *
 * @param pMat the P matrix  
 * @param xMat the x matrix (tracking parameters)
 * @param rMat the R matrix
 * @param hMat the H matrix (sensitivity to the parameters)
 * @param zMat the z matrix (measurements)
 * @param hxMat the h(x) matrix (the estimated values for the measurement)
 * 
 */
public class ExtendedKalmanFilter {
	double [][] pMatrixOld;
	double [][] xMatrixOld;
	double [][]	pMatrixNew;
	double [][] xMatrixNew;
	double [][] qMatrix;
	double [][] rMatrix;
	double [][] kMatrix;
	double [][] zMatrix;
	double [][] hMatrix;
	double [][] hxMatrix;
	double [][] tempMatrix; //the matrix for temporary data
	
	public ExtendedKalmanFilter(double[][] pMat, double xMat [][], double[][] rMat, double[][] hMat, double[][] zMat, double[][]hxMat,double[][] qMat  )
	{	//initialize
		pMatrixOld = pMat;
		xMatrixOld = xMat;
		rMatrix = rMat;
		hMatrix = hMat;
		zMatrix = zMat;
		hxMatrix = hxMat;
		qMatrix = qMat;
		
		//Measurement Update
		//compute the Kalman gain (K matrix)
		/* DEBUG ONLY: pretty-print the "H Matrix"
		{
			StringBuilder sb = new StringBuilder("Matrix H = ");
			for (int i = 0; i < hMatrix.length; i++)
			{
				sb.append(i > 0 ? "           | " : "| ");
				for (int j = 0; j < hMatrix[0].length; j++)
				{
					sb.append(String.format("% 10.4f ", hMatrix[i][j]));
				}
				sb.append("|\n");
			}
			System.out.println(sb.toString());
		}
		*/

		tempMatrix = Matrix.mulMatrix(hMatrix, pMatrixOld);
		tempMatrix = Matrix.mulMatrix(tempMatrix, Matrix.transpose(hMatrix));
		tempMatrix = Matrix.add(tempMatrix, rMatrix);
		//get the matrix inversion
		int []perm = Matrix.lupDecompose(tempMatrix);
		tempMatrix = Matrix.lupInvert(tempMatrix, perm); 
		kMatrix = Matrix.mulMatrix(Matrix.mulMatrix(pMatrixOld,Matrix.transpose(hMatrix)),tempMatrix);

		/* DEBUG ONLY: pretty-print the "K Matrix"
		{
			StringBuilder sb = new StringBuilder("Matrix K = ");
			for (int i = 0; i < kMatrix.length; i++)
			{
				sb.append(i > 0 ? "           | " : "| ");
				for (int j = 0; j < kMatrix[0].length; j++)
				{
					sb.append(String.format("% 10.4f ", kMatrix[i][j]));
				}
				sb.append("|\n");
			}
			System.out.println(sb.toString());
		}
		*/

		//update tracking parameters (x matrix) 
		tempMatrix = Matrix.sub(zMatrix, hxMatrix);

		/* DEBUG ONLY: pretty-print the "E Matrix"
		{
			StringBuilder sb = new StringBuilder("Matrix E = ");
			for (int i = 0; i < tempMatrix.length; i++)
			{
				sb.append(i > 0 ? "           | " : "| ");
				for (int j = 0; j < tempMatrix[0].length; j++)
				{
					sb.append(String.format("% 10.4f ", tempMatrix[i][j]));
				}
				sb.append("|\n");
			}
			System.out.println(sb.toString());
		}
		*/
		
		xMatrixNew = Matrix.add(xMatrixOld,Matrix.mulMatrix(kMatrix,tempMatrix));
		
		//update error covariance (P matrix)
		tempMatrix = Matrix.mulMatrix(Matrix.mulMatrix(kMatrix, hMatrix),pMatrixOld);
		pMatrixNew = Matrix.sub(pMatrixOld, tempMatrix);
	
		
		//Time Update
		xMatrixOld = xMatrixNew;
		pMatrixOld = Matrix.add(pMatrixNew, qMatrix);
	}
	/*
	 * @return the updated tracking parameters
	 */
	public double [][]getUpdatedXMatrix()
	{	return xMatrixNew;
		
	}
	
	/*
	 * @return the updated error convarinace
	 */
	public double [][]getUpdatedPMatrix()
	{	return pMatrixNew;
		
	}
	/*
	 * @return the projected tracking parameters
	 */
	public double [][]getProjectedXMatrix()
	{	return xMatrixOld;
		
	}
	
	/*
	 * @return the projected error covariance
	 */
	public double [][]getProjectedPMatrix()
	{	return pMatrixOld;
		
	}

}
