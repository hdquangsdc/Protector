package com.protector.objects;

import java.io.Serializable;

public class SmsCallLogItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2634200807462925117L;
	private int id;
	private int groupId;
	private String name;
	private String address;
	private long time;
	private String bodySms;
	private int read;
	private long date;
	private int state;
	private int numberIndex;
	private int type;
	private long durationCallLog;
	private int typeCompare;
	private int threadIdSms;
	private byte[] avatarByte;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
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

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public int getNumberIndex() {
		return numberIndex;
	}

	public void setNumberIndex(int numberIndex) {
		this.numberIndex = numberIndex;
	}

	public int getRead() {
		return read;
	}

	public void setRead(int read) {
		this.read = read;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getBodySms() {
		return bodySms;
	}

	public void setBodySms(String bodySms) {
		this.bodySms = bodySms;
	}

	public long getDurationCallLog() {
		return durationCallLog;
	}

	public void setDurationCallLog(long durationCallLog) {
		this.durationCallLog = durationCallLog;
	}

	public int getTypeCompare() {
		return typeCompare;
	}

	public void setTypeCompare(int typeCompare) {
		this.typeCompare = typeCompare;
	}

	public int getThreadIdSms() {
		return threadIdSms;
	}

	public void setThreadIdSms(int threadIdSms) {
		this.threadIdSms = threadIdSms;
	}

	public byte[] getAvatarByte() {
		return avatarByte;
	}

	public void setAvatarByte(byte[] avatarByte) {
		this.avatarByte = avatarByte;
	}
}
