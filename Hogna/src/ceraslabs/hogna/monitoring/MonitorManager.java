package ceraslabs.hogna.monitoring;

import java.util.ArrayList;

import ceraslabs.hogna.data.MetricCollection;
//import ceraslabs.hogna.monitoring.Data.MetricValues;
import Framework.Diagnostics.Trace;

public class MonitorManager extends Thread {
	private final int DATASTORE_SIZE = 3600;
	private final String LOCK_MONITORS = "Lock for Monitors access.";

	// Access to these two variables should be done only in the methods
	// "GetLastSample", "GetSamples" and "AddSample". These methods are
	// thread-safe.
	// private MetricValues[] m_samples = new MetricValues[DATASTORE_SIZE];
	private MetricCollection[] m_samples = new MetricCollection[DATASTORE_SIZE];
	private Integer m_currentIdx = 0;

	private int m_samplingInterval = 20000;

	/**
	 * The list of monitors in use. This list is accessed from only one thread
	 * and should be modified only on some specific moments (once at each
	 * iteration).
	 * 
	 * To record requested changes to the list, variables "m_monitorsNewList",
	 * "m_monitorsToAdd" and "m_monitorsToRemove" are used. These changes are
	 * applied before getting data from monitored machines.
	 */
	ArrayList<Monitor> m_monitors = new ArrayList<Monitor>();

	ArrayList<Monitor> m_monitorsNewList = new ArrayList<Monitor>();
	ArrayList<Monitor> m_monitorsToAdd = new ArrayList<Monitor>();
	ArrayList<Monitor> m_monitorsToRemove = new ArrayList<Monitor>();

	public MonitorManager() {

	}

	// private void AddSample(MetricValues theSample)
	// {
	// synchronized (this.m_samples)
	// {
	// this.m_currentIdx = (this.m_currentIdx + 1) % DATASTORE_SIZE;
	// this.m_samples[this.m_currentIdx] = theSample;
	// }
	// }
	private void AddSample(MetricCollection theSample) {
		synchronized (this.m_samples) {
			this.m_currentIdx = (this.m_currentIdx + 1) % DATASTORE_SIZE;
			this.m_samples[this.m_currentIdx] = theSample;
		}
	}

	// public MetricValues GetLastSample()
	// {
	// synchronized (this.m_samples)
	// {
	// return this.m_samples[this.m_currentIdx];
	// }
	// }

	public MetricCollection GetLastSample() {
		synchronized (this.m_samples) {
			return this.m_samples[this.m_currentIdx];
		}
	}

	public MetricCollection[] GetSamples(int count) {
		if (count <= 0 || count > DATASTORE_SIZE) {
			count = DATASTORE_SIZE;
		}
		// MetricValues[] theSamples = new MetricValues[count];
		MetricCollection[] theSamples = new MetricCollection[count];

		synchronized (this.m_samples) {
			int sampleIdx = this.m_currentIdx + DATASTORE_SIZE;

			for (int i = 0; i < count; ++i) {
				theSamples[i] = this.m_samples[sampleIdx % DATASTORE_SIZE];
				--sampleIdx;
			}
		}

		return theSamples;
	}

	public void AddMonitor(Monitor theMonitor) {
		synchronized (LOCK_MONITORS) {
			if (this.m_monitorsNewList.size() > 0) {
				if (this.m_monitorsNewList.contains(theMonitor) == false) {
					this.m_monitorsNewList.add(theMonitor);
				}
			} else if (this.m_monitorsToAdd.contains(theMonitor) == false) {
				this.m_monitorsToAdd.add(theMonitor);
				this.m_monitorsToRemove.remove(theMonitor);
			}
		}
	}

	public void AddMonitors(Monitor[] monitors) {
		for (Monitor monitor : monitors) {
			this.AddMonitor(monitor);
		}
	}

	public void RemoveMonitor(Monitor theMonitor) {
		synchronized (LOCK_MONITORS) {
			if (this.m_monitorsNewList.size() > 0) {
				this.m_monitorsNewList.remove(theMonitor);
			} else if (this.m_monitorsToRemove.contains(theMonitor) == false) {
				this.m_monitorsToRemove.add(theMonitor);
				this.m_monitorsToAdd.remove(theMonitor);
			}
		}
	}

	public void RemoveMonitors(Monitor[] monitors) {
		for (Monitor monitor : monitors) {
			this.RemoveMonitor(monitor);
		}
	}

	public void SetMonitors(Monitor[] monitors) {
		synchronized (LOCK_MONITORS) {
			// clear all the lists and set the new list
			this.m_monitorsNewList.clear();
			this.m_monitorsToAdd.clear();
			this.m_monitorsToRemove.clear();

			for (Monitor monitor : monitors) {
				this.m_monitorsNewList.add(monitor);
			}
		}
	}

	private void UpdateMonitorList() {
		synchronized (LOCK_MONITORS) {
			// check if we should replace the new list
			if (this.m_monitorsNewList.size() > 0) {
				// clear the old list of monitors, and replace it with this one
				this.m_monitors.clear();
				this.m_monitors.addAll(this.m_monitorsNewList);

				this.m_monitorsNewList.clear();

				Trace.Assert(this.m_monitorsToAdd.size() == 0,
						"The list of monitors has been replaced. There should be no monitors to add, but there are [%d].",
						this.m_monitorsToAdd.size());
				Trace.Assert(this.m_monitorsToRemove.size() == 0,
						"The list of monitors has been replaced. There should be no monitors to remove, but there are [%d].",
						this.m_monitorsToRemove.size());
			} else {
				// the list is not replaced
				// add/remove monitors
				this.m_monitors.removeAll(this.m_monitorsToRemove);
				this.m_monitors.addAll(this.m_monitorsToAdd);

				this.m_monitorsToRemove.clear();
				this.m_monitorsToAdd.clear();
			}
		}
	}

	@Override
	public void run() {
		// int idxIteration = 0;
		while (true) {
			// ++idxIteration;
			try {
				Thread.sleep(this.m_samplingInterval);
			} catch (Exception e) {
				Trace.WriteException(e);
			}

			// apply changes requested to the monitor list
			this.UpdateMonitorList();

			// MetricValues metricValues = new MetricValues();
			MetricCollection metricValues = new MetricCollection();
			// metricValues.SetId(idxIteration);

			// extract data
			for (Monitor monitor : this.m_monitors) {
				// double value = monitor.GetValue();
				// String name = monitor.GetName();
				// String nodeId = monitor.GetHost();

				// metricValues.AddMetricValue(monitor.GetCluster() != null ?
				// monitor.GetCluster() : "Not Set", nodeId, name, value);

				try {
					metricValues.Add(monitor.GetValues());
				} catch (Exception ex) {
					Trace.WriteException(ex);
				}
			}
			this.AddSample(metricValues);
		}
	}
}
