package ProxyServer;

import java.util.regex.Pattern;

public class Rule
{
	private Pattern pattern;
	private Actions action;
	/**
	 * @return the pattern
	 */
	public Pattern getPattern()
	{
		return pattern;
	}
	public Rule(String patern, Actions action)
	{
		super();
		this.pattern = Pattern.compile(patern);
		this.action = action;
	}

	public Rule(Pattern patern, Actions action)
	{
		super();
		this.pattern = patern;
		this.action = action;
	}
	/**
	 * @param patern the pattern to set
	 */
	public void setPattern(Pattern patern)
	{
		this.pattern = patern;
	}
	/**
	 * @return the action
	 */
	public Actions getAction()
	{
		return action;
	}
	/**
	 * @param action the action to set
	 */
	public void setAction(Actions action)
	{
		this.action = action;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		String str = pattern.toString().hashCode() + action.toString(); 
		return str.hashCode();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		Rule other = (Rule) obj;
		if (!pattern.toString().equals(other.pattern.toString()))
		{
			return false;
		}
		if (!action.toString().equals(other.action.toString()))
		{
			return false;
		}
		return true;
	}
	
	
}
