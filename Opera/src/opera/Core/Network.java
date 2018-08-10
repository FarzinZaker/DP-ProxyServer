package opera.Core;

public class Network {
	double Rkc[] = new double[20];
	int code;
	double latency;
	double msPerByte;
	int[] hosts;
	String name;
	public double[] getRkc() {
		return Rkc;
	}
	public void setRkc(double[] rkc) {
		Rkc = rkc;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public double getLatency() {
		return latency;
	}
	public void setLatency(double latency) {
		this.latency = latency;
	}
	public double getMsPerByte() {
		return msPerByte;
	}
	public void setMsPerByte(double msPerByte) {
		this.msPerByte = msPerByte;
	}
	public int[] getHosts() {
		return hosts;
	}
	public void setHosts(int[] hosts) {
		this.hosts = hosts;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
