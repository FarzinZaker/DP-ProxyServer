package ceraslabs.hogna.utilities.IO;

import java.io.BufferedReader;
import java.io.FileReader;

public class FileText
{
	public static String GetContent(String fileName) throws Exception
	{
		StringBuilder stringBuilder = new StringBuilder();
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		
		try
		{
			fileReader = new FileReader(fileName);
			bufferedReader = new BufferedReader(fileReader);
			char[] buf = new char[23];
	        int numRead=0;

	        while((numRead = bufferedReader.read(buf)) != -1)
	        {
	            String readData = String.valueOf(buf, 0, numRead);
	            stringBuilder.append(readData);
	        }
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			if (bufferedReader != null)
			{
				bufferedReader.close();
			}
			if (fileReader != null)
			{
				fileReader.close();
			}
		}
		
		return stringBuilder.toString();
	}
}
