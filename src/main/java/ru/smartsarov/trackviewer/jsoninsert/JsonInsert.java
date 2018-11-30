
package ru.smartsarov.trackviewer.jsoninsert;

import java.math.BigDecimal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JsonInsert {

    @SerializedName("time")
    @Expose
    private Long time;
    @SerializedName("latitude")
    @Expose
    private BigDecimal latitude;
    @SerializedName("longitude")
    @Expose
    private BigDecimal longitude;
    @SerializedName("velocity")
    @Expose
    private Short velocity;
    @SerializedName("direction")
    @Expose
    private Short direction;
    @SerializedName("extra")
    @Expose
    private Extra extra;
    @SerializedName("region")
    @Expose
    private String region;
    @SerializedName("vehicle")
    @Expose
    private Vehicle vehicle;
    @SerializedName("icon")
    @Expose
    private Icon icon;

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Short getVelocity() {
        return velocity;
    }

    public void setVelocity(Short velocity) {
        this.velocity = velocity;
    }

    public Short getDirection() {
        return direction;
    }

    public void setDirection(Short direction) {
        this.direction = direction;
    }

    public Extra getExtra() {
        return extra;
    }

    public void setExtra(Extra extra) {
        this.extra = extra;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

}
