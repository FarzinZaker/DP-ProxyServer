package ceraslabs.hogna.workload.Http;

import ceraslabs.hogna.workload.Parameter;
import ceraslabs.hogna.workload.Scenario;

public class ScenarioHttp extends Scenario
{
	private String m_sUrl = null;
	
	public ScenarioHttp() { }
	
	public ScenarioHttp(String name)
	{
		this.m_sName = name;
	}
	
	public void SetUrl(String sUrl) { this.m_sUrl = sUrl; }

	public String GetUrl() { return this.m_sUrl; }

	public void SetHost(String sHost)
	{
		if (this.m_sUrl == null)
		{
			this.m_sUrl = sHost;
			return;
		}

		String sUrl = this.m_sUrl;
		if (this.m_sUrl.startsWith("http://"))
		{
			sUrl = this.m_sUrl.substring(7); // skip the "http://" from the beginning
		}

		int slashPos = sUrl.indexOf('/');
		
		if (slashPos >= 0)
		{
			this.m_sUrl = "http://" + sHost + sUrl.substring(slashPos);
		}
		else
		{
			this.m_sUrl = "http://" + sHost + "/";
		}
	}
	
	public String GetRequestURL()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.m_sUrl);
		if (this.m_parameters.size() > 0)
		{
			char connector = '?';
			for(Parameter parameter : this.m_parameters)
			{
				sb.append(connector);
				sb.append(parameter.GetName());
				sb.append("=");
				sb.append(parameter.GetRandomValue());

				connector = '&';
			}
		}
		return sb.toString();
	}
	
	public String toString()
	{
		return this.m_sName;
	}

	public String ToLongString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(this.m_sName);
		sb.append("] [");
		sb.append(this.m_sUrl);

		if (this.m_parameters.size() > 0)
		{
			char connector = '?';
			for(Parameter parameter : this.m_parameters)
			{
				sb.append(connector);
				sb.append('<');
				sb.append(parameter.GetName());
				sb.append(">=<?>");

				connector = '&';
			}
		}
		sb.append(']');
		
		return sb.toString();
	}
}
