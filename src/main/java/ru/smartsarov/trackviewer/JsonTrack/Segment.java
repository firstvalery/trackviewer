
package ru.smartsarov.trackviewer.JsonTrack;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Segment {

    @SerializedName("distance")
    @Expose
    private Integer distance;
    @SerializedName("average")
    @Expose
    private float avarage;
    @SerializedName("waiting")
    @Expose
    private long waiting;
    @SerializedName("odometer")
    @Expose
    private Integer odometer;
	@SerializedName("trackPoints")
    @Expose
    private List<TrackPoint> trackPoints = null;

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public float getAvaerage() {
        return avarage;
    }

    public void setAvaerage(float avaerage) {
        this.avarage = avaerage;
    }

    public long getWaiting() {
        return waiting;
    }

    public void setWaiting(long waiting) {
        this.waiting = waiting;
    }

    public List<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    public void setTrackPoints(List<TrackPoint> trackPoints) {
        this.trackPoints = trackPoints;
    }
    public Integer getOdometer() {
		return odometer;
	}

	public void setOdometer(Integer odometer) {
		this.odometer = odometer;
	}

}
