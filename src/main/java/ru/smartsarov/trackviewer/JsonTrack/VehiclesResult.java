package ru.smartsarov.trackviewer.JsonTrack;

import java.util.ArrayList;
import java.util.List;

public class VehiclesResult {
	List<VehicleModel> active;
	List<VehicleModel> passive;
	public List<VehicleModel> getActive() {
		return active;
	}
	public void setActive(List<VehicleModel> active) {
		this.active = active;
	}
	public List<VehicleModel> getPassive() {
		return passive;
	}
	public void setPassive(List<VehicleModel> passive) {
		this.passive = passive;
	}
	public VehiclesResult(List<VehicleModel> active, List<VehicleModel> passive) {
		this.active = active;
		this.passive = passive;
	}
	public VehiclesResult() {
		this.active = new ArrayList<>();
		this.passive = new ArrayList<>();
	}
	
}
