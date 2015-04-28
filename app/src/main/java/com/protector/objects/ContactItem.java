package com.protector.objects;

import java.util.ArrayList;

public class ContactItem {
	private int id;
	private String avatar;
	private String name;
	private String address;
	private String passwordText;
	private int groupId;
	private int type;
	private int contactIndex;
	private int phoneId;
	private int phoneType;
	private String phoneLabel;
	private int numberIndex;
	private long time;
	private int newSMS;
	private byte[] avatarByte;
	private String email;
	private String addressLocation;
	private String birthday;
	private String notes;
	private int typeTmpBlack = 0;
	private int idOrg;
	private boolean isChanged;
	private ArrayList<String> phoneOther;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
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

	public String getPasswordText() {
		return passwordText;
	}

	public void setPasswordText(String passwordText) {
		this.passwordText = passwordText;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getContactIndex() {
		return contactIndex;
	}

	public void setContactIndex(int contactIndex) {
		this.contactIndex = contactIndex;
	}

	public int getPhoneId() {
		return phoneId;
	}

	public void setPhoneId(int phoneId) {
		this.phoneId = phoneId;
	}

	public int getPhoneType() {
		return phoneType;
	}

	public void setPhoneType(int phoneType) {
		this.phoneType = phoneType;
	}

	public String getPhoneLabel() {
		return phoneLabel;
	}

	public void setPhoneLabel(String phoneLabel) {
		this.phoneLabel = phoneLabel;
	}

	public int getNumberIndex() {
		return numberIndex;
	}

	public void setNumberIndex(int numberIndex) {
		this.numberIndex = numberIndex;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getNewSMS() {
		return newSMS;
	}

	public void setNewSMS(int newSMS) {
		this.newSMS = newSMS;
	}

	public byte[] getAvatarByte() {
		return avatarByte;
	}

	public void setAvatarByte(byte[] avatarByte) {
		this.avatarByte = avatarByte;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddressLocation() {
		return addressLocation;
	}

	public void setAddressLocation(String addressLocation) {
		this.addressLocation = addressLocation;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getTypeTmpBlack() {
		return typeTmpBlack;
	}

	public void setTypeTmpBlack(int typeTmpBlack) {
		this.typeTmpBlack = typeTmpBlack;
	}

	public int getIdOrg() {
		return idOrg;
	}

	public void setIdOrg(int idOrg) {
		this.idOrg = idOrg;
	}

	public void compareContact(ContactItem object) {
		this.id = object.getId();
		this.idOrg = object.getIdOrg();
		this.avatar = object.getAvatar();
		this.name = object.getName();
		this.address = object.getAddress();
		this.passwordText = object.getPasswordText();
		this.groupId = object.getGroupId();
		this.type = object.getType();
		this.contactIndex = object.getContactIndex();
		this.phoneId = object.getPhoneId();
		this.phoneType = object.getPhoneType();
		this.phoneLabel = object.getPhoneLabel();
		this.numberIndex = object.getNumberIndex();
		this.time = object.getTime();
		this.newSMS = object.getNewSMS();
		this.avatarByte = object.getAvatarByte();
		this.email = object.getEmail();
		this.addressLocation = object.getAddressLocation();
		this.birthday = object.getBirthday();
		this.notes = object.getNotes();
		this.typeTmpBlack = object.getTypeTmpBlack();
	}

	public boolean isChanged() {
		return isChanged;
	}

	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}

	public ArrayList<String> getPhoneOther() {
		return phoneOther;
	}

	public void setPhoneOther(ArrayList<String> phoneOther) {
		this.phoneOther = phoneOther;
	}
}
