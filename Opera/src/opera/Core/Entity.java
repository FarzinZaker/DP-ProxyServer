package opera.Core;

public class Entity {
	int server; //Client or Server
	int code;
	String name = new String();
	int Assigned;
	int AssignedToH;
	int multiplicity = 1000;
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
	public int getAssigned() {
		return Assigned;
	}
	public void setAssigned(int assigned) {
		Assigned = assigned;
	}
	public int getAssignedToH() {
		return AssignedToH;
	}
	public void setAssignedToH(int assignedToH) {
		AssignedToH = assignedToH;
	}
	public int getMultiplicity() {
		return multiplicity;
	}
	public void setMultiplicity(int multiplicity) {
		this.multiplicity = multiplicity;
	}
}