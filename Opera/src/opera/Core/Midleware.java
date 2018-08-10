package opera.Core;


public class Midleware {
	String name;
	int code;
	double latencySend;
	double latencyReceive;
	double msPerByteSent;
	double msPerByteReceived;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public double getLatencySend() {
		return latencySend;
	}
	public void setLatencySend(double latencySend) {
		this.latencySend = latencySend;
	}
	public double getLatencyReceive() {
		return latencyReceive;
	}
	public void setLatencyReceive(double latencyReceive) {
		this.latencyReceive = latencyReceive;
	}
	public double getMsPerByteSent() {
		return msPerByteSent;
	}
	public void setMsPerByteSent(double msPerByteSent) {
		this.msPerByteSent = msPerByteSent;
	}
	public double getMsPerByteReceived() {
		return msPerByteReceived;
	}
	public void setMsPerByteReceived(double msPerByteReceived) {
		this.msPerByteReceived = msPerByteReceived;
	}
}