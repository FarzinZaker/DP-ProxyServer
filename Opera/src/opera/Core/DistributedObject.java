/*
 * Created on Jun 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package opera.Core;


public class DistributedObject {
	private final LQM dobject;
	/**
	 * @param LQM
	 */
	DistributedObject(LQM allocation) {
		this.dobject = allocation;
		// TODO Auto-generated constructor stub
	}
	int code;
	String name = new String();
	double CPUdemand;
	double DISKdemand;
	ScenarioDemands[] ScenarioDemand;
	double[] Roc; // per class object response time
	double[] Uoc; // per class utilization;
	double[] Xoc; // per class troughput;
	double Uo; // total utilization;
	double visits;
	int biff;
	int AssignedE;
	int AssignedToE;
	int AssignedTo;
	int biff_Backup;
	int AssignedE_Backup;
	int AssignedToE_Backup;
	int AssignedTo_Backup;
	DistributedObject[][] Areato;
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getCPUdemand() {
		return CPUdemand;
	}
	public void setCPUdemand(double udemand) {
		CPUdemand = udemand;
	}
	public double getDISKdemand() {
		return DISKdemand;
	}
	public void setDISKdemand(double kdemand) {
		DISKdemand = kdemand;
	}
	public ScenarioDemands[] getScenarioDemand() {
		return ScenarioDemand;
	}
	public void setScenarioDemand(ScenarioDemands[] scenarioDemand) {
		ScenarioDemand = scenarioDemand;
	}
	public double[] getRoc() {
		return Roc;
	}
	public void setRoc(double[] roc) {
		Roc = roc;
	}
	public double[] getUoc() {
		return Uoc;
	}
	public void setUoc(double[] uoc) {
		Uoc = uoc;
	}
	public double[] getXoc() {
		return Xoc;
	}
	public void setXoc(double[] xoc) {
		Xoc = xoc;
	}
	public double getUo() {
		return Uo;
	}
	public void setUo(double uo) {
		Uo = uo;
	}
	public double getVisits() {
		return visits;
	}
	public void setVisits(double visits) {
		this.visits = visits;
	}
	public int getBiff() {
		return biff;
	}
	public void setBiff(int biff) {
		this.biff = biff;
	}
	public int getAssignedE() {
		return AssignedE;
	}
	public void setAssignedE(int assignedE) {
		AssignedE = assignedE;
	}
	public int getAssignedToE() {
		return AssignedToE;
	}
	public void setAssignedToE(int assignedToE) {
		AssignedToE = assignedToE;
	}
	public int getAssignedTo() {
		return AssignedTo;
	}
	public void setAssignedTo(int assignedTo) {
		AssignedTo = assignedTo;
	}
	public int getBiff_Backup() {
		return biff_Backup;
	}
	public void setBiff_Backup(int biff_Backup) {
		this.biff_Backup = biff_Backup;
	}
	public int getAssignedE_Backup() {
		return AssignedE_Backup;
	}
	public void setAssignedE_Backup(int assignedE_Backup) {
		AssignedE_Backup = assignedE_Backup;
	}
	public int getAssignedToE_Backup() {
		return AssignedToE_Backup;
	}
	public void setAssignedToE_Backup(int assignedToE_Backup) {
		AssignedToE_Backup = assignedToE_Backup;
	}
	public int getAssignedTo_Backup() {
		return AssignedTo_Backup;
	}
	public void setAssignedTo_Backup(int assignedTo_Backup) {
		AssignedTo_Backup = assignedTo_Backup;
	}
	public DistributedObject[][] getAreato() {
		return Areato;
	}
	public void setAreato(DistributedObject[][] areato) {
		Areato = areato;
	}
	public LQM getDobject() {
		return dobject;
	}
}