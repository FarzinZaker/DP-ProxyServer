package ceraslabs.hogna.data;

public enum DataType
{
	BOOLEAN
	{
		@Override
		Object GetDefaultValue()
		{
			return false;
		}

		@Override
		Object CreateValue(Object value)
		{
			if (null == value)
			{
				return false;
			}
			if (value instanceof Boolean)
			{
				return value;
			}
			try
			{
				return Boolean.parseBoolean(value.toString());
			}
			catch (Exception ex)
			{
				return false;
			}
		}
	},
	NUMBER
	{
		@Override
		Object GetDefaultValue()
		{
			return Double.NaN;
		}

		Object CreateValue(Object value)
		{
			if (null == value)
			{
				return Double.NaN;
			}
			if (value instanceof Number)
			{
				return ((Number)value).doubleValue();
			}
			// not a numeric type, check if we can interpret it as a number
			try
			{
				return Double.parseDouble(value.toString());
			}
			catch (Exception ex)
			{
				return Double.NaN;
			}
		}
	},
	TEXT
	{
		@Override
		Object GetDefaultValue()
		{
			return "";
		}

		@Override
		Object CreateValue(Object value)
		{
			if (null == value)
			{
				return "";
			}
			else
			{
				return value.toString();
			}
		}
	};

	abstract Object GetDefaultValue();
	abstract Object CreateValue(Object value);
}
