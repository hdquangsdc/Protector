package com.protector.utils;

import java.util.LinkedHashSet;
import java.util.Set;

public class NotificationIdManager {
	private static final Set<Integer> mIds = new LinkedHashSet<Integer>();

	public static int obtainAnId() {
		int i = 1;
		while (mIds.contains(i)) {
			i++;
		}
		mIds.add(i);
		return i;
	}

	public static void removeId(int id) {
		mIds.remove(id);
	}
}
