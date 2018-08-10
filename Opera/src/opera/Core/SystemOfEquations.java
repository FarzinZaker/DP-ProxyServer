package opera.Core;

import java.util.ArrayList;

public class SystemOfEquations {

    ExtremePoints ep = null;
    double[][] A = null;
    double[][] B = null;

    double[] b = null;

    public SystemOfEquations(double[][] mat, double[] limits) {
        ep = new ExtremePoints();
        B = mat;
        b = limits;

    }

    public ArrayList getAllExtremePoints() {
        return ep.getAll();
    }

    ArrayList computeExtremePoints(double[][] A, double[] b, double[] u) {

        int c = u.length;
        int r = b.length;
        ArrayList list = new ArrayList();
        // list.add(u);

        // check that every resource utilization is still less than the b[m]
        for (int m = 0; m < r; m++) {
            double sum = 0;
            double[] temp = new double[u.length];
            for (int n = 0; n < c; n++) {
                temp[n] = A[m][n] * u[n];
                sum += temp[n];
            }
            if ((sum < (b[m] + 0.001)) && (sum > (b[m] - 0.001))) {
                list.add(temp);
            }
        }

        return list;

    }

    boolean validSolution(double[][] A, double[] b, double[] u) {

        int c = u.length;
        int r = b.length;
        boolean valid = true;

        // check all utilizations are in the interval 0, 1
        for (int m = 0; m < c; m++)
            if (u[m] > 1.001) {
                valid = false;
                break;
            }
        if (!valid)
            return valid;

        for (int m = 0; m < c; m++)
            if (u[m] < 0) {
                valid = false;
                break;
            }
        if (!valid)
            return valid;

        // check that every resource utilization is still less than the b[m]
        for (int m = 0; m < r; m++) {
            double sum = 0;
            for (int n = 0; n < c; n++)
                sum += A[m][n] * u[n];
            if ((sum > (b[m] + 0.001)) || (sum < 0)) {
                valid = false;
                break;
            }
        }

        return valid;

    }

    public void solve() {

        double[] uRef = null;
        int scenarios = B[0].length;
        int resources = b.length;
        int[] indicesScenarios;
        int[] indicesResources;
        uRef = new double[scenarios];
        try
        {
            for (int c = 1; c <= scenarios; c++)
            {
                if (resources >= c)
                {
                	// we only support the case in which the number of resources
                	// is greater than the considered scenarios

                	CombinationGenerator scenarioCombination = new CombinationGenerator(scenarios, c);
                    // System.out.println("----scenarios " + c + " -----");
                    while (scenarioCombination.hasMore())
                    {
                        indicesScenarios = scenarioCombination.getNext();

                        CombinationGenerator resourceCombination = new CombinationGenerator(resources, c);
                        while (resourceCombination.hasMore())
                        {
                            indicesResources = resourceCombination.getNext();

                            // create a system of equations

                            double utiliz[] = new double[c];
                            A = new double[c][c + 1];
                            for (int m = 0; m < c; m++)
                            {
                                for (int n = 0; n < c; n++)
                                {
                                    A[m][n] = B[indicesResources[m]][indicesScenarios[n]];
                                }
                            }

                            // find a device shared by all classes under the microscop

                            int refDev = -1;

                            for (int i = 0; i < c; i++)
                            {
                                refDev = i; // this might be shared
                                for (int j = 0; j < c; j++)
                                {
                                    if (A[i][j] == 0)
                                        refDev = -1;
                                }
                                if (refDev >= 0)
                                    break;
                            }

                            // normalize matrix A

                            if (refDev > 0)
                                for (int i = 0; i < c; i++)
                                    for (int j = 0; j < c; j++)
                                        A[i][j] = A[i][j] / A[refDev][j];

                            // solve the system

                            for (int m = 0; m < c; m++)
                            {
                                A[m][c] = b[indicesResources[m]];
                            } // add the vector b

                            try
                            {
                                utiliz = Matrix.linSolve(A);

                                // create the utilization vector of the ref
                                // resource

                                for (int l = 0; l < uRef.length; l++)
                                {
                                    uRef[l] = 0;
                                }
                                for (int l = 0; l < c; l++)
                                {
                                    uRef[indicesScenarios[l]] = utiliz[l];
                                }

                                // validate the solutions
                                if (validSolution(B, b, uRef))
                                {
                                    ep.addPoints(computeExtremePoints(B, b, uRef));
                                }
                            }
                            catch (Exception ex)
                            {
                            	// do nothing; not all equations are valid
                            }
                        }
                    }
                }// end if
            }// end for
            // print all extreme points
            // ep.print();

        } catch (Exception e) {
 //           String s = e.getMessage();
 //           System.out.println(e);
        }
    }
}