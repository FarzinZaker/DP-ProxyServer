package ceraslabs.hogna.utilities;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Convert
{
	public static <T> String ToString(ArrayList<T> list)
	{
		return Convert.ToString(list, "%s");
	}
	
	public static <T> String ToString(ArrayList<T> list, String format)
	{
		StringBuilder result = new StringBuilder();
		result.append('{');
		
		if (list.size() > 0)
		{
			result.append(String.format(format, list.get(0)));
		}

		for (int i = 1; i < list.size(); ++i)
		{
			result.append(", ");
			result.append(String.format(format, list.get(i)));
		}

		result.append('}');
		return result.toString();
	}
	
	public static String ToString(String[] array)
	{
		return Convert.ToString(array, "%s");
	}
	
	public static <T> String ToString(T[] array, String format)
	{
		StringBuilder result = new StringBuilder();
		result.append('{');

		if (array.length > 0)
		{
			result.append(String.format(format, array[0]));
		}

		for (int i = 1; i < array.length; ++i)
		{
			result.append(", ");
			result.append(String.format(format, array[i]));
		}

		result.append('}');
		return result.toString();
	}

	public static String ToString(int[] array)
    {
		return Convert.ToString(array, "%d");
	}

	public static String ToString(int[] array, String formatter)
    {
    	StringBuilder result = new StringBuilder();
    	
    	result.append("{ ");
    	
    	// put the first value from vector.
    	// this value is added separately because there is no ',' before the value
    	if (array.length > 0)
    	{
    		result.append(String.format(formatter, array[0]));
    	}
    	
    	for (int i = 1; i < array.length; ++i)
    	{
    		result.append(", ");
    		result.append(String.format(formatter, array[i]));
    	}
    	
    	result.append('}');

    	return result.toString();
    }

	public static String ToString(double[] array)
	{
		return Convert.ToString(array, "%.2f");
	}

	public static String ToString(double[] array, String formatter)
    {
    	StringBuilder result = new StringBuilder();
    	
    	result.append("{ ");
    	
    	// put the first value from vector.
    	// this value is added separately because there is no ',' before the value
    	if (array.length > 0)
    	{
    		result.append(String.format(formatter, array[0]));
    	}
    	
    	for (int i = 1; i < array.length; ++i)
    	{
    		result.append(", ");
    		result.append(String.format(formatter, array[i]));
    	}
    	
    	result.append('}');
    	
    	return result.toString();
    }

	public static <T> T[] ToArray(ArrayList<T> list, Class<T> type)
	{
		@SuppressWarnings("unchecked")
		T[] array = (T[])Array.newInstance(type, list.size());
	
		for (int i = 0; i < list.size(); ++i)
		{
			array[i] = list.get(i);
		}

		return array;
	}
	
	public static <T> ArrayList<T> ToArrayList(T[] array)
	{
		ArrayList<T> list = new ArrayList<T>(array.length);
		
		for (int i = 0; i < array.length; ++i)
		{
			list.add(array[i]);
		}
		
		return list;
	}
}
