/*
 * Created on Jan 26, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package opera.Core;

import java.util.ArrayList;
import java.util.Iterator;
//import java.util.Vector;

/**
 * @author marin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ExtremePoints {
	
	ArrayList<double[]> exPoints= null;
	
	public ExtremePoints()
	{
		exPoints=new ArrayList<double[]>();
	}
	
	void addPoint (double[] u)
	{
		exPoints.add(u);
	}

	void addPoints(ArrayList<double[]> points)
	{
		Iterator<double[]> iter = points.iterator();
		while (iter.hasNext())
		{
			exPoints.add(iter.next());
		}
	}

	boolean isEmpty()
	{
		return exPoints.isEmpty();
	}

	ArrayList<double[]> getAll()
    {
    	return exPoints;
    }

    void print()
    {
		Iterator<double[]> iter = exPoints.iterator();
		while (iter.hasNext())
		{
			double[] point= iter.next();
			for (int i = 0; i < point.length; i++)
			{
				System.out.print(" " + point[i]);
			}
		    System.out.println("");
		}	
    }
}
