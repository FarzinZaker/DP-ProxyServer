package ceraslabs.hogna.data;

public class DataColumn
{
	private String m_id;
	private String m_label;
	private String m_pattern;
	private DataType m_dataType;
	
	public DataColumn(String id, String label, DataType dataType, String pattern)
	{
		this.m_id = id;
		this.m_label = label;
		this.m_pattern = pattern;
		this.m_dataType = dataType;
	}
	
	public DataColumn(String id, String label, DataType dataType)
	{
		this(id, label, dataType, null);
	}
	
	public DataColumn(String id, String label)
	{
		this(id, label, DataType.TEXT, null);
	}
	
	public DataColumn(String id)
	{
		this(id, id, DataType.TEXT, null);
	}
	
	public DataColumn(String id, DataType dataType, String pattern)
	{
		this(id, id, dataType, pattern);
	}
	
	public String GetId()
	{
		return this.m_id;
	}
	
	public String GetLabel()
	{
		return this.m_label;
	}
	
	public String GetPattern()
	{
		return this.m_pattern;
	}
	
	public DataType GetDataType()
	{
		return this.m_dataType;
	}
}
