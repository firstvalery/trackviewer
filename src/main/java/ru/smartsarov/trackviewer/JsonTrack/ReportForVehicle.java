package ru.smartsarov.trackviewer.JsonTrack;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ru.smartsarov.trackviewer.jsoninsert.Vehicle;

public class ReportForVehicle {
	@SerializedName("tsFrom")
    @Expose
    private long tsFrom;
	@SerializedName("tsTo")
    @Expose
    private Vehicle tsTo;
	@SerializedName("vehicle")
    @Expose
    private Vehicle vehicle;
    @SerializedName("distance")
    @Expose
    private Integer distance;
    @SerializedName("totalWaiting")
    @Expose
    private Integer totalWaiting;

	public Integer getDistance() {
		return distance;
	}
	public void setDistance(Integer distance) {
		this.distance = distance;
	}
	@SerializedName("waitTrackPoints")
    @Expose
    private List<WaitTrackPoint> waitTrackPoints = null;
    
    public Vehicle getVehicle() {
		return vehicle;
	}
	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public List<WaitTrackPoint> getWaitTrackPoints() {
		return waitTrackPoints;
	}
	public void setWaitTrackPoints(List<WaitTrackPoint> waitTrackPoints) {
		this.waitTrackPoints = waitTrackPoints;
	}
	public ReportForVehicle(Vehicle vehicle, Integer distance, Integer totalWaiting, List<WaitTrackPoint> waitTrackPoints) {
		this.vehicle = vehicle;
		this.distance = distance;
		this.waitTrackPoints = waitTrackPoints;
		this.totalWaiting = totalWaiting;
	}
	public long getTsFrom() {
		return tsFrom;
	}
	public void setTsFrom(long tsFrom) {
		this.tsFrom = tsFrom;
	}
	public Vehicle getTsTo() {
		return tsTo;
	}
	public void setTsTo(Vehicle tsTo) {
		this.tsTo = tsTo;
	}
    public Integer getTotalWaiting() {
		return totalWaiting;
	}
	public void setTotalWaiting(Integer totalWaiting) {
		this.totalWaiting = totalWaiting;
	}
}
