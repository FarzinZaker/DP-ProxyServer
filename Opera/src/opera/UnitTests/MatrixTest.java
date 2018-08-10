package opera.UnitTests;

//import static org.junit.Assert.*;

import opera.Core.Matrix;

//import org.junit.Test;

//import ca.yorku.filter.Matrix;

public class MatrixTest {
	private static final double DELTA = 1.0E-8;
	/*
	@Test
	public void testIsZero() {
		double[][] a = {{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0},{0.0, 1.0, 0.0}};
		double[][] b = {{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0},{0.0, 0.0, 0.0}};
		double[][] c = {{0.0, 0.0}, {0.0, 0.0},{0.0, 0.0}};
		double[][] d = {{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}};
		double[][] e = {{1.0, 0.0}, {0.0, 3.0},{0.0, 3.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		assertFalse(Matrix.isZero(a));
		assertTrue(Matrix.isZero(b));
		assertTrue(Matrix.isZero(c));
		assertTrue(Matrix.isZero(d));
		assertFalse(Matrix.isZero(e));
		assertTrue(Matrix.isZero(f));
		assertFalse(Matrix.isZero(g));
	}


	@Test
	public void testIsDiagonal() {
		//http://en.wikipedia.org/wiki/Diagonal_matrix
		double[][] a = {{1.0, 1.0, 1.0}, {2.0, 3.0, 4.0},{2.0, 1.0, 3.0}};
		double[][] b = {{1.0, 0.0, 0.0}, {0.0, 3.0, 0.0},{0.0, 0.0, 3.0}};
		double[][] c = {{1.0, 0.0}, {0.0, 3.0},{0.0, 0.0}};
		double[][] d = {{1.0, 0.0, 0.0}, {0.0, 3.0, 0.0}};
		double[][] e = {{1.0, 0.0, 0.0}, {0.0, 3.0, 0.0},{0.0, 0.0, 0.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		assertFalse(Matrix.isDiagonal(a));
		assertTrue(Matrix.isDiagonal(b));
		assertTrue(Matrix.isDiagonal(c));
		assertTrue(Matrix.isDiagonal(d));
		assertTrue(Matrix.isDiagonal(e));
		assertTrue(Matrix.isDiagonal(f));
		assertTrue(Matrix.isDiagonal(g));
	}

	@Test
	public void testIsIdentity() {
		//http://en.wikipedia.org/wiki/Identity_matrix
		double[][] a = {{1.0, 1.0, 1.0}, {2.0, 3.0, 4.0},{2.0, 1.0, 3.0}};
		double[][] b = {{1.0, 0.0, 0.0}, {0.0, 1.0, 0.0},{0.0, 0.0, 1.0}};
		double[][] c = {{1.0, 0.0}, {0.0, 1.0},{0.0, 0.0}};
		double[][] d = {{1.0, 0.0, 0.0}, {0.0, 1.0, 0.0}};
		double[][] e = {{1.0, 0.0, 0.0}, {0.0, 1.0, 0.0},{0.0, 0.0, 2.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		double[][] h = {{2.0}};
		assertFalse(Matrix.isIdentity(a));
		assertTrue(Matrix.isIdentity(b));
		assertFalse(Matrix.isIdentity(c));
		assertFalse(Matrix.isIdentity(d));
		assertFalse(Matrix.isIdentity(e));
		assertFalse(Matrix.isIdentity(f));
		assertTrue(Matrix.isIdentity(g));
		assertFalse(Matrix.isIdentity(h));
	}

	@Test
	public void testIsTridiagonal() {
		//http://en.wikipedia.org/wiki/Tridiagonal_matrix
		double[][] a = {{1.0, 1.0, 1.0, 1.0, 1.0}, {2.0, 3.0, 4.0, 5.0, 6.0},{2.0, 1.0, 3.0, 2.0, 1.0}, {2.0, 3.0, 4.0, 5.0, 6.0},{2.0, 1.0, 3.0, 2.0, 1.0}};
		double[][] b = {{1.0, 4.0, 0.0, 0.0}, {3.0, 4.0, 1.0, 0.0},{0.0, 2.0, 3.0, 4.0}, {0.0, 0.0, 1.0, 3.0}};
		double[][] c = {{1.0, 1.0}, {1.0, 1.0},{1.0, 1.0}};
		double[][] d ={{1.0, 1.0, 0.0}, {1.0, 1.0, 1.0},{0.0, 1.0, 1.0}};
		double[][] e = {{1.0, 0.0, 0.0}, {0.0, 1.0, 0.0},{0.0, 0.0, 0.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		assertFalse(Matrix.isTridiagonal(a));
		assertTrue(Matrix.isTridiagonal(b));
		assertFalse(Matrix.isTridiagonal(c));
		assertTrue(Matrix.isTridiagonal(d));
		assertTrue(Matrix.isTridiagonal(e));
		assertTrue(Matrix.isTridiagonal(f));
		assertTrue(Matrix.isTridiagonal(g));
	}

	@Test
	public void testIsUpperTriangular() {
		//http://en.wikipedia.org/wiki/Triangular_matrix
		double[][] a = {{1.0, 1.0, 1.0, 1.0, 1.0}, {2.0, 3.0, 4.0, 5.0, 6.0},{2.0, 1.0, 3.0, 2.0, 1.0}, {2.0, 3.0, 4.0, 5.0, 6.0},{2.0, 1.0, 3.0, 2.0, 1.0}};
		double[][] b = {{1.0, 4.0, 0.0, 0.0}, {0.0, 4.0, 1.0, 0.0},{0.0, 0.0, 3.0, 4.0}, {0.0, 0.0, 0.0, 3.0}};
		double[][] c = {{1.0, 4.0, 0.0, 0.0}, {1.0, 4.0, 0.0, 0.0},{0.0, 0.0, 3.0, 4.0}, {0.0, 0.0, 0.0, 3.0}};
		double[][] d ={{1.0, 0.0, 0.0}, {0.0, 1.0, 0.0},{0.0, 0.0, 1.0}};
		double[][] e = {{1.0, 0.0}, {0.0, 3.0},{0.0, 3.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		assertFalse(Matrix.isUpperTriangular(a));
		assertTrue(Matrix.isUpperTriangular(b));
		assertFalse(Matrix.isUpperTriangular(c));
		assertTrue(Matrix.isUpperTriangular(d));
		assertFalse(Matrix.isUpperTriangular(e));
		assertTrue(Matrix.isUpperTriangular(f));
		assertTrue(Matrix.isUpperTriangular(g));
	}

	@Test
	public void testIsLowerTriangular() {
		double[][] a = {{1.0, 1.0, 1.0, 1.0, 1.0}, {2.0, 3.0, 4.0, 5.0, 6.0},{2.0, 1.0, 3.0, 2.0, 1.0}, {2.0, 3.0, 4.0, 5.0, 6.0},{2.0, 1.0, 3.0, 2.0, 1.0}};
		double[][] b = {{1.0, 4.0, 0.0, 0.0}, {0.0, 4.0, 1.0, 0.0},{0.0, 0.0, 3.0, 4.0}, {0.0, 0.0, 0.0, 3.0}};
		double[][] c = {{1.0, 4.0, 0.0, 0.0}, {1.0, 4.0, 0.0, 0.0},{0.0, 0.0, 3.0, 4.0}, {0.0, 0.0, 0.0, 3.0}};
		double[][] d ={{1.0, 0.0, 0.0}, {0.0, 1.0, 0.0},{0.0, 0.0, 1.0}};
		double[][] e = {{1.0, 0.0}, {0.0, 3.0},{0.0, 3.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		assertFalse(Matrix.isLowerTriangular(Matrix.transpose(a)));
		assertTrue(Matrix.isLowerTriangular(Matrix.transpose(b)));
		assertFalse(Matrix.isLowerTriangular(Matrix.transpose(c)));
		assertTrue(Matrix.isLowerTriangular(Matrix.transpose(d)));
		assertFalse(Matrix.isLowerTriangular(Matrix.transpose(e)));
		assertTrue(Matrix.isLowerTriangular(Matrix.transpose(f)));
		assertTrue(Matrix.isLowerTriangular(Matrix.transpose(g)));
	}

	@Test
	public void testIsPermutation() {
		//http://en.wikipedia.org/wiki/Permutation_matrix
		double[][] a = {{1.0, 0.0, 0.0, 1.0, 1.0}, {2.0, 3.0, 4.0, 5.0, 6.0},{2.0, 1.0, 3.0, 2.0, 1.0}, {2.0, 3.0, 4.0, 5.0, 6.0},{2.0, 1.0, 3.0, 2.0, 1.0}};
		double[][] b = {{0.0, 1.0, 0.0, 0.0}, {1.0, 0.0, 0.0, 0.0},{0.0, 0.0, 1.0, 0.0}, {0.0, 0.0, 0.0, 1.0}};
		double[][] c = {{1.0, 4.0, 0.0, 0.0}, {1.0, 4.0, 0.0, 0.0},{3.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 0.0}};
		double[][] d ={{1.0, 0.0, 0.0}, {0.0, 0.0, 1.0},{0.0, 1.0, 0.0}};
		double[][] e = {{1.0, 0.0}, {0.0, 3.0},{0.0, 3.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		assertFalse(Matrix.isPermutation(a));
		assertTrue(Matrix.isPermutation(b));
		assertFalse(Matrix.isPermutation(c));
		assertTrue(Matrix.isPermutation(d));
		assertFalse(Matrix.isPermutation(e));
		assertFalse(Matrix.isPermutation(f));
		assertTrue(Matrix.isPermutation(g));
	}

	@Test
	public void testIsSingular() {
		//http://mathworld.wolfram.com/SingularMatrix.html
		double[][] a0 = {{0.0, 0.0}, {0.0, 0.0}};
		double[][] a1 = {{0.0, 0.0}, {0.0, 1.0}};
		double[][] a2 = {{0.0, 0.0}, {1.0, 0.0}};
		double[][] a3 = {{0.0, 0.0}, {1.0, 1.0}};
		double[][] a4 = {{0.0, 1.0}, {0.0, 0.0}};
		double[][] a5 = {{0.0, 1.0}, {0.0, 1.0}};
		double[][] a6 = {{1.0, 0.0}, {0.0, 0.0}};
		double[][] a7 = {{1.0, 0.0}, {1.0, 0.0}};
		double[][] a8 = {{1.0, 1.0}, {0.0, 0.0}};
		double[][] a9 = {{1.0, 1.0}, {1.0, 1.0}};
		double[][] b = {{1.0, 0.0, 0.0}, {0.0, 3.0, 0.0},{0.0, 0.0, 3.0}};
		double[][] c = {{1.0, 0.0}, {0.0, 3.0}};
		double[][] d = {{1.0, 0.0, 0.0}, {0.0, 3.0, 0.0}, {1.0, 1.0, 1.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		
		System.out.println("det a0: " + Matrix.det(a0));
		System.out.println("det a1: " + Matrix.det(a1));
		System.out.println("det a2: " + Matrix.det(a2));
		System.out.println("det a3: " + Matrix.det(a3));
		System.out.println("det a4: " + Matrix.det(a4));
		System.out.println("det a5: " + Matrix.det(a5));
		System.out.println("det a6: " + Matrix.det(a6));
		System.out.println("det a7: " + Matrix.det(a7));
		System.out.println("det a8: " + Matrix.det(a8));
		System.out.println("det a9: " + Matrix.det(a9));
		System.out.println("det b: " + Matrix.det(b));
		System.out.println("det c: " + Matrix.det(c));
		System.out.println("det d: " + Matrix.det(d));
		
		
		assertTrue(Matrix.isSingular(a0));
		assertTrue(Matrix.isSingular(a1));
		assertTrue(Matrix.isSingular(a2));
		assertTrue(Matrix.isSingular(a3));
		assertTrue(Matrix.isSingular(a4));
		assertTrue(Matrix.isSingular(a5));
		assertTrue(Matrix.isSingular(a6));
		assertTrue(Matrix.isSingular(a7));
		assertTrue(Matrix.isSingular(a8));
		assertTrue(Matrix.isSingular(a9));
		assertFalse(Matrix.isSingular(b));
		assertFalse(Matrix.isSingular(c));
		assertFalse(Matrix.isSingular(d));
		assertTrue(Matrix.isSingular(f));
		assertFalse(Matrix.isSingular(g));
	}


	@Test
	public void testMakeRowVector() {
		double[][] a = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0},{7.0, 8.0, 9.0}};
		double[] b = {4.0, 5.0, 6.0};
		double [] c = Matrix.makeRowVector(a,1);
		double[][] f = {{0.0}};
		double [] f1 = {0.0};
		double [] f2 = Matrix.makeRowVector(f,0);
		double[][] g = {{1.0}};
		double [] g1 = {1.0};
		double [] g2 = Matrix.makeRowVector(g,0);
		for (int i = 0; i < b.length; i++)
			assertTrue(c[i] == b[i]);
		for (int i = 0; i < f1.length; i++)
			assertTrue(f1[i] == f2[i]);
		for (int i = 0; i < g1.length; i++)
			assertTrue(g1[i] == g2[i]);
		
	}

	@Test
	public void testMakeColVector() {
		double[][] a = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0},{7.0, 8.0, 9.0}};
		double[] b = {1.0, 4.0, 7.0};
		double [] c = Matrix.makeColVector(a,0);
		double[][] f = {{0.0}};
		double [] f1 = {0.0};
		double [] f2 = Matrix.makeColVector(f,0);
		double[][] g = {{1.0}};
		double [] g1 = {1.0};
		double [] g2 = Matrix.makeColVector(g,0);
		for (int i = 0; i < b.length; i++)
			assertTrue(c[i] == b[i]);
		for (int i = 0; i < f1.length; i++)
			assertTrue(f1[i] == f2[i]);
		for (int i = 0; i < g1.length; i++)
			assertTrue(g1[i] == g2[i]);
	}

	@Test
	public void testTranspose() {
		double[][] a = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0},{7.0, 8.0, 9.0}};
		double[][] b = Matrix.transpose(a);
		double[][] c = {{1.0, 4.0, 7.0}, {2.0, 5.0, 8.0},{3.0, 6.0, 9.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		assertTrue(Matrix.equal(b,c));
		assertTrue(Matrix.equal(f,Matrix.transpose(f)));
		assertTrue(Matrix.equal(g,Matrix.transpose(g)));
	}

	@Test
	public void testAddDoubleArrayArrayDoubleArrayArray() {
		double[][] a = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0},{7.0, 8.0, 9.0}};
		double[][] b = {{1.0, 4.0, 7.0}, {2.0, 5.0, 8.0},{3.0, 6.0, 9.0}};
		double[][] c = {{2.0, 6.0, 10.0}, {6.0, 10.0, 14.0},{10.0, 14.0, 18.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		double[][] h = {{1.0}};
		assertTrue(Matrix.equal(Matrix.add(a, b),c));
		assertTrue(Matrix.equal(Matrix.add(f, g),h));
	}

	@Test
	public void testAddDoubleArrayArrayDouble() {
		double[][] a = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0},{7.0, 8.0, 9.0}};
		double b = 5.0;
		double[][] c = {{6.0, 7.0, 8.0}, {9.0, 10.0, 11.0},{12.0, 13.0, 14.0}};
		double[][] g = {{1.0}};
		double[][] h = {{6.0}};
		assertTrue(Matrix.equal(Matrix.add(a, b),c));
		assertTrue(Matrix.equal(Matrix.add(g, b),h));
	}

	@Test
	public void testSubDoubleArrayArrayDoubleArrayArray() {
		double[][] a = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0},{7.0, 8.0, 9.0}};
		double[][] b = {{1.0, 4.0, 7.0}, {2.0, 5.0, 8.0},{3.0, 6.0, 9.0}};
		double[][] c = {{0.0, -2.0, -4.0}, {2.0, 0.0, -2.0},{4.0, 2.0, 0.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		double[][] h = {{-1.0}};;
		assertTrue(Matrix.equal(Matrix.sub(a, b),c));
		assertTrue(Matrix.equal(Matrix.sub(f, g),h));
	}

	@Test
	public void testSubDoubleArrayArrayDouble() {
		double[][] a = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0},{7.0, 8.0, 9.0}};
		double b = 5.0;
		double[][] c = {{-4.0, -3.0, -2.0}, {-1.0, 0.0, 1.0},{2.0, 3.0, 4.0}};
		double[][] g = {{1.0}};
		double[][] h = {{-4.0}};
		assertTrue(Matrix.equal(Matrix.sub(a, b),c));
		assertTrue(Matrix.equal(Matrix.sub(g, b),h));
	}

	@Test
	public void testMulDoubleArrayArrayDoubleArrayArray() {
		double[][] a = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0},{7.0, 8.0, 9.0}};
		double[][] b = {{1.0, 4.0, 7.0}, {2.0, 5.0, 8.0},{3.0, 6.0, 9.0}};
		double[][] c = {{1.0, 8.0, 21.0}, {8.0, 25.0, 48.0},{21.0, 48.0, 81.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		double[][] h = {{0.0}};
		assertTrue(Matrix.equal(Matrix.mul(a, b),c));
		assertTrue(Matrix.equal(Matrix.mul(f, g),h));
	}

	@Test
	public void testMulDoubleArrayArrayDouble() {
		double[][] a = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0},{7.0, 8.0, 9.0}};
		double b = 5.0;
		double[][] c = {{5.0, 10.0, 15.0}, {20.0, 25.0, 30.0},{35.0, 40.0, 45.0}};
		double[][] g = {{1.0}};
		double[][] h = {{5.0}};
		assertTrue(Matrix.equal(Matrix.mul(a, b),c));
		assertTrue(Matrix.equal(Matrix.mul(g, b),h));
	}

	@Test
	public void testDivDoubleArrayArrayDoubleArrayArray() {
		double[][] a = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0},{7.0, 8.0, 9.0}};
		double[][] b = {{1.0, 4.0, 4.0}, {2.0, 5.0, 8.0},{2.0, 5.0, 9.0}};
		double[][] c = {{1.0, 0.5, 0.75}, {2.0, 1.0, 0.75},{3.5, 1.6, 1.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		double[][] h = {{0.0}};
		assertTrue(Matrix.equal(Matrix.div(a, b),c));
		assertTrue(Matrix.equal(Matrix.div(f, g),h));
	}

	@Test
	public void testDivDoubleArrayArrayDouble() {
		double[][] a = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0},{7.0, 8.0, 9.0}};
		double b = 5.0;
		double[][] c = {{0.2, 0.4, 0.6}, {0.8, 1.0, 1.2},{1.4, 1.6, 1.8}};
		double[][] g = {{1.0}};
		double[][] h = {{0.2}};
		assertTrue(Matrix.equal(Matrix.div(a, b),c));
		assertTrue(Matrix.equal(Matrix.div(g, b),h));
	}

	@Test
	public void testNeg() {
		double[][] a = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0},{7.0, 8.0, 9.0}};
		double[][] b = {{-1.0, -2.0, -3.0}, {-4.0, -5.0, -6.0},{-7.0, -8.0, -9.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		double[][] h = {{-1.0}};
		assertTrue(Matrix.equal(Matrix.neg(a),b));
		assertTrue(Matrix.equal(Matrix.neg(f),f));
		assertTrue(Matrix.equal(Matrix.neg(g),h));
	}

	@Test
	public void testEqual() {
		double[][] a = {{1.0, 1.0, 1.0}, {2.0, 3.0, 4.0},{2.0, 1.0, 3.0}};
		double[][] b = {{1.0, 1.0, 1.0}, {1.0, 1.5, 2.0},{2.0, 1.0, 3.0}};
		double[][] c = {{1.0, 1.0, 1.0}, {2.0, 3.0, 4.0},{2.0, 1.0, 3.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		double[][] h = {{1.0}};
		assertFalse(Matrix.equal(a,b));
		assertTrue(Matrix.equal(a,c));
		assertTrue(Matrix.equal(f,f));
		assertTrue(Matrix.equal(g,h));
	}

	@Test
	public void testCompEqual() {
		double[][] a = {{1.0, 1.0, 1.0}, {2.0, 3.0, 4.0},{2.0, 1.0, 3.0}};
		double[][] b = {{1.0, 1.0, 1.0}, {1.0, 1.5, 2.0},{2.0, 1.0, 3.0}};
		boolean[][] c = {{true, true, true}, {false, false, false},{true, true, true}};
		boolean[][] result = Matrix.compEqual(a, b);
		for (int i =0; i < c.length; i++)
		{	for (int j=0; j < c[0].length; j++)
			{	assertTrue(c[i][j] == result[i][j]);
			}
		}
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		boolean[][] h = {{false}};
		boolean[][] h1 = Matrix.compEqual(f, g);
		for (int i =0; i < h.length; i++)
		{	for (int j=0; j < h[0].length; j++)
			{	assertTrue(h[i][j] == h1[i][j]);
			}
		}
	}

	@Test
	public void testCompNotEqual() {
		double[][] a = {{1.0, 1.0, 1.0}, {2.0, 3.0, 4.0},{2.0, 1.0, 3.0}};
		double[][] b = {{1.0, 1.0, 1.0}, {1.0, 1.5, 2.0},{2.0, 1.0, 3.0}};
		boolean[][] c = {{false, false, false}, {true, true, true},{false, false, false}};
		boolean[][] result = Matrix.compNotEqual(a, b);
		for (int i =0; i < c.length; i++)
		{	for (int j=0; j < c[0].length; j++)
			{	assertTrue(c[i][j] == result[i][j]);
			}
		}
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		boolean[][] h = {{true}};
		boolean[][] h1 = Matrix.compNotEqual(f, g);
		for (int i =0; i < h.length; i++)
		{	for (int j=0; j < h[0].length; j++)
			{	assertTrue(h[i][j] == h1[i][j]);
			}
		}
	}

	@Test
	public void testCompLess() {
		double[][] a = {{1.0, 1.0, 1.0}, {2.0, 3.0, 4.0},{2.0, 1.0, 3.0}};
		double[][] b = {{1.0, 1.0, 1.0}, {1.0, 1.5, 2.0},{2.0, 1.0, 3.0}};
		boolean[][] c = {{false, false, false}, {false, false, false},{false, false, false}};
		boolean[][] result = Matrix.compLess(a, b);
		for (int i =0; i < c.length; i++)
		{	for (int j=0; j < c[0].length; j++)
			{	assertTrue(c[i][j] == result[i][j]);
			}
		}
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		boolean[][] h = {{true}};
		boolean[][] h1 = Matrix.compLess(f, g);
		for (int i =0; i < h.length; i++)
		{	for (int j=0; j < h[0].length; j++)
			{	assertTrue(h[i][j] == h1[i][j]);
			}
		}
	}

	@Test
	public void testCompGreater() {
		double[][] a = {{1.0, 1.0, 1.0}, {2.0, 3.0, 4.0},{2.0, 1.0, 3.0}};
		double[][] b = {{1.0, 1.0, 1.0}, {1.0, 1.5, 2.0},{2.0, 1.0, 3.0}};
		boolean[][] c = {{false, false, false}, {true, true, true},{false, false, false}};
		boolean[][] result = Matrix.compGreater(a, b);
		for (int i =0; i < c.length; i++)
		{	for (int j=0; j < c[0].length; j++)
			{	assertTrue(c[i][j] == result[i][j]);
			}
		}
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		boolean[][] h = {{false}};
		boolean[][] h1 = Matrix.compGreater(f, g);
		for (int i =0; i < h.length; i++)
		{	for (int j=0; j < h[0].length; j++)
			{	assertTrue(h[i][j] == h1[i][j]);
			}
		}
	}

	@Test
	public void testMulMatrix() {
		//http://en.wikipedia.org/wiki/Matrix_multiplication
		double[][] a = {{14.0, 9.0, 3.0}, {2.0, 11.0, 15.0},{0.0, 12.0, 17.0},{5.0, 2.0, 3.0}};
		double[][] b = {{12.0, 25.0}, {9.0, 10.0},{8.0, 5.0}};
		double[][] c = {{273.0, 455.0}, {243.0, 235.0},{244.0, 205.0},{102.0, 160.0}};
		assertTrue(Matrix.equal(Matrix.mulMatrix(a, b),c));
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		assertTrue(Matrix.equal(Matrix.mulMatrix(f, g),f));
	}

	@Test
	public void testInnerProduct() {
		//http://en.wikipedia.org/wiki/Matrix_multiplication
		double[][] a = {{14.0, 9.0, 3.0}, {2.0, 11.0, 15.0},{0.0, 12.0, 17.0},{5.0, 2.0, 3.0}};
		double[] b = {12.0, 9.0, 8.0};
		double[] c = {273.0, 243.0, 244.0, 102.0};
		double[] result = Matrix.innerProduct(a, b);
		for (int i =0; i < c.length; i++)
			assertTrue(c[i] == result[i]);
		double[][] f = {{5.0}};
		double[] g = {3.0};
		double [] h = {15.0};
		double [] h1 = Matrix.innerProduct(f, g);
		for (int i =0; i < f.length; i++)
			assertTrue(h[i] == h1[i]);
	}

	@Test
	public void testOuterProduct() {
		//http://en.wikipedia.org/wiki/Outer_product
		double[] a = {1.0, 2.0, 3.0, 4.0};
		double[] b = {3.0, 4.0, 5.0, 6.0};
		double[][] c = {{3.0, 4.0, 5.0, 6.0}, {6.0, 8.0, 10.0, 12.0},{9.0, 12.0, 15.0, 18.0},{12.0, 16.0, 20.0, 24.0}};
		double[][] result = Matrix.outerProduct(a, b);
		assertTrue(Matrix.equal(c, result));
		double[] f = {5.0};
		double[] g = {3.0};
		double [][] h = {{15.0}};
		double [][] h1 = Matrix.outerProduct(f, g);
		assertTrue(Matrix.equal(h, h1));
	}

	@Test
	public void testNorm() {
		double [] a = {3.0, 4.0, 12};
		double b = 13.0;
		double[] f = {0.0};
		double[] g = {1.0};
		assertEquals(Matrix.norm(a), b, DELTA);
		assertEquals(Matrix.norm(f), 0.0, DELTA);
		assertEquals(Matrix.norm(g), 1.0, DELTA);
	}

	@Test
	public void testDet() {
		//http://en.wikipedia.org/wiki/Determinant
		double[][] a = {{1.0, 1.0}, {1.0, 1.0}};
		double[][] b = {{-2.0, 2.0, 3.0}, {-1.0, 1.0, 3.0},{2.0, 0.0, -1.0}};
		double[][] c = {{-2.0, 2.0, -3.0}, {-1.0, 1.0, 3.0},{2.0, 0.0, -1.0}};
		double[][] f = {{0.0}};
		double[][] g = {{1.0}};
		assertEquals(0.0, Matrix.det(a), DELTA);
		assertEquals(6.0, Matrix.det(b), DELTA);
		assertEquals(18.0, Matrix.det(c), DELTA);
		assertEquals(0.0, Matrix.det(f), DELTA);
		assertEquals(1.0, Matrix.det(g), DELTA);
	}

	@Test
	public void testCreateMinor() {
		//http://en.wikipedia.org/wiki/Minor_%28linear_algebra%29
		double[][] a = {{-2.0, 3.0}, {2.0, -1.0}};
		double[][] b = {{-2.0, 2.0, 3.0}, {-1.0, 1.0, 3.0},{2.0, 0.0, -1.0}};
		//Matrix.print(Matrix.createMinor(b, 1, 1));
		assertTrue(Matrix.equal(Matrix.createMinor(b, 1, 1), a));
	}

	@Test
	public void testLinSolve() {
		//double[][] a = {{2.0, 4.0, 26}, {5.0, 8.0, 55.0}};
		//double[] b = {3.0, 5.0};
		double[][] a = {{3.0, 5.0, -1.0, 36.0}, {2.0, -2.0, 3.0, -10.0},{-2.0, 2.0, 1.0, 2.0}};
		double[] b = {3.0, 5.0, -2.0};
		double[] result =Matrix.linSolve(a);
		for(int i = 0; i < b.length; i++)
	    {	//System.out.println(result[i]);
			assertEquals(b[i], result[i], DELTA);
	    }
	}

	@Test
	public void testLupDecompose() {
		//http://reference.wolfram.com/legacy/v5_2/Built-inFunctions/AdvancedDocumentation/LinearAlgebra/LinearAlgebraInMathematica/MatrixComputations/MatrixDecompositions/AdvancedDocumentationLinearAlgebra3.4.0.html
		double[][] a = {{5.0, 2.0, 8.0}, {1.0, 2.0, 3.0},{3.0, 4.0, 1.0}};
		int[] b = {0, 2, 1};
		int[] result =Matrix.lupDecompose(a);
		for(int i = 0; i < b.length; i++)
	    {	assertEquals(b[i], result[i], 0);
	    }
		double[][] f ={{3.0}};
		int [] g = Matrix.lupDecompose(f);
		int [] h = {0};
		assertEquals(g[0], h[0], 0);
	}

	@Test
	public void testLupSolve() {
		double[][] a = {{3.0, 5.0, -1.0}, {2.0, -2.0, 3.0},{-2.0, 2.0, 1.0}};
		double [] v = {36.0, -10.0, 2.0};
		int [] perm = Matrix.lupDecompose(a);
		double[] b = {3.0, 5.0, -2.0};
		double[] result =Matrix.lupSolve(a, v, perm);
		for(int i = 0; i < b.length; i++)
	    {	//System.out.println(result[i]);
			assertEquals(b[i], result[i], DELTA);
	    }
	}

	@Test
	public void testLupInvert() {
		//http://www.zweigmedia.com/RealWorld/tutorialsf1/frames3_3.html
		double[][] a = {{1.0, 0.0, -2.0}, {4.0, 1.0, 0.0},{1.0, 1.0, 7.0}};
		int[] perm= Matrix.lupDecompose(a);
	    double[][] result= Matrix.lupInvert(a,perm);
	    double[][] a_1 = {{7.0, -2.0, 2.0}, {-28.0, 9.0, -8.0},{3.0, -1.0, 1.0}};
	    for(int i = 0; i < a_1.length; i++)
	    {	for (int j = 0; j < a_1[0].length; j++)
	    		assertEquals(a_1[i][j], result[i][j], DELTA);
	    }
	    double[][] f ={{2.0}};
	    perm= Matrix.lupDecompose(f);
	    double[][] f1= Matrix.lupInvert(f,perm);
	    assertEquals(f1[0][0], 0.5, 0);
	}

	@Test
	public void testPrint() {
		double[][] a = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0},{7.0, 8.0, 9.0}};
		Matrix.print(a); 
	}

	
	@Test
	public void testGetHouseholderMatrix() {
		double[][] a0 = {{4.0, 1.0, -2.0, 2.0}, {1.0, 2.0, 0.0, 1.0},{-2.0, 0.0, 3.0, -2.0}, {2.0, 1.0, -2.0, -1.0}};
		double[][] a1 = {{12.0, -51.0, 4.0}, {6.0, 167.0, -68.0},{-4.0, 24.0, -41.0}};
		double[][] a2 = {{1.0, 1.0}, {1.0, 2.0}, {1.0, 3.0}};
		Matrix.print(Matrix.getHouseholderMatrix(a2, 1));
		//Matrix.print(Matrix.getQRQMatrix(a2));
		//Matrix.print(Matrix.getQRRMatrix(a2));
		
	}
	
	
	@Test
	public void testGetQRRMatrix() {
		//http://en.wikipedia.org/wiki/QR_decomposition
		double[][] a1 = {{12.0, -51.0, 4.0}, {6.0, 167.0, -68.0},{-4.0, 24.0, -41.0}};
		double[][] r = {{14.0, 21.0, -14.0}, {0.0, 175.0, -70.0}, {0.0, 0.0, 35.0}};
		double [][] result = Matrix.getQRRMatrix(a1);
		//Matrix.print(result);
		assertTrue(Matrix.isUpperTriangular(Matrix.toMatrixWithDelta0(result, DELTA)));
		for(int i = 0; i < r.length; i++)
	    {	for (int j = 0; j < r[0].length; j++)
	    		//the result from the function is -1 * the expected value from the link
	    		assertEquals(r[i][j] * -1, result[i][j], DELTA);
	    }
	}

	@Test
	public void testGetQRQMatrix() {
		//http://en.wikipedia.org/wiki/QR_decomposition
		double[][] a1 = {{12.0, -51.0, 4.0}, {6.0, 167.0, -68.0},{-4.0, 24.0, -41.0}};
		double[][] r = {{6.0/7.0, -69.0/175.0, -58.0/175.0}, {3.0/7.0, 158.0/175.0, 6.0/175.0}, {-2.0/7.0, 6.0/35.0, -33.0/35.0}};
		double [][] result = Matrix.getQRQMatrix(a1);
		for(int i = 0; i < r.length; i++)
	    {	for (int j = 0; j < r[0].length; j++)
	    		//the result from the function is -1 * the expected value from the link
	    		assertEquals(r[i][j] * -1, result[i][j], DELTA);
	    }
	}

	@Test
	public void testGetMatrixRank() {
		//http://stattrek.com/matrix-algebra/matrix-rank.aspx
		double[][] a = {{0.0, 1.0, 2.0}, {1.0, 2.0, 1.0},{2.0, 7.0, 8.0}};
		double[][] b = {{1.0, 2.0, 3.0}, {2.0, 4.0, 6.0}};
		double[][] c= {{1.0, 0.0, 2.0}, {2.0, 1.0, 0.0},{3.0, 2.0, 1.0}};
		assertEquals(2, Matrix.getMatrixRank(a, DELTA), 0);
		assertEquals(1, Matrix.getMatrixRank(b, DELTA), 0);
		assertEquals(3, Matrix.getMatrixRank(c, DELTA), 0);
	}
	
	@Test
	public void testEqualWithDelta() {
		//http://en.wikipedia.org/wiki/QR_decomposition
		double[][] a1 = {{12.0, -51.0, 4.0}, {6.0, 167.0, -68.0},{-4.0, 24.0, -41.0}};
		double[][] r = {{14.0, 21.0, -14.0}, {0.0, 175.0, -70.0}, {0.0, 0.0, 35.0}};
		double [][] result = Matrix.getQRRMatrix(a1);
		assertFalse(Matrix.equal(Matrix.neg(r), result));
		assertTrue(Matrix.equalWithDelta(Matrix.neg(r), result, DELTA));
	}
	
	@Test
	public void testIsZeroWithDelta() {
		//http://en.wikipedia.org/wiki/QR_decomposition
		double[][] a1 = {{12.0, -51.0, 4.0}, {6.0, 167.0, -68.0},{-4.0, 24.0, -41.0}};
		double[][] r = {{14.0, 21.0, -14.0}, {0.0, 175.0, -70.0}, {0.0, 0.0, 35.0}};
		double [][] result = Matrix.getQRRMatrix(a1);
		double[][] z = Matrix.sub(result, Matrix.neg(r));
		//Matrix.print(z);
		assertFalse(Matrix.isZero(z));
		assertTrue(Matrix.isZeroWithDelta(z, DELTA));
	}
	
	@Test
	public void testToMatrixWithDelta0() {
		//http://en.wikipedia.org/wiki/QR_decomposition
		double[][] a1 = {{12.0, -51.0, 4.0}, {6.0, 167.0, -68.0},{-4.0, 24.0, -41.0}};
		double[][] r = {{14.0, 21.0, -14.0}, {0.0, 175.0, -70.0}, {0.0, 0.0, 35.0}};
		double [][] result = Matrix.getQRRMatrix(a1);
		double[][] z = Matrix.sub(result, Matrix.neg(r));
		//Matrix.print(z);
		assertFalse(Matrix.isZero(z));
		assertTrue(Matrix.isZero(Matrix.toMatrixWithDelta0(z, DELTA)));
	}
	
	@Test
	public void testCopyMatrixValues() {
		double[][] a = {{12.0, -51.0, 4.0}, {6.0, 167.0, -68.0},{-4.0, 24.0, -41.0}};
		double[][] b = Matrix.copyMatrixValues(a);
		assertTrue(Matrix.equal(a, b));
		b[0][0] = 0.0;
		assertFalse(Matrix.equal(a, b));
		//the assign function makes both c and a point to the same matrix. 
		double[][] c = a;
		c[0][0] = 1.0;
		assertTrue(Matrix.equal(c, a));
	}
	*/
}
