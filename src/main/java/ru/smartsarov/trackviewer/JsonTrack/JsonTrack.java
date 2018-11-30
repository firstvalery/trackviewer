
package ru.smartsarov.trackviewer.JsonTrack;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ru.smartsarov.trackviewer.jsoninsert.Vehicle;

public class JsonTrack {

    @SerializedName("vehicle")
    @Expose
    private Vehicle vehicle;
    @SerializedName("distance")
    @Expose
    private Integer distance;
    @SerializedName("segments")
    @Expose
    private List<Segment> segments = null;
    
    public Vehicle getVehicle() {
        return vehicle;
    }

	public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}

    public List<Segment> getSegments() {
        return segments;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }

}
