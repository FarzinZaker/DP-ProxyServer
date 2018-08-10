package ceraslabs.hogna.monitoring;

import java.util.List;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import ceraslabs.hogna.data.MetricValue;
import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

public class SnmpMonitor extends Monitor
{
	int m_timeout;
	int m_retries;
	
	String m_strOid;
	String m_strCommunity;
	
	String m_strHost;
	String m_strPort;
	String m_strCluster;
	
	public SnmpMonitor() { }
	
/*	public SnmpMonitor(SnmpMonitor other)
	{
		this.m_host = other.m_host;
		this.m_community = other.m_community;
		this.m_oid = other.m_oid;
		this.m_port = other.m_port;
		this.m_retries = other.m_retries;
		this.m_timeout = other.m_timeout;
	}*/
	
	// temporary code
	public void SetHost(String strIp)
	{
		this.m_strHost = strIp;
	}
	// temporary code
	public void SetPort(String strPort)
	{
		this.m_strPort = strPort;
	}
	// temporary code
	public void SetCluster(String strCluster)
	{
		this.m_strCluster = strCluster;
	}
	
	public int GetTimeout()
	{
		return this.m_timeout;
	}
	public void SetTimeout(int timeout)
	{
		this.m_timeout = timeout;
	}

	public int GetRetries()
	{
		return this.m_retries;
	}
	public void SetRetries(int retries)
	{
		this.m_retries = retries;
	}
	
	public String GetOid()
	{
		return this.m_strOid;
	}
	public void SetOid(String sOid)
	{
		this.m_strOid = sOid;
	}
	
	public String GetCommunity()
	{
		return this.m_strCommunity;
	}
	public void SetCommunity(String sCommunity)
	{
		this.m_strCommunity = sCommunity;
	}
	
//	public void SetNetwork(String sNetwork)
//	{
//		this.m_strNetwork = sNetwork;
//	}

	@Override
	public MetricValue[] GetValues()
	{
		String strAddress = this.m_strHost + "/" + this.m_strPort;
		//System.out.println(strAddress);
		CommunityTarget comtarget = new CommunityTarget();
		comtarget.setCommunity(new OctetString(this.m_strCommunity));
		comtarget.setVersion(SnmpConstants.version1);
		comtarget.setAddress(new UdpAddress(strAddress));
		comtarget.setRetries(this.m_retries);
		comtarget.setTimeout(this.m_timeout);

		java.util.ArrayList<MetricValue> result = new java.util.ArrayList<>();

		TransportMapping<UdpAddress> transport = null;
		Snmp snmp = null;
		try
		{
			transport = new DefaultUdpTransportMapping();
			transport.listen();
			snmp = new Snmp(transport);

			TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());      
		    List<TreeEvent> events = treeUtils.getSubtree(comtarget, new OID(this.m_strOid));

		    for (TreeEvent event : events)
		    {
	        	if (event.isError())
	        	{	System.out.println(strAddress);
	        		Trace.WriteLine(TraceLevel.ERROR, "Monitor [%s]: An error occured [%d] while getting the metric value.\n\tError message: [%s].",
	        				this.GetName(), event.getStatus(), event.getErrorMessage());
	        		continue;
	        	}

	        	VariableBinding[] varBindings = event.getVariableBindings();
	        	for (VariableBinding varBinding : varBindings)
	        	{
//	        		System.out.println(
//	        				varBinding.getOid() + 
//	        				" : " + 
//	        				varBinding.getVariable().getSyntaxString() +
//	        				" : " +
//	        				varBinding.getVariable());
	        		result.add(new MetricValue(this.m_strCluster + "/" + this.m_strHost + "/" + varBinding.getOid(),
	        				                   this.m_strName,
	        				                   Double.parseDouble(varBinding.getVariable().toString())));
	        	}
		    }
		}
		catch (Exception ex)
		{
			Trace.WriteLine(TraceLevel.ERROR, "Monitor [%s]: Unknown error.\n\tError message: [%s].", this.GetName(), ex.getLocalizedMessage());
		}
		finally
		{
			if (null != transport)
			{
				try { transport.close(); }
				catch (Exception ex)
				{
					Trace.WriteLine(TraceLevel.ERROR, "Monitor [%s]: Error while closing the transport.\n\tError message: [%s].", this.GetName(), ex.getLocalizedMessage());
				}
			}
			if (null != snmp)
			{
				try { snmp.close(); }
				catch (Exception ex)
				{
					Trace.WriteLine(TraceLevel.ERROR, "Monitor [%s]: Error while closing SNMP session.\n\tError message: [%s].", this.GetName(), ex.getLocalizedMessage());
				}
			}
			else{
				System.out.println("error"+this.m_strHost);
				
			}
//			System.out.println(this.m_strHost);
		}

