
package ru.smartsarov.trackviewer.JsonTrack;

import java.math.BigDecimal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TrackPoint {

    @SerializedName("vel")
    @Expose
    private short velocity;
    @SerializedName("dir")
    @Expose
    private short direction;
    @SerializedName("lng")
    @Expose
    private BigDecimal longitude;
    @SerializedName("lat")
    @Expose
    private BigDecimal latitude;
    @SerializedName("ts")
    @Expose
    private long timestamp;
    @SerializedName("code")
    @Expose
    private short colorcode;   
    
    private double odometer;

    public short getVelocity() {
        return velocity;
    }

    public void setVelocity(short velocity) {
        this.velocity = velocity;
    }

    public short getDirection() {
        return direction;
    }

    public void setDirection(short direction) {
        this.direction = direction;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public short getCollorcode() {
        return colorcode;
    }

    public void setCollorcode(short collorcode) {
        this.colorcode = collorcode;
    }

	public TrackPoint(short velocity, short direction, BigDecimal longitude, BigDecimal latitude, long timestamp,
			short collorcode) {
		this.velocity = velocity;
		this.direction = direction;
		this.longitude = longitude;
		this.latitude = latitude;
		this.timestamp = timestamp;
		this.colorcode = collorcode;
	}

	public double getOdometer() {
		return odometer;
	}

	public void setOdometer(double odometer) {
		this.odometer = odometer;
	}
    

}
