package ProxyServer;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
	private static final long serialVersionUID = 3029545706156388929L;
	public String IP;
	// throughput=responseTime.size()/timeInSeconds
	public ArrayList<Integer> responseTime = new ArrayList<Integer>();
	private Integer arrivals = 0;
	// public String scenario;
	
	public void setIP(String iP) {
		IP = iP;
	}
	
	public String getIP(){
		return IP;
	}
	
	public int getArrival(){
		return arrivals;
	}
	
	public void addResponseTime(Integer rt) {
		responseTime.add(rt);
	}

	public void incrementArrival() {
		this.arrivals++;
	}
	
	
	public Double getRTSum() {
		double sum = 0;
		if (responseTime.size()!=0){
		for (Integer rt : responseTime) {
			sum += rt;
		}
		
		return (Double) (sum);
		}
		else return 0.0;
	}
	
	public int getThroughput(){
		return responseTime.size();
	}
	
	public void reset(){
		responseTime.clear();
		arrivals=0;
	}
	
	
	public void copyObject(User user){
		responseTime.clear();
		
		this.IP = user.IP;
		for (Integer integer : user.responseTime) {
			this.responseTime.add(integer);
		}
		this.arrivals = user.arrivals;
	}
}