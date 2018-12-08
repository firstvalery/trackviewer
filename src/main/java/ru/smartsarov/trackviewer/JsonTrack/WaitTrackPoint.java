package ru.smartsarov.trackviewer.JsonTrack;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WaitTrackPoint {
	@SerializedName("trackPoint")
    @Expose
    private TrackPoint trackPoint;
    
	@SerializedName("waiting")
    @Expose
    private int waiting;
	
	public TrackPoint getTrackPoint() {
		return trackPoint;
	}
	public void setTrackPoint(TrackPoint trackPoint) {
		this.trackPoint = trackPoint;
	}
	public int getWaiting() {
		return waiting;
	}
	public void setWaiting(int waiting) {
		this.waiting = waiting;
	}
	public WaitTrackPoint(TrackPoint trackPoint, int waiting) {
		this.trackPoint = trackPoint;
		this.waiting = waiting;
	}
}