		return result.toArray(new MetricValue[result.size()]);
	}

	@Override
	public double GetValue()
	{
		String strAddress = this.m_strHost + "/" + this.m_strPort;
		CommunityTarget comtarget = new CommunityTarget();
		comtarget.setCommunity(new OctetString(this.m_strCommunity));
		comtarget.setVersion(SnmpConstants.version1);
		comtarget.setAddress(new UdpAddress(strAddress));
		comtarget.setRetries(this.m_retries);
		comtarget.setTimeout(this.m_timeout);

		PDU pdu = new PDU();
		pdu.setType(PDU.GET);
		pdu.add(new VariableBinding(new OID(this.m_strOid)));


		TransportMapping<UdpAddress> transport = null;
		Snmp snmp = null;
		ResponseEvent response = null;
		try
		{
			transport = new DefaultUdpTransportMapping();
			transport.listen();
			snmp = new Snmp(transport);
			response = snmp.get(pdu, comtarget);

			if(null != response && null != response.getResponse())
			{
				PDU pduResponse = response.getResponse();
				if(pduResponse.getErrorStatus() == SnmpConstants.SNMP_ERROR_SUCCESS)
				{
					VariableBinding vb = (VariableBinding)pduResponse.getVariableBindings().firstElement();
					return Double.parseDouble(vb.getVariable().toString());
				}
				else
				{
					Trace.WriteLine(TraceLevel.ERROR, "Monitor [%s]: An error occured [%d] while getting the metric value.\n\tError message: [%s].",
									this.GetName(), pduResponse.getErrorStatus(), pduResponse.getErrorStatusText());
					Trace.WriteLine(TraceLevel.ERROR, "Monitor [%s]: An error occured [%d] while getting the metric value.\n\tError message: [%s].",
							this.m_strHost, pduResponse.getErrorStatus(), pduResponse.getErrorStatusText());
				}
			}
			else
			{
				Trace.WriteLine(TraceLevel.ERROR, "Monitor [%s]: Timeout while getting data.", this.GetName());
			}
		}
		catch (Exception ex)
		{
			Trace.WriteLine(TraceLevel.ERROR, "Monitor [%s]: Unknown error.\n\tError message: [%s].", this.GetName(), ex.getLocalizedMessage());
		}
		finally
		{
			if (null != transport)
			{
				try { transport.close(); }
				catch (Exception ex)
				{
					Trace.WriteLine(TraceLevel.ERROR, "Monitor [%s]: Error while closing the transport.\n\tError message: [%s].", this.GetName(), ex.getLocalizedMessage());
				}
			}
			if (null != snmp)
			{
				try { snmp.close(); }
				catch (Exception ex)
				{
					Trace.WriteLine(TraceLevel.ERROR, "Monitor [%s]: Error while closing SNMP session.\n\tError message: [%s].", this.GetName(), ex.getLocalizedMessage());
				}
			}
		}

		// failed to get a value;
		return Double.NaN;
	}


//	@Override
//	public Monitor Clone()
//	{
//		SnmpMonitor clone = new SnmpMonitor();
//		this.UptdateClone(clone);
//		
//		clone.m_timeout = this.m_timeout;
//		clone.m_retries = this.m_retries;
//		clone.m_community = this.m_community;
//		clone.m_oid = this.m_oid;
//		
//		return clone;
//	}



	/**
	 * Get the CPU utilization on the specified machine as an average over a minute.
	 * <p>
	 * The value is extracted from OID <code>1.3.6.1.2.1.25.3.3.1.2.2</code>.
	 * 
	 * @param strAddress
	 *                    the address of the machine from where to get the CPU Utilization.
	 * @return
	 *                    a value between 0 and 100. If it fails, for any reason,
	 *                    to get a value from host, -1 is returned.
	 * @throws Exception 
	 *                    if fails to bind to a local socket.
	 */
/*
	public static double GetCpuUtilization(String strAddress) throws Exception
	{
		return SnmpMonitor.GetCpuUtilization(strAddress, ".1.3.6.1.2.1.25.3.3.1.2.2");
	}
*/

	/**
	 * Get the last computed value for CPU utilization on the specified machine.
	 * <p>
	 * The value is extracted from performance counter <code>Processor% Processor Time_Total</code>.
	 * On the target host a SNMP agent must run and have the OID <code>1.3.6.1.4.1.15.7</code> defined.
	 * 
	 * @param strAddress
	 *                    the address of the machine from where to get the CPU Utilization.
	 * @return
	 *                    a value between 0 and 100. If it fails, for any reason,
	 *                    to get a value from host, -1 is returned.
	 * @throws Exception 
	 *                    if fails to bind to a local socket.
	 */
/*
	public static double GetCpuUtilizationCurrent(String strAddress) throws Exception
	{
		return SnmpMonitor.GetCpuUtilization(strAddress, ".1.3.6.1.4.1.15.7");
	}
*/
}
