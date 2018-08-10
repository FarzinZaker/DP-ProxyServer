package ProxyServer.RequestObserver;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import ProxyServer.IServiceClassRequestObserver;
import ProxyServer.User;
import ProxyServer.Monitoring.DataSample;
import ProxyServer.Monitoring.MonitoredDatastore;

/**
 * 
 * @author Cornel
 *
 */
public class ServiceClassRequestObserver extends Thread implements IServiceClassRequestObserver {
	private ArrayList<ServiceClassPattern> m_serviceClasses = new ArrayList<ServiceClassPattern>();
	private HashMap<String, ServiceClassMetrics> m_serviceMetrics = null;
	private HashMap<UUID, ProxyRequest> m_activeRequests = new HashMap<UUID, ProxyRequest>();

	private long m_timestampLastSample = 0;

	public ServiceClassRequestObserver() {
		this.setName("Thread - Request Observer");

		this.m_serviceMetrics = CreateServiceMetrics();
	}

	@Override
	public void run() {
		this.m_timestampLastSample = System.currentTimeMillis();
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}

			DataSample sample = this.GetMonitoredData();
			MonitoredDatastore.GetDatastore().AddSample(sample);
			 
			
			
		}
	}

	@Override
	public void onRequest(URI request, UUID uuid, String IP) {
		ProxyRequest req = new ProxyRequest();
		req.timestamp = System.currentTimeMillis();
		//System.out.println("on request start:"+req.timestamp);
		req.uri = request;
		req.uuid = uuid;
//		System.out.println(uuid);

		this.m_activeRequests.put(uuid, req);
		String serviceClass = this.FindServiceClass(request);
		User theUser = null;
		synchronized (this) {
			++this.m_serviceMetrics.get(serviceClass).reqUnfiltered;
			if (this.m_serviceMetrics.get(serviceClass).users.containsKey(IP)) {
				theUser = this.m_serviceMetrics.get(serviceClass).users.get(IP);
			}
			// add new user
			else {
				theUser = new User();
				theUser.setIP(IP);
				this.m_serviceMetrics.get(serviceClass).users.put(IP, theUser);

			}
		}
		if (null != theUser) {
			synchronized (theUser) {
				theUser.incrementArrival();
				this.m_serviceMetrics.get(serviceClass).users.put(IP, theUser);
			}
		} else {
			System.out.println("Error - Panic");
		}

		// if (this.m_serviceMetrics.get(serviceClass).user.get(IP) != null) {
		// ++this.m_serviceMetrics.get(serviceClass).user.get(IP).arrivals;
		// }
		// arrival rate=arrivals/timeInSeconds
		
	}

	@Override
	public void onMonitorRequest(URI request) {
		String serviceClass = this.FindServiceClass(request);
		synchronized (this) {
			++this.m_serviceMetrics.get(serviceClass).reqFiltered;

		}
	}

	@Override
	public void onReply(UUID uuid, String IP) {
		long timestampNow = System.currentTimeMillis();
		//System.out.println("reply time"+timestampNow);

		ProxyRequest request = this.m_activeRequests.get(uuid);
		
		if (null != request) {
			String serviceClass = this.FindServiceClass(request.uri);
			int responseTime = (int) (timestampNow - request.timestamp);
			this.m_activeRequests.remove(uuid);
			System.out.println("responseTime="+responseTime);
			User theUser = null;
			synchronized (this) {
				if (this.m_serviceMetrics.get(serviceClass).users.containsKey(IP)) {
					theUser = this.m_serviceMetrics.get(serviceClass).users.get(IP);
				}
				// this should not happen, because user has been already added
				// in onRequest
				else {

					theUser = new User();
					theUser.IP = IP;
					this.m_serviceMetrics.get(serviceClass).users.put(IP, theUser);
				}
			}
			if (null != theUser) {
				synchronized (theUser) {
					theUser.addResponseTime(responseTime);
					this.m_serviceMetrics.get(serviceClass).users.put(IP, theUser);
				}
			} else {
				System.out.println("Error - Panic");
			}

			// if (this.m_serviceMetrics.get(serviceClass).user.get(IP) != null)
			// {
			// this.m_serviceMetrics.get(serviceClass).user.get(IP).responseTime.add(responseTime);
			// ++this.m_serviceMetrics.get(serviceClass).user.get(IP).arrivals;
			//
			// }
			// add new user
			// else {
			// User newUser = new User();
			// newUser.IP = IP;
			// newUser.responseTime.add(responseTime);
			// ++newUser.arrivals;
			// this.m_serviceMetrics.get(serviceClass).user.put(IP, newUser);
			// }

			// for (int i = 0; i <
			// this.m_serviceMetrics.get(serviceClass).user.size(); i++) {
			// // check if the user already exists
			// if
			// (this.m_serviceMetrics.get(serviceClass).user.get(i).IP.equals(IP))
			// {
			// this.m_serviceMetrics.get(serviceClass).user.get(i).responseTime.add(responseTime);
			// ++this.m_serviceMetrics.get(serviceClass).user.get(i).arrivals;
			// userExist = 1;
			// }
			// }
			// //new user
			// if (userExist == 0) {
			// User newUser = new User();
			// newUser.IP = IP;
			// newUser.responseTime.add(responseTime);
			// ++newUser.arrivals;
			// this.m_serviceMetrics.get(serviceClass).user.add(newUser);
			// }

			synchronized (this) {
				this.m_serviceMetrics.get(serviceClass).responseTime.add(responseTime);
			}

		}
	}

	@Override
	public void AddServiceClass(String name, String pattern) {
		if (this.isAlive()) {
			// the thread already runs. The classes of service will not change
			return;
		}

		// search for this class. If it's added already, just change update the
		// pattern
		for (int i = 0; i < this.m_serviceClasses.size(); ++i) {
			ServiceClassPattern scPattern = this.m_serviceClasses.get(i);
			if (scPattern.name.equals(name)) {
				scPattern.SetPattern(pattern);
				return;
			}
		}
		this.m_serviceClasses.add(new ServiceClassPattern(name, pattern));
	}

	@Override
	public void RemoveServiceClass(String name) {
		if (this.isAlive()) {
			// the thread already runs. The classes of service will not change.
			return;
		}

		// search for this class. If found, remove it from the list
		for (int i = 0; i < this.m_serviceClasses.size(); ++i) {
			ServiceClassPattern scPattern = this.m_serviceClasses.get(i);
			if (scPattern.name.equals(name)) {
				this.m_serviceClasses.remove(i);
				return;
			}
		}
	}

	/**
	 * Look in the list of defined service classes and find one that matches the
	 * URI.
	 * 
	 * @param request
	 * @return If a match is found, return the name of the class. If no match is
	 *         found, return "unknown".
	 */
	private String FindServiceClass(URI request) {
		for (ServiceClassPattern serviceClass : this.m_serviceClasses) {
			if (serviceClass.pattern.matcher(request.toString()).matches()) {
				return serviceClass.name;
			}
		}

		return "unknown";
	}

	/**
	 * Create a list of metrics for each defined class of service. Also, add to
	 * the list metrics for "unknown" class of service (defined as the class for
	 * any request that does not match a defined one).
	 * 
	 * @return
	 */
	private HashMap<String, ServiceClassMetrics> CreateServiceMetrics() {
		HashMap<String, ServiceClassMetrics> serviceMetrics = new HashMap<String, ServiceClassMetrics>();

		// This method will becalled from the thread where the writer is.
		// Because of this, no synchronization is required for access to
		// "m_serviceClasses".
		for (ServiceClassPattern serviceClass : this.m_serviceClasses) {
			serviceMetrics.put(serviceClass.name, new ServiceClassMetrics(serviceClass.name));

		}

		serviceMetrics.put("unknown", new ServiceClassMetrics("unknown"));

		return serviceMetrics;
	}

	private double CalculateAverage(ArrayList<Integer> values) {
		double avg = 0;

		if (values != null && values.size() > 0) {
			for (int value : values) {
				avg += value;
			}
			avg /= values.size();
		}

		return avg;
	}

	private DataSample GetMonitoredData() {
		HashMap<String, ServiceClassMetrics> serviceMetrics = this.CreateServiceMetrics();
		DataSample theSample = new DataSample();

		synchronized (this) {
			HashMap<String, ServiceClassMetrics> serviceMetricsTmp = this.m_serviceMetrics;
			this.m_serviceMetrics = serviceMetrics;
			serviceMetrics = serviceMetricsTmp;

			long timestampNow = System.currentTimeMillis();
			theSample.sampleIntervalLength = timestampNow - this.m_timestampLastSample;
			this.m_timestampLastSample = timestampNow;
		}

		// this is to determine the overall users per sample
		// int i = 0;
		// int cntServiceUsers=0;
		//
		// for (ServiceClassMetrics serviceClassMetrics :
		// serviceMetrics.values()) {
		// cntServiceUsers=+serviceClassMetrics.user.size();
		// ++i;
		// }

		int serviceClassCnt = serviceMetrics.size();
		theSample.scenarioNames = new String[serviceClassCnt];
		theSample.responseTime = new double[serviceClassCnt];
		theSample.requestsCnt = new int[serviceClassCnt];
		theSample.requestsProcessedCnt = new int[serviceClassCnt];
		theSample.requestsBlockedCnt = new int[serviceClassCnt];

		int i = 0;
		for (ServiceClassMetrics serviceClassMetrics : serviceMetrics.values()) {
			/*
			 * here we create this temporary object and allocate separate memory
			 * to it to make the reset operation only applicable to
			 * serviceClassMetrics; this way, the theSample object preserves
			 * previous samples values.
			 */
			//ServiceClassMetrics serviceClassMetricsTemp = new ServiceClassMetrics("temp");
			//serviceClassMetricsTemp = serviceClassMetrics;

			theSample.scenarioNames[i] = serviceClassMetrics.name;
			theSample.responseTime[i] = this.CalculateAverage(serviceClassMetrics.responseTime);
			theSample.requestsCnt[i] = serviceClassMetrics.reqUnfiltered;
			// these for calculating throughput
			theSample.requestsProcessedCnt[i] = serviceClassMetrics.responseTime.size();
			theSample.requestsBlockedCnt[i] = serviceClassMetrics.reqFiltered / 2;	
			//users= <ip, User>
			// we now need to have a copy of serviceClassMetrics and users to put it in sample coz we want to reset the users later
			ServiceClassMetrics serviceClassMetricsTemp = new ServiceClassMetrics("temp");
			serviceClassMetricsTemp.copyObject(serviceClassMetrics);
			
			/*User [] users = new User[serviceClassMetrics.users.size()];
			int j=0;
			for(String key: serviceClassMetrics.users.keySet()){
				users[j] = new User();
				users[j].copyObject(serviceClassMetrics.users.get(key));
				serviceClassMetricsTemp.users.put(key,users[j]);
				j++;
			}	*/				
			
			theSample.users.add(i, serviceClassMetricsTemp.users);
			// System.out.println("users samples:"+theSample.users);
			++i;
		}
		//here I reset the users so that response time and arrivals get rest for next sample interval
		for (ServiceClassMetrics serviceClassMetrics : serviceMetrics.values()) {
			for(String key: serviceClassMetrics.users.keySet()){
				serviceClassMetrics.users.get(key).reset();
			}
		}
		
		
		//for (i = 0; i < theSample.users.size(); ++i) {
			//for (String key : theSample.users.get(i).keySet()) {
				//System.out.println(theSample.users.get(i).get(key).IP);
				//System.out.println(theSample.users.get(i).get(key).responseTime);
				//System.out.println(theSample.users.get(i).get(key).getArrival());
			//}
		//}
		//System.out.println(theSample);
		return theSample;
	}

	/*
	 * This code was supposed to allow dinamic change in the classes of service
	 * that the observer is handling. However, because the synchronization
	 * overhead might be too great, the observer will require that the classes
	 * of service are initialized before the thread starts - once the classes of
	 * service are set and the thread starts, they cannot be modified.
	 * 
	 * 
	 * private ArrayList<ServiceClassPattern> m_serviceClassesToAdd = new
	 * ArrayList<ServiceClassPattern>(); private ArrayList<String>
	 * m_serviceClassesToRemove = new ArrayList<String>(); private final
	 * ReentrantReadWriteLock lockReadWrite = new ReentrantReadWriteLock();
	 * private final Lock lockRead = lockReadWrite.readLock(); private final
	 * Lock lockWrite = lockReadWrite.writeLock();
	 * 
	 * @Override public void AddServiceClass(String name, String pattern) { //
	 * Check if this service class is in the "to add" list. // If it is, update
	 * the pattern. for (int i = 0; i < this.m_serviceClassesToAdd.size(); ++i)
	 * { ServiceClassPattern scPattern = this.m_serviceClassesToAdd.get(i); if
	 * (scPattern.name.equals(name)) { scPattern.SetPattern(pattern); return; }
	 * }
	 * 
	 * this.m_serviceClassesToAdd.add(new ServiceClassPattern(name, pattern));
	 * 
	 * // Check if this service class is in the "to remove" list. // If it is,
	 * remove it from there. for (int i = 0; i <
	 * this.m_serviceClassesToRemove.size(); ++i) { String className =
	 * this.m_serviceClassesToRemove.get(i); if (className.equals(name)) {
	 * this.m_serviceClassesToRemove.remove(i); return; } } }
	 * 
	 * @Override public void RemoveServiceClass(String name) { // Check if this
	 * service class is in the "to add" list. // If it is, update the pattern.
	 * for (int i = 0; i < this.m_serviceClassesToRemove.size(); ++i) { String
	 * className = this.m_serviceClassesToRemove.get(i); if
	 * (className.equals(name)) { return; } }
	 * 
	 * this.m_serviceClassesToRemove.add(name);
	 * 
	 * // Check if this service class is in the "to remove" list. // If it is,
	 * remove it from there. for (int i = 0; i <
	 * this.m_serviceClassesToAdd.size(); ++i) { ServiceClassPattern scPattern =
	 * this.m_serviceClassesToAdd.get(i); if (scPattern.name.equals(name)) {
	 * this.m_serviceClassesToAdd.remove(i); return; } } }
	 * 
	 * private void UpdateServiceClasses() { // check if there is anything to
	 * update if (this.m_serviceClassesToAdd.size() == 0 ||
	 * this.m_serviceClassesToRemove.size() == 0) return;
	 * 
	 * this.lockWrite.lock(); // first do the remove part if
	 * (this.m_serviceClassesToRemove.size() > 0) { for (int i = 0; i <
	 * this.m_serviceClassesToRemove.size(); ++i) { String className =
	 * this.m_serviceClassesToRemove.get(i); for (int j = 0; j <
	 * this.m_serviceClasses.size(); ++j) { ServiceClassPattern scPattern =
	 * this.m_serviceClasses.get(j); if (className.equals(scPattern.name)) {
	 * this.m_serviceClasses.remove(j); break; } } } }
	 * 
	 * if (this.m_serviceClassesToAdd.size() > 0) { for (int i = 0; i <
	 * this.m_serviceClassesToAdd.size(); ++i) { ServiceClassPattern
	 * scPatternToAdd = this.m_serviceClassesToAdd.get(i); boolean isInTheList =
	 * false; for (int j = 0; j < this.m_serviceClasses.size(); ++j) {
	 * ServiceClassPattern scPattern = this.m_serviceClasses.get(j); if
	 * (scPatternToAdd.name.equals(scPattern.name)) {
	 * scPattern.SetPattern(scPatternToAdd.pattern); isInTheList = true; break;
	 * } } if (isInTheList == false) {
	 * this.m_serviceClasses.add(scPatternToAdd); } } } this.lockWrite.unlock();
	 * 
	 * this.m_serviceClassesToAdd.clear();
	 * this.m_serviceClassesToRemove.clear(); }
	 */
}
