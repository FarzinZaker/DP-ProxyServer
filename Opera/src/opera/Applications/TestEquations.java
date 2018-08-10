/*
 * Created on Jan 26, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package opera.Applications;

import opera.Core.SystemOfEquations;

/**
 * @author marin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestEquations {


	public static void main(String[] args) {
		double[][] B = { { 1, 1 }, {
				0.4, 0.8}, {
				0.5, 1.8 }, {
				0.9, 1.4 }
		};

		double[] b = { 1, 1, 1, 1 };

		
		SystemOfEquations soe= new SystemOfEquations(B, b);
		soe.solve();
		
	}
}
