package opera.Core;

public class Host {
	int server; // Client(0) or Server
	int code;
	String name = new String();
	double CPUdemand;
	double DISKdemand;
	double Rkc[] = new double[20];
	double scCPUdemand[] = new double[20];
	double scDISKdemand[] = new double[20];
	double CPURatio;
	double DISKRatio;
	int CPUMultiplicity = 1;
	int diskMultiplicity = 1;
	int List[];
	int ObjAssigned;
	int NoObjectsVisited;
	public int getServer() {
		return server;
	}
	public void setServer(int server) {
		this.server = server;
	}
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
	public double[] getRkc() {
		return Rkc;
	}
	public void setRkc(double[] rkc) {
		Rkc = rkc;
	}
	public double[] getScCPUdemand() {
		return scCPUdemand;
	}
	public void setScCPUdemand(double[] scCPUdemand) {
		this.scCPUdemand = scCPUdemand;
	}
	public double[] getScDISKdemand() {
		return scDISKdemand;
	}
	public void setScDISKdemand(double[] scDISKdemand) {
		this.scDISKdemand = scDISKdemand;
	}
	public double getCPURatio() {
		return CPURatio;
	}
	public void setCPURatio(double ratio) {
		CPURatio = ratio;
	}
	public double getDISKRatio() {
		return DISKRatio;
	}
	public void setDISKRatio(double ratio) {
		DISKRatio = ratio;
	}
	public int getCPUMultiplicity() {
		return CPUMultiplicity;
	}
	public void setCPUMultiplicity(int multiplicity) {
		CPUMultiplicity = multiplicity;
	}
	public int getDiskMultiplicity() {
		return diskMultiplicity;
	}
	public void setDiskMultiplicity(int diskMultiplicity) {
		this.diskMultiplicity = diskMultiplicity;
	}
	public int[] getList() {
		return List;
	}
	public void setList(int[] list) {
		List = list;
	}
	public int getObjAssigned() {
		return ObjAssigned;
	}
	public void setObjAssigned(int objAssigned) {
		ObjAssigned = objAssigned;
	}
	public int getNoObjectsVisited() {
		return NoObjectsVisited;
	}
	public void setNoObjectsVisited(int noObjectsVisited) {
		NoObjectsVisited = noObjectsVisited;
	}
}
