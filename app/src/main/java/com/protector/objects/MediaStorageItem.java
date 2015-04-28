package com.protector.objects;

import java.io.Serializable;

public class MediaStorageItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 939191909757852368L;
	// public static final int IMAGE = 1;
	// public static final int VIDEO = 2;

	private long id;
	private String orgPath;
	private String name;
	private String newPath;
	private int type;
	private String extention;
	private long videoTime;
	private long date;
	private String solution;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOrgPath() {
		return orgPath;
	}

	public void setOrgPath(String orgPath) {
		this.orgPath = orgPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNewPath() {
		return newPath;
	}

	public void setNewPath(String newPath) {
		this.newPath = newPath;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getExtention() {
		return extention;
	}

	public void setExtention(String extention) {
		this.extention = extention;
	}

	public long getVideoTime() {
		return videoTime;
	}

	public void setVideoTime(long videoTime) {
		this.videoTime = videoTime;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getSolution() {
		return solution;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}

}
