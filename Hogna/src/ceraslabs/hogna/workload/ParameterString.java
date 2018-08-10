package ceraslabs.hogna.workload;

import java.util.Random;

public class ParameterString extends Parameter
{
	private static final char[] SYMBOLS = new char[36];
	private static final Random random = new Random();

	static
	{
		for (int idx = 0; idx < 10; ++idx)
		{
			ParameterString.SYMBOLS[idx] = (char) ('0' + idx);
		}
		for (int idx = 10; idx < 36; ++idx)
		{
			ParameterString.SYMBOLS[idx] = (char) ('a' + idx - 10);
		}
	}
	
	public ParameterString(String sName)
	{
		this.m_sName = sName;
	}

	@Override
	public Object GetRandomValue()
	{
		char[] buf = new char[10];
		for (int idx = 0; idx < buf.length; ++idx)
		{
			buf[idx] = ParameterString.SYMBOLS[random.nextInt(ParameterString.SYMBOLS.length)];
		}
		return new String(buf);
	}
}
