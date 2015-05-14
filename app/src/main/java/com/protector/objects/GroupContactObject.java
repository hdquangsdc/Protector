package com.protector.objects;

import java.io.Serializable;
import java.util.ArrayList;

public class GroupContactObject implements Serializable {
	private String address;
	private long groupID;
	private ArrayList<String> arrayPhoneAdded;
	private String name;
	private int contactID;

	public GroupContactObject(String addr, long groupID) {
		this.address = addr;
		this.groupID = groupID;
	}

	public GroupContactObject(String addr, long groupID,
			ArrayList<String> arrsPhoneAddedNew, String name) {
		this.address = addr;
		this.groupID = groupID;
		this.arrayPhoneAdded = arrsPhoneAddedNew;
		this.name = name;
	}

	public GroupContactObject(String addr, long groupID,
			ArrayList<String> arrsPhoneAddedNew, String name, int contactID) {
		this.address = addr;
		this.groupID = groupID;
		this.arrayPhoneAdded = arrsPhoneAddedNew;
		this.name = name;
		this.contactID = contactID;
	}

	public int getContactID() {
		return contactID;
	}

	public void setContactID(int contactID) {
		this.contactID = contactID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public long getGroupID() {
		return groupID;
	}

	public void setGroupID(long groupID) {
		this.groupID = groupID;
	}

	public ArrayList<String> getArrayPhoneAdded() {
		return arrayPhoneAdded;
	}

	public void setArrayPhoneAdded(ArrayList<String> arrayPhoneAdded) {
		this.arrayPhoneAdded = arrayPhoneAdded;
	}

}
