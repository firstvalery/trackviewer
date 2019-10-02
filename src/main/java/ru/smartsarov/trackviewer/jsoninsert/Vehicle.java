
package ru.smartsarov.trackviewer.jsoninsert;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Vehicle {

	@SerializedName("type")
	@Expose
	private String type;
	@SerializedName("uid")
	@Expose
	private String uid;
	@SerializedName("number")
	@Expose
	private String number;
	@SerializedName("owner")
	@Expose
	private String owner;
	@SerializedName("model")
	@Expose
	private String model;
	@SerializedName("description")
	@Expose
	private String description;
	@SerializedName("rnum")
	@Expose
	private String rnum;

	public String getRnum() {
		return rnum;
	}

	public void setRnum(String rnum) {
		this.rnum = rnum;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public Vehicle(String type, String uid, String number, String owner, String model, String description,
			String rnum) {
		this.type = type;
		this.uid = uid;
		this.number = number;
		this.owner = owner;
		this.model = model;
		this.description = description;
		this.rnum = rnum;
	}

	public Vehicle() {
		this.type = "";
		this.uid = "";
		this.number = "";
		this.owner = "";
		this.model = "";
		this.description = "";
		this.rnum ="";
	}

}
