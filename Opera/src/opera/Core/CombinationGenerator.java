package opera.Core;
	//--------------------------------------
// Generate combinations.
//--------------------------------------


public class CombinationGenerator {

  private int[] a;
  private int n;
  private int k;
  private int numLeft;
  private int total;

  //------------
  // Constructor
  //------------

  public CombinationGenerator (int n, int k) {
    if (k > n) {
      throw new IllegalArgumentException ();
    }
    if (n < 1) {
      throw new IllegalArgumentException ();
    }
    this.n = n;
    this.k = k;
    a = new int[k];
    int nFact = getFactorial (n);
    int rFact = getFactorial (k);
    int nminusrFact = getFactorial (n - k);
    total = nFact/(rFact*nminusrFact);
    reset ();
  }

  //------
  // Reset
  //------

  public void reset () {
    for (int i = 0; i < a.length; i++) {
      a[i] = i;
    }
    numLeft = total;
  }

  //------------------------------------------------
  // Return number of combinations not yet generated
  //------------------------------------------------

  public int getNumLeft () {
    return numLeft;
  }

  //-----------------------------
  // Are there more combinations?
  //-----------------------------

  public boolean hasMore () {
    if (numLeft<=0) return false;
    else return true;
  }

  //------------------------------------
  // Return total number of combinations
  //------------------------------------

  public int getTotal () {
    return total;
  }

  //------------------
  // Compute factorial
  //------------------

  private static int getFactorial (int n) {
    int factorial = 1;
    for (int i = n; i > 1; i--) {
      factorial = factorial*i;
    }
    return factorial;
  }

  //--------------------------------------------------------
  // Generate next combination (algorithm from Rosen p. 286)
  //--------------------------------------------------------

  public int[] getNext () {

    if (numLeft==total) {
      numLeft = numLeft-1;
      return a;
    }

    int i = k - 1;
    while (a[i] == n - k + i) {
      i--;
    }
    a[i] = a[i] + 1;
    for (int j = i + 1; j < k; j++) {
      a[j] = a[i] + j - i;
    }

    numLeft = numLeft-1;
    return a;

  }
}

	



