package ru.smartsarov.trackviewer.JsonTrack;

public class VehicleData {
	private int id;
	private String uid;
	private String number;
	private String type;
	private String owner;
	private String model;
	private String description;
	private String color;
	private String garageNum;
	private long ts;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getGarageNum() {
		return garageNum;
	}
	public void setGarageNum(String garageNum) {
		this.garageNum = garageNum;
	}
	public long getTs() {
		return ts;
	}
	public void setTs(long ts) {
		this.ts = ts;
	}
	public VehicleData(int id, String uid, String number, String type, String owner, String model, String description,
			String color, String garageNum, long ts) {
		this.id = id;
		this.uid = uid;
		this.number = number;
		this.type = type;
		this.owner = owner;
		this.model = model;
		this.description = description;
		this.color = color;
		this.garageNum = garageNum;
		this.ts = ts;
	}
	public VehicleData() {
		this.id = 0;
		this.uid = "";
		this.number = "";
		this.type = "";
		this.owner = "";
		this.model = "";
		this.description = "";
		this.color = "";
		this.garageNum = "";
		this.ts = 0;
	}
}
