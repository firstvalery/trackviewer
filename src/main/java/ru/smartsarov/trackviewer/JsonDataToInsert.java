package ru.smartsarov.trackviewer;


public class JsonDataToInsert {
	private String timestamp;
	private String latitude;
	private String longitude;
	private String velocity;
	private String direction;
	private String vehicle_type;
	private String vehicle_uid;
	private String vehicle_number;
	private String additional;
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getVelocity() {
		return velocity;
	}
	public void setVelocity(String velocity) {
		this.velocity = velocity;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public String getVehicle_type() {
		return vehicle_type;
	}
	public void setVehicle_type(String vehicle_type) {
		this.vehicle_type = vehicle_type;
	}
	public String getVehicle_uid() {
		return vehicle_uid;
	}
	public void setVehicle_uid(String vehicle_uid) {
		this.vehicle_uid = vehicle_uid;
	}
	public String getVehicle_number() {
		return vehicle_number;
	}
	public void setVehicle_number(String vehicle_number) {
		this.vehicle_number = vehicle_number;
	}
	public String getAdditional() {
		return additional;
	}
	public void setAdditional(String additional) {
		this.additional = additional;
	}
	public JsonDataToInsert(String timestamp, String latitude, String longitude, String velocity, String direction,
			String vehicle_type, String vehicle_uid, String vehicle_number, String additional) {
		this.timestamp = timestamp;
		this.latitude = latitude;
		this.longitude = longitude;
		this.velocity = velocity;
		this.direction = direction;
		this.vehicle_type = vehicle_type;
		this.vehicle_uid = vehicle_uid;
		this.vehicle_number = vehicle_number;
		this.additional = additional;
	}
	
	public JsonDataToInsert() {
		this.timestamp = "";
		this.latitude = "";
		this.longitude = "";
		this.velocity = "";
		this.direction = "";
		this.vehicle_type = "";
		this.vehicle_uid = "";
		this.vehicle_number = "";
		this.additional = "";
	}
	
	
	
	
}
