package com.protector.objects;

import java.io.Serializable;

/**
 * @author Ho
 * 
 */

public class MediaItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8561036327997834021L;

	public enum Type {
		VIDEO, IMAGE
	}

	int id;
	Type type;
	String path;
	long dateModified;
	String solution;
	boolean selected;

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getDateModified() {
		return dateModified;
	}

	public void setDateModified(long dateModified) {
		this.dateModified = dateModified;
	}

	public String getSolution() {
		return solution;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}

	/**
	 * @return path of media file
	 */
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
