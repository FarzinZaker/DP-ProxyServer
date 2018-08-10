package ceraslabs.hogna.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataTable
{
	List<DataColumn> m_columns;
	Map<String, Integer> m_columnIdxById;
	private List<DataRow> m_rows;
	
	public DataTable()
	{
		this.m_columns = new ArrayList<DataColumn>();
		this.m_columnIdxById = new HashMap<String, Integer>();
		this.m_rows = new ArrayList<DataRow>();
	}
	
	public void AddColumn(DataColumn column)
	{
		// check if a column with this name already exists
		String columnId = column.GetId();
		if (this.m_columnIdxById.containsKey(columnId))
		{
			throw new RuntimeException("Column [" + columnId + "] already in table.");
		}
		
		this.m_columnIdxById.put(columnId, this.m_columns.size());
		this.m_columns.add(column);
		
		// resize all columns
		for (DataRow row : this.m_rows)
		{
			row.AddCellResize();
		}
	}
	
	public void AddColumn(String columnId)
	{
		this.AddColumn(new DataColumn(columnId));
	}
	
	public void AddRow(DataRow row)
	{
		if (row.m_owner == this)
		{
			this.m_rows.add(row);
		}
		else
		{
			throw new RuntimeException("The datarow does not belong to current table.");
		}
	}

	public DataRow CreateRow()
	{
		return new DataRow(this);
	}
	
	public DataColumn[] GetColumns()
	{
		DataColumn[] columns = new DataColumn[this.m_columns.size()];
		
		for (int i = 0; i < this.m_columns.size(); ++i)
		{
			columns[i] = this.m_columns.get(i);
		}
		
		return columns;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		for (DataColumn column : this.m_columns)
		{
			sb.append(column.GetLabel());
			sb.append(", ");
		}
		sb.append("\n");
		
		for (DataRow row : this.m_rows)
		{
			sb.append(row);
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	public static void main(String... args)
	{
		DataTable dt = new DataTable();
		
		dt.AddColumn("timestamp");
		dt.AddColumn(new DataColumn("CPU W", "CPU W", DataType.NUMBER, "%5.2f"));
		dt.AddColumn("CPU D");
		dt.AddColumn("arr u");
		dt.AddColumn("arr f");
		dt.AddColumn(new DataColumn("throughput", "Throughput", DataType.NUMBER, "%4.0f"));
		
		DataRow row = dt.CreateRow();
		
		row.AddCell("CPU W", 12.6);
		row.AddCell("CPU D", 80.6);
		
		dt.AddRow(row);

		
		row = dt.CreateRow();
		
		row.AddCell("arr f", 12.6);
		row.AddCell("arr u", 80.6);
		row.AddCell("throughput", 100.0);
		
		dt.AddRow(row);

		dt.AddColumn("respT");
		
		System.out.print(dt);
	}
}












