package Application;


public class ProgramSettings
{
	@ProgramParameter(Name="local-port", Description="The local port for the incoming connections.")
	public int localPort = 80;
	
	@ProgramParameter(Name="remote-address", Description="The remote address where the network requests are forwarded.")
	public String remoteAddress = "127.0.0.1";
	
	@ProgramParameter(Name="remote-port", Description="The remote port where the network requests are forwarded.")
	public int remotePort = 9100;
	
	@ProgramParameter(Name="control-port", Description="The port where the control server listens.")
	public int monitorPort = 9300;
}
