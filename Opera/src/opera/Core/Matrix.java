package opera.Core;

//
//
// Matrix
//
//
public class Matrix 
{
    //-------------------------
    // exceptions
    //-------------------------
    protected static MatrixException err_incompat =
        new MatrixException("incompatible matrices");

    protected static MatrixException err_singular =
        new MatrixException("singular matrix");

    //-------------------------
    // properties
    //-------------------------
    public static boolean isZero
        (
        double [][] matrix
        )
    {
        for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                if (matrix[r][c] != 0.0)
                    return false;
            }
        }

        return true;
    }

    public static boolean isDiagonal
        (
        double [][] matrix
        )
    {
        for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                if (r == c)
                {	//Tao: The diagonal entries themselves may or may not be zero. Jan. 26, 2012
                    //if (matrix[r][c] == 0.0)
                    //    return false;
                }
                else
                {
                    if (matrix[r][c] != 0.0)
                        return false;
                }
            }
        }

        return true;
    }

    public static boolean isIdentity
        (
        double [][] matrix
        )
    {
    	//add condition checking by Tao, Jan. 25, 2012
    	if (matrix[0].length != matrix.length)
            return false;
    	
    	for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                if (r == c)
                {
                    if (matrix[r][c] != 1.0)
                        return false;
                }
                else
                {
                    if (matrix[r][c] != 0.0)
                        return false;
                }
            }
        }

        return true;
    }

    public static boolean isTridiagonal
        (
        double [][] matrix
        )
    {
        for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                if (r != c)
                {
                    if (r > c)
                    {
                        if ((r - c) > 1)
                        {
                            if (matrix[r][c] != 0.0)
                                return false;
                        }
                    }
                    else
                    {
                        if ((c - r) > 1)
                        {
                            if (matrix[r][c] != 0.0)
                                return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public static boolean isUpperTriangular
        (
        double [][] matrix
        )
    {
        for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                if (r > c)
                {
                    if (matrix[r][c] != 0.0)
                        return false;
                }
            }
        }

        return true;
    }

    public static boolean isLowerTriangular
        (
        double [][] matrix
        )
    {
        for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                if (r < c)
                {
                    if (matrix[r][c] != 0.0)
                        return false;
                }
            }
        }

        return true;
    }

    public static boolean isPermutation
        (
        double [][] matrix
        )
    {
        if (matrix[0].length != matrix.length)
            return false;
        //added by Tao for a matrix with 1 row and 1 column, Jan. 27, 2012
        if(matrix[0].length == 1)
        {	if (matrix[0][0] == 1.0)
        		return true;
        	else
        		return false;
        }
        
        int [] ctags = new int [matrix[0].length];
        int [] rtags = new int [matrix.length];

        for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                if (matrix[r][c] != 0.0)
                {
                    if ((matrix[r][c] > 1.0) || (rtags[r] == 1) || (ctags[c] == 1))
                        return false;

                    rtags[r] = 1;
                    ctags[c] = 1;
                }
            }
        }

        return true;
    }
    
    //modified by Tao, Jan. 26, 2012
    public static boolean isSingular
        (
        double [][] matrix
        )
    {
        if (matrix[0].length != matrix.length)
            return false;

        /*double [] csum = new double[matrix[0].length];
        double [] rsum = new double[matrix.length];

        for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                csum[c] += matrix[r][c];
                rsum[r] += matrix[r][c];
            }
        }

        for (int i = 0; i < matrix.length; ++i)
        {
            if ((csum[i] == 0.0) || (rsum[i] == 0.0))
                return true;
        }
        
        return false;
		*/
        if (det(matrix) == 0.0)
        	return true;
        else
        	return false;
    }

    //-------------------------
    // methods
    //-------------------------

    public static double [] makeRowVector
        (
        double [][] matrix,
        int row
        )
    {
        double [] rvec = new double [matrix.length];

        for (int c = 0; c < matrix[0].length; ++c)
            rvec[c] = matrix[row][c];

        return rvec;
    }

    public static double [] makeColVector
        (
        double [][] matrix,
        int col
        )
    {
        double [] cvec = new double [matrix[0].length];

        for (int r = 0; r < matrix.length; ++r)
            cvec[r] = matrix[r][col];

        return cvec;
    }

    public static double [][] transpose
        (
        double [][] matrix
        )
    {
        double [][] result = new double [matrix[0].length][matrix.length];

        for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                result[c][r] = matrix[r][c];
            }
        }

        return result;
    }

    // scalar addition
    public static double [][] add
        (
        double [][] matrix1,
        double [][] matrix2
        )
    {
        if ((matrix1.length    != matrix2.length)
        ||  (matrix1[0].length != matrix2[0].length))
            throw err_incompat;

        double [][] result = new double [matrix1.length][matrix1[0].length];

        for (int r = 0; r < matrix1.length; ++r)
        {
            for (int c = 0; c < matrix1[0].length; ++c)
            {
                result[r][c] = matrix1[r][c] + matrix2[r][c];
            }
        }

        return result;
    }
    
    public static double [][] add
        (
        double [][] matrix,
        double x
        )
    {
        double [][] result = new double [matrix.length][matrix[0].length];

        for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                result[r][c] = matrix[r][c] + x;
            }
        }

        return result;
    }

    // scalar subtration
    public static double [][] sub
        (
        double [][] matrix1,
        double [][] matrix2
        )
    {
        if ((matrix1.length    != matrix2.length)
        ||  (matrix1[0].length != matrix2[0].length))
            throw err_incompat;

        double [][] result = new double [matrix1.length][matrix1[0].length];

        for (int r = 0; r < matrix1.length; ++r)
        {
            for (int c = 0; c < matrix1[0].length; ++c)
            {
                result[r][c] = matrix1[r][c] - matrix2[r][c];
            }
        }

        return result;
    }

    public static double [][] sub
        (
        double [][] matrix,
        double x
        )
    {
        double [][] result = new double [matrix.length][matrix[0].length];

        for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                result[r][c] = matrix[r][c] - x;
            }
        }

        return result;
    }

    // scalar multiplication
    public static double [][] mul
        (
        double [][] matrix1,
        double [][] matrix2
        )
    {
        if ((matrix1.length    != matrix2.length)
        ||  (matrix1[0].length != matrix2[0].length))
            throw err_incompat;

        double [][] result = new double [matrix1.length][matrix1[0].length];

        for (int r = 0; r < matrix1.length; ++r)
        {
            for (int c = 0; c < matrix1[0].length; ++c)
            {
                result[r][c] = matrix1[r][c] * matrix2[r][c];
            }
        }

        return result;
    }

    public static double [][] mul
        (
        double [][] matrix,
        double x
        )
    {
        double [][] result = new double [matrix.length][matrix[0].length];

        for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                result[r][c] = matrix[r][c] * x;
            }
        }

        return result;
    }

    // scalar division
    public static double [][] div
        (
        double [][] matrix1,
        double [][] matrix2
        )
    {
        if ((matrix1.length    != matrix2.length)
        ||  (matrix1[0].length != matrix2[0].length))
            throw err_incompat;

        double [][] result = new double [matrix1.length][matrix1[0].length];

        for (int r = 0; r < matrix1.length; ++r)
        {
            for (int c = 0; c < matrix1[0].length; ++c)
            {
                result[r][c] = matrix1[r][c] / matrix2[r][c];
            }
        }

        return result;
    }

    public static double [][] div
        (
        double [][] matrix,
        double x
        )
    {
        double [][] result = new double [matrix.length][matrix[0].length];

        for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                result[r][c] = matrix[r][c] / x;
            }
        }

        return result;
    }

    // negation
    public static double [][] neg
        (
        double [][] matrix
        )
    {
        double [][] result = new double [matrix.length][matrix[0].length];

        for (int r = 0; r < matrix.length; ++r)
        {
            for (int c = 0; c < matrix[0].length; ++c)
            {
                result[r][c] = -matrix[r][c];
            }
        }

        return result;
    }

    // scalar comparison
    public static boolean equal
        (
        double [][] matrix1,
        double [][] matrix2
        )
    {
        if ((matrix1.length    != matrix2.length)
        ||  (matrix1[0].length != matrix2[0].length))
            throw err_incompat;

        for (int r = 0; r < matrix1.length; ++r)
        {
            for (int c = 0; c < matrix1[0].length; ++c)
            {
                if (matrix1[r][c] != matrix2[r][c])
                    return false;
            }
        }

        return true;
    }

    public static boolean [][] compEqual
        (
        double [][] matrix1,
        double [][] matrix2
        )
    {
        if ((matrix1.length    != matrix2.length)
        ||  (matrix1[0].length != matrix2[0].length))
            throw err_incompat;

        boolean [][] result = new boolean [matrix1.length][matrix1[0].length];

        for (int r = 0; r < matrix1.length; ++r)
        {
            for (int c = 0; c < matrix1[0].length; ++c)
            {
                result[r][c] = matrix1[r][c] == matrix2[r][c];
            }
        }

        return result;
    }

    public static boolean [][] compNotEqual
        (
        double [][] matrix1,
        double [][] matrix2
        )
    {
        if ((matrix1.length    != matrix2.length)
        ||  (matrix1[0].length != matrix2[0].length))
            throw err_incompat;

        boolean [][] result = new boolean [matrix1.length][matrix1[0].length];

        for (int r = 0; r < matrix1.length; ++r)
        {
            for (int c = 0; c < matrix1[0].length; ++c)
            {
                result[r][c] = matrix1[r][c] != matrix2[r][c];
            }
        }

        return result;
    }

    public static boolean [][] compLess
        (
        double [][] matrix1,
        double [][] matrix2
        )
    {
        if ((matrix1.length    != matrix2.length)
        ||  (matrix1[0].length != matrix2[0].length))
            throw err_incompat;

        boolean [][] result = new boolean [matrix1.length][matrix1[0].length];

        for (int r = 0; r < matrix1.length; ++r)
        {
            for (int c = 0; c < matrix1[0].length; ++c)
            {
                result[r][c] = matrix1[r][c] < matrix2[r][c];
            }
        }

        return result;
    }

    public static boolean [][] compGreater
        (
        double [][] matrix1,
        double [][] matrix2
        )
    {
        if ((matrix1.length    != matrix2.length)
        ||  (matrix1[0].length != matrix2[0].length))
            throw err_incompat;

        boolean [][] result = new boolean [matrix1.length][matrix1[0].length];

        for (int r = 0; r < matrix1.length; ++r)
        {
            for (int c = 0; c < matrix1[0].length; ++c)
            {
                result[r][c] = matrix1[r][c] > matrix2[r][c];
            }
        }

        return result;
    }

    // matrix multiplucation
    public static double [][] mulMatrix
        (
        double [][] matrix1,
        double [][] matrix2
        )
    {	//System.out.println("matrix2 column length: " + matrix1[0].length + " matrix2 length: " + matrix2.length);
        
    	if (matrix1[0].length != matrix2.length)
            throw err_incompat;

        double [][] result = new double [matrix1.length][matrix2[0].length];

        for (int i = 0; i < matrix1.length; ++i)
        {
            for (int j = 0; j < matrix2[0].length; ++j)
            {
                result[i][j] = 0.0;

                for (int k = 0; k < matrix1[0].length; ++k)
                    result[i][j] += matrix1[i][k] * matrix2[k][j];
            }
        }

        return result;
    }

    // inner product
    public static double [] innerProduct
        (
        double [][] matrix,
        double []   vector
        )
    {
        if (vector.length != matrix[0].length)
            throw err_incompat;

        double [] result = new double [matrix.length];

        for (int i = 0; i < matrix.length; ++i)
        {
            for (int n = 0; n < vector.length; ++n)
            {
                result[i] += matrix[i][n] * vector[n];
            }
        }

        return result;
    }

    // outer product of two vectors
    public static double [][] outerProduct
        (
        double [] vector1,
        double [] vector2
        )
    {
        if (vector1.length != vector2.length)
            throw err_incompat;

        double [][] result = new double [vector1.length][vector1.length];

        for (int i = 0; i < vector1.length; ++i)
        {
            for (int j = 0; j < vector2.length; ++j)
            {
                result[i][j] = vector1[i] * vector2[j];
            }
        }

        return result;
    }

    // calculation euclidean norm
    public static double norm
        (
        double [] vector
        )
    {
        double result = 0.0;
        
        for (int i = 0; i < vector.length; ++i)
            result += vector[i] * vector[i];

        return Math.sqrt(result);
    }

    // calculate determinant value
    public static double det
        (
        double [][] matrix
        )
    {
        if (matrix.length != matrix[0].length)
            throw err_incompat;

        if (matrix.length == 1)
            return matrix[0][0];
        
        //commented by Tao, Jan. 25, 2012
        //if (isSingular(matrix))
        //    return 0.0;

        return detRecursive(matrix);
    }

    // recursive function for determinant (bug fixed by Tao, Jan. 25, 2012)
    protected static double detRecursive
        (
        double [][] matrix
        )
    {
        if (matrix.length == 2)
            return (matrix[0][0] * matrix[1][1]) - (matrix[0][1] * matrix[1][0]);

        double result = 0.0;

        for (int n = 0; n < matrix.length; n++)
        {
            if ((n & 1) == 1)
                result -= matrix[n][0] * detRecursive(createMinor(matrix,n,0));
            else
                result += matrix[n][0] * detRecursive(createMinor(matrix,n,0));
        }

        return result;
    }

    // create a minor matrix (bug fixed by Tao, Jan. 26, 2012)
    public static double [][] createMinor
        (
        double [][] matrix,
        int rdel,
        int cdel
        )
    {
        if ((matrix.length != matrix[0].length) || (matrix.length < 2))
            throw err_incompat;

        double [][] result = new double [matrix.length - 1][matrix[0].length - 1];

        int rdest = 0;

        for (int r = 0; r < matrix.length; r++)
        {
            if (r != rdel)
            {
                int cdest = 0;

                for (int c = 0; c < matrix[0].length; c++)
                {
                    if (c != cdel)
                    {
                        result[rdest][cdest] = matrix[r][c];
                        ++cdest;
                    }
                    //else
                    //	++cdest;
                }
                ++rdest;
            }
            //else
            //    ++rdest;

            
        }

        return result;
    }

    // solve system of linear equations
    public static double [] linSolve
        (
        double [][] matrix
        )
    {
        if (((matrix[0].length - matrix.length) != 1) || (isSingular(matrix)))
            throw err_incompat;

        int i, j, k, max;
        double temp;

        // forward elimination
        for (i = 0; i < matrix.length; ++i)
        {
            max = i;

            for (j = i + 1; j < matrix.length; ++j)
            {
                if (Math.abs(matrix[j][i]) > Math.abs(matrix[max][i]))
                    max = j;
            }

            for (k = i; k < matrix[0].length; ++k)
            {
                temp = matrix[i][k];
                matrix[i][k] = matrix[max][k];
                matrix[max][k] = temp;
            }

            for (j = i + 1; j < matrix.length; ++j)
            {
                for (k = matrix.length; k >= i; --k)
                {
                    matrix[j][k] -= matrix[i][k] * matrix[j][i] / matrix[i][i];

                    if (k == 0) break;
                }
            }
        }

        // backward substitution
        double [] result = new double [matrix.length]; // results

        for (j = matrix.length - 1; ; --j)
        {
            temp = 0.0;

            for (k = j + 1; k < matrix.length; ++k)
                temp += matrix[j][k] * result[k];

            result[j] = (matrix[j][matrix.length] - temp) / matrix[j][j];

            if (j == 0) break;
        }

        return result;
    }

    // LUP decomposition
    public static int [] lupDecompose
        (
        double [][] matrix
        )
    {
        // make sure its square (Tao: "matrix.length < 2" --> "matrix.length < 1", Jan. 27, 2012)
        if ((matrix.length != matrix[0].length) || (matrix.length < 1))
            throw err_incompat;

        int i, j, k, k2 = 0, t;
        double p, temp;

        int [] perm = new int [matrix.length];

        // initialize permutation
        for (i = 0; i < matrix.length; ++i)
            perm[i] = i;

        // LU decomposition
        for (k = 0; k < (matrix.length - 1); ++k)
        {
            p = 0.0;

            for (i = k; i < matrix.length; ++i)
            {
                temp = Math.abs(matrix[i][k]);

                if (temp > p)
                {
                    p  = temp;
                    k2 = i;
                }
            }

            if (p == 0.0)
                throw err_singular;

            // exchange
            t        = perm[k];
            perm[k]  = perm[k2];
            perm[k2] = t;

            for (i = 0; i < matrix.length; ++i)
            {
                temp          = matrix[k][i];
                matrix[k][i]  = matrix[k2][i];
                matrix[k2][i] = temp;
            }

            for (i = k + 1; i < matrix.length; ++i)
            {
                matrix[i][k] /= matrix[k][k];

                for (j = k + 1; j < matrix.length; ++j)
                    matrix[i][j] -= matrix[i][k] * matrix[k][j];
            }
        }

        // return values
        return perm;
}

    // LUP decomposition (call w/ result of LUPDecomp)
    public static double [] lupSolve
        (
        double [][] matrix,
        double []   vector,
        int    []   perm
        )
{
        if ((matrix.length != vector.length) || (matrix.length != perm.length))
            throw err_incompat;

        int i, j, j2;
        double sum, u;
        double [] y = new double [matrix.length];
        double [] x = new double [matrix.length];

        for (i = 0; i < matrix.length; ++i)
        {
            sum = 0.0;
            j2  = 0;

            for (j = 1; j <= i; ++j)
            {
                sum += matrix[i][j2] * y[j2];
                ++j2;
            }

            y[i] = vector[perm[i]] - sum;
        }

        i = matrix.length - 1;

        while (true)
        {
            sum = 0.0;
            u   = matrix[i][i];

            for (j = i + 1; j < matrix.length; ++j)
                sum += matrix[i][j] * x[j];

            x[i] = (y[i] - sum) / u;

            if (i == 0) break;

            --i;
        }

        return x;
    }

    // LUP inversion (call w/ result of LUPDecomp)
    public static double [][] lupInvert
        (
        double [][] matrix,
        int    []   perm
        )
    {
        int i, j;

        double [] p = new double [matrix.length];

        double [][] result = new double [matrix.length][matrix.length];

        for (j = 0; j < matrix.length; ++j)
        {
            for (i = 0; i < matrix.length; ++i)
                p[i] = 0.0;

            p[j] = 1.0;

            p = lupSolve(matrix,p,perm);

            for (i = 0; i < matrix.length; ++i)
                result[i][j] = p[i];
        }

        return result;
    }
	 
	 public static void print(double[][] matrix)
    {	int row = matrix.length;
    	int col = matrix[0].length;
    	for (int i = 0; i < row; i++)
    	{	for (int j = 0; j < col; j++)
    		{	System.out.print(matrix[i][j] + " ");
    		}
    		System.out.println();
    	}
    	System.out.println();
    	
    }

	public static void main(String[] argv){
		
		//testing QR factorization
		//double[][] a = {{2.0, 0.0, 1.0}, {6.0, 2.0, 0.0},{-3.0,-1.0,-1.0}};
		double[][] a = {{1.0, 1.0}, {2.0, 3.0},{2.0,1.0}};
		//double[][] a = {{2.0, 0.0, 1.0},{-3.0,-1.0,-1.0}};
		//double[][] a = {{1.0, 1.0}, {2.0, 3.0},{2.0,1.0},{5.0,6.0}};
		//double[][] a = {{2.0, 0.0, 1.0, 5.0},{-3.0,-1.0,-1.0,6.0}};

		System.out.println("H: " );
		Matrix.print(getHouseholderMatrix(a,1));
		 
		System.out.println("R: ");
		Matrix.print(getQRRMatrix(a));
		System.out.println("Q: ");
		Matrix.print(getQRQMatrix(a));
		System.out.println("QR");
		Matrix.print(Matrix.mulMatrix(getQRQMatrix(a),getQRRMatrix(a)));
		System.out.println("rank of matrix: ");
		System.out.println(getMatrixRank(a, 0.000001));
		//System.exit(0);
		
		
	    Matrix mat=new Matrix();
	    int m=2;
	    int n=2;
	    double[] other= new double[1];
	    double[][] A={{0.5,1.8},{0.9,1.4}};
	    double[][] B={{0.5,1.8,1},{0.9,1.4,1}};
	    double [][] Z ={{8,1,6},{3,5,7},{4,9,2}
	    };
	    double[] b={1,1};  
	    double[] U={0,0,1,1};
	    double[] system =new double [2];
	    other[0]=0.01;
	    int[] perm= Matrix.lupDecompose(A);
	    double[][] result= Matrix.lupInvert(A,perm);
	    system=Matrix.linSolve(B);
	    System.out.println ("A inverted");
	    for (int i=0;i<m;i++){
	    	System.out.println();  	
	        for (int j=0;j<n;j++){
	           System.out.print(" "+result[i][j]);
	        }
	    }
	    System.out.println ("\nsystem solved ");
	    for (int i=0;i<2;i++){
	           System.out.print(" "+system[i]);
	    }
	    
		perm= Matrix.lupDecompose(Z);
		result= Matrix.lupInvert(Z,perm);
		
		System.out.println();
		for (int i = 0; i < 3; i++)
		{	for (int j = 0; j < 3; j++)
			{ System.out.print(result[i][j]+ " ");
				
			}
			System.out.println();
			
		}
		    
	}
	
	
	//the following methods are used to calculate the matrix rank using householder QR factorization
	/* reference: http://www.cs.ut.ee/~toomas_l/linalg/lin2/node5.html
	 * make sure the number of rows is no more than the number of columns
	 * @param matrix the input matrix
	 * @param sn the sequnce number of the returned householder matrix
	 * @return the householder matrix
	 */
	protected static double[][] getHouseholderMatrix(double[][] matrix, int sn) 
	{  double [][] result = new double[matrix.length][matrix.length];
		double [] householderVec = new double [matrix.length - sn + 1];
		//initialize the householder matrix H as an identity matrix
		for (int i = 0; i < matrix.length; i++)
		{	for (int j = 0; j < matrix.length; j++)
			{	if (i == j)
					result[i][j] = 1.0;
				else 
					result[i][j] = 0.0;
			}
		}
	/*	System.out.println("initialization of H: ");
		Matrix.print(result);
		*/
		//get the householder vector
		
		for (int i = sn - 1; i < matrix.length; i++)
		{
			householderVec[i - sn + 1 ] = matrix[i][sn - 1]; 
		} 
		
		/*System.out.println("initial vector: ");
		for (int i = 0; i < householderVec.length; i++)
		{
			System.out.println(householderVec[i] + "\t"); 
		} 
		*/
		
		double d = 0.0;
		for (int i = 0; i< householderVec.length; i ++)
		{ d += householderVec[i] * householderVec[i];
		}
		d = Math.sqrt(d);
		
		if (householderVec[0] >= 0.0 )
			householderVec[0] += d;
		else
			householderVec[0] -= d;
		//System.out.println("householder vector: ");
		//for (int i = 0; i < householderVec.length; i++)
		//{
		//	System.out.println(householderVec[i] + "\t"); 
		//} 
		
		d = 0.0;
		for (int i = 0; i< householderVec.length; i ++)
		{ d += householderVec[i] * householderVec[i];
		}
		
		
		
		
		double[][] tempMat = new double[householderVec.length][householderVec.length];
		for (int i = 0; i < tempMat.length; i ++)
		{	for (int j = 0; j < tempMat.length; j ++) 
			{	tempMat[i][j] = householderVec[i] *householderVec[j];
			}
			
		}
		/*
		System.out.println("tempMat: ");
		Matrix.print(tempMat);
		*/
		if ( d > 0)
		{	for (int i = 0; i < matrix.length -sn + 1; i++)
			{	for (int j = 0; j < matrix.length -sn + 1; j++)
				{	result[i + sn -1][j + sn - 1] -= tempMat[i][j]* 2.0 / d; 
				}
		
			}
		}
		return result;  
	}
	
	/* requirement: the number of rows <= number of columns + 1
	 * @param matrix the input matrix
	 * @return the R matrix of QR factorization
	 */
	public static double[][] getQRRMatrix(double[][] matrix) 
	{	double [][] result = matrix;
		double [][] temp; 
		for (int i = 1; i <= Math.min(matrix.length-1,matrix[0].length); i++)
		{	temp = getHouseholderMatrix(result, i);
			//System.out.println("H: ");
			//Matrix.print(temp);
			result = Matrix.mulMatrix(temp,result);
			//System.out.println("HA: ");
			//Matrix.print(result);
		}
		return result;  
	}
	
	/* 
	 * @param matrix the input matrix
	 * @return the Q matrix of QR factorization
	 */
	public static double[][] getQRQMatrix(double[][] matrix) 
	{	double [][] result = new double[matrix.length][matrix.length];
		double [][] temp = matrix; 
		double [][] h;
		for (int i = 0; i < result.length; i++)
		{	for (int j = 0; j < result.length; j++)
			{	if ( i == j )
					result[i][j] = 1.0;
				else 
					result[i][j] = 0.0;
			}
		}
		for (int i = 1; i <= Math.min(matrix.length-1,matrix[0].length); i++)
		{ 	h = getHouseholderMatrix(temp, i);
			//.out.println("H: ");
			//Matrix.print(h);
			temp = Matrix.mulMatrix(h,temp);
			result = Matrix.mulMatrix(result, h);
			//System.out.println("result: ");
			//Matrix.print(result);
		}
		return result;  
	}
	
	/* 
	 * @param matrix the input matrix
	 * @param epsilon the number is considered as 0 if the absolute value is no more than epsilon
	 * @return the matrix rank
	 */
	public static int getMatrixRank(double[][] matrix, double epsilon) 
	{	//double[][]tempMatrix;
		//make sure the number of rows is no more than the number of columns
		//if (matrix.length > matrix[0].length)
		//	tempMatrix = Matrix.transpose(matrix);
		//else
		//tempMatrix = matrix;
		int rank = 0;

		int num = Math.min(matrix.length,matrix[0].length);
		//System.out.println("num: " + num);
		double [][] m = getQRRMatrix(matrix);
		//System.out.println("m: ");
		//Matrix.print(m);
		for (int i = 0; i < num; i ++)
		{	if (Math.abs(m[i][i]) > epsilon)
			{	
				rank++;
			}
		}
		
		return rank;	
	}
	
	/* 
	 * @param matrix the input matrix
	 * @param epsilon the number is considered as 0 if the absolute value is no more than epsilon
	 * @return the matrix rank, conditionNumber and min, max value of the diag
	 */
	/*public static double[] getMatrixRankAndConditionNumber(double[][] matrix, double epsilon) 
	{	//double[][]tempMatrix;
		//make sure the number of rows is no more than the number of columns
		//if (matrix.length > matrix[0].length)
		//	tempMatrix = Matrix.transpose(matrix);
		//else
		//tempMatrix = matrix;
		
		//System.out.println("matrix: ");
		//Matrix.print(matrix);
		
		double[] results = new double[4];
		int rank = 0;
		double conditionNumber = 0.0;
		double maxValue = 0.0;
		double minValue = Double.POSITIVE_INFINITY;
		double value = 0.0;
		int num = Math.min(matrix.length,matrix[0].length);
		//System.out.println("num: " + num);
		double [][] m = getQRRMatrix(matrix);
		//System.out.println("m: ");
		//Matrix.print(m);
		//System.out.println("m: ");
		//Matrix.print(m);
		for (int i = 0; i < num; i ++)
		{	value = Math.abs(m[i][i]);
			if ( value > epsilon)
			{	
				rank++;
			}
			if (value > maxValue)
				maxValue = value;
			if (value < minValue)
				minValue = value;
		}
		
		conditionNumber = minValue / maxValue;
		
		
		results[0] = rank;
		results[1] = conditionNumber;
		results[2] = maxValue;
		results[3] = minValue;
		//for(int i = 0; i< results.length; i++)
		//{
		//	System.out.println("results[" + i + "]: " + results[i]);
		//}
		return results;	
	}*/

	
	/* 
	 * @param matrix1 the input matrix1
	 * @param matrix2 the input matrix2
	 * @param delta the acceptable error range
	 * @return if two matrices are equal within the acceptable delta error 
	 */
    public static boolean equalWithDelta (double [][] matrix1, double [][] matrix2, double delta)
    {	if ((matrix1.length    != matrix2.length)
        ||  (matrix1[0].length != matrix2[0].length))
            throw err_incompat;
    	
    	for (int r = 0; r < matrix1.length; ++r)
        {	for (int c = 0; c < matrix1[0].length; ++c)
            {	if (Math.abs(matrix1[r][c] - matrix2[r][c])>= delta)
                    return false;
            }
        }
        return true;
    }
	
    /* 
	 * @param matrix1 the input matrix
	 * @param delta the acceptable error range
	 * @return if all the values in the matrix are zero (within the error range of delta)
	 */
    public static boolean isZeroWithDelta (double [][] matrix, double delta)
    {	for (int r = 0; r < matrix.length; ++r)
    	{	for (int c = 0; c < matrix[0].length; ++c)
    		{	if (Math.abs(matrix[r][c]) >=  delta)
    				return false;
    		}
    	}
    	return true;
    }
    
	/* 
	 * @param matrix the input matrix
	 * @param delta the acceptable small absolute value to be considered as 0
	 * @return a new matrix which set the absolute value smaller than delta to 0 
	 */
    public static double[][] toMatrixWithDelta0 (double [][] matrix, double delta)
    {	double[][] result = new double[matrix.length][matrix[0].length];
    	for (int r = 0; r < matrix.length; ++r)
        {	for (int c = 0; c < matrix[0].length; ++c)
            {	if (Math.abs(matrix[r][c] ) < delta)
                    result[r][c] = 0.0;
	            else
	            	result[r][c] = matrix[r][c];
            }
        }
        return result;
    }
    
	/* 
	 * @param matrix the input matrix
	 * @return a new matrix with the copied values (the original matrix won't be affected) 
	 */
    public static double[][] copyMatrixValues (double [][] matrix)
    {	double[][] result = new double[matrix.length][matrix[0].length];
    	for (int r = 0; r < matrix.length; r++)
        {	for (int c = 0; c < matrix[0].length; c++)
            {	result[r][c] = matrix[r][c];
            }
        }
        return result;
    }
}

