package ceraslabs.hogna.workload;

import java.util.Random;

public class RandomStringGenerator
{
	private static final char[] symbols = new char[36];
	private final Random random = new Random();
	private final char[] buf;

	static
	{
		for (int idx = 0; idx < 10; ++idx)
		{
			RandomStringGenerator.symbols[idx] = (char) ('0' + idx);
		}
		for (int idx = 10; idx < 36; ++idx)
		{
			RandomStringGenerator.symbols[idx] = (char) ('a' + idx - 10);
		}
	}

	public RandomStringGenerator(int length)
	{
		if (length < 1)
		{
			throw new IllegalArgumentException("length < 1: " + length);
		}
		buf = new char[length];
	}

	public String nextString()
	{
		for (int idx = 0; idx < buf.length; ++idx)
		{
			buf[idx] = RandomStringGenerator.symbols[random.nextInt(RandomStringGenerator.symbols.length)];
		}
		return new String(buf);
	}
}
