package ceraslabs.hogna.data;

import java.util.ArrayList;
import java.util.List;

public class DataRow
{
	DataTable m_owner;
	private List<Object> m_cells;

	DataRow(DataTable owner)
	{
		this.m_owner = owner;
		this.m_cells = new ArrayList<Object>(this.m_owner.m_columns.size());
		for (int i = 0; i < this.m_owner.m_columns.size(); ++i)
		{
			this.m_cells.add(this.m_owner.m_columns.get(i).GetDataType().GetDefaultValue());
		}
	}

	//public DataRow()
	//{
	//	this.m_owner = null;
	//	this.m_columns = new HashMap<String, Integer>();
	//	this.m_cells = new ArrayList<Object>();
	//}

	public void AddCell(String columnId, Object value)
	{
		if (this.m_owner.m_columnIdxById.containsKey(columnId))
		{
			// get the index of this column
			int idx = this.m_owner.m_columnIdxById.get(columnId);
			this.m_cells.set(idx, this.m_owner.m_columns.get(idx).GetDataType().CreateValue(value));
		}
		else
		{
			throw new RuntimeException("Column [" + columnId + "] dows not exist in the table.");
		}
	}
	
	void AddCellResize()
	{
		for (int i = this.m_cells.size(); i < this.m_owner.m_columns.size();  ++i)
		{
			this.m_cells.add(this.m_owner.m_columns.get(i).GetDataType().GetDefaultValue());
		}
	}

	public Object GetCell(int idx)
	{
		return this.m_cells.get(idx);
	}

	public Object GetCell(String columnId)
	{
		if (this.m_owner.m_columnIdxById.containsKey(columnId))
		{
			return this.m_cells.get(this.m_owner.m_columnIdxById.get(columnId));
		}
		else
		{
			throw new RuntimeException("Column [" + columnId + "] dows not exist.");
		}
	}

	public int GetSize()
	{
		return this.m_cells.size();
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < this.m_cells.size(); ++i)
		{
			String pattern = this.m_owner.m_columns.get(i).GetPattern();
			String strCell = null;
			if (null != pattern)
				strCell = String.format(pattern, this.m_cells.get(i));
			else
				strCell = "" + this.m_cells.get(i);
			sb.append(strCell);
			sb.append(", ");
		}
		
		return sb.toString();
	}
}
