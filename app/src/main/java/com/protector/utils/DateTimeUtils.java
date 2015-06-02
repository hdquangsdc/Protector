package com.protector.utils;

import android.content.Context;

import com.protector.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Ho Duy Quang
 */
public class DateTimeUtils {
    static private DecimalFormat timeFormater = new DecimalFormat("00");

    static public String getDateString(Context context, long milliseconds) {
        Date d = new Date(milliseconds);
        Calendar calNow = Calendar.getInstance();
        Calendar calYesterday = Calendar.getInstance();
        calYesterday.add(Calendar.DATE, -1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        String dateNow = dateFormat.format(calNow.getTime());
        String dateYesterday = dateFormat.format(calYesterday.getTime());
        String dateLog = dateFormat.format(d.getTime());

        if (dateLog.equals(dateNow))
            return context.getString(R.string.today);
        else if (dateLog.compareTo(dateYesterday) == 0)
            return context.getString(R.string.yesterday);
        Date date = new Date(milliseconds);
        SimpleDateFormat f = new SimpleDateFormat("MM-dd", Locale.getDefault());
        return f.format(date);
    }

    static public String getHoursString(long milliseconds) {
        Date date = new Date(milliseconds);
        SimpleDateFormat f = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return f.format(date);
    }

    public static String formatTime(long timeInMilisecond) {
        long hour = timeInMilisecond / (60 * 60 * 1000);
        long minute = (timeInMilisecond / (60 * 1000)) % 60;
        long second = (timeInMilisecond / 1000) % 60;
        return timeFormater.format(hour) + ":" + timeFormater.format(minute)
                + ":" + timeFormater.format(second);
    }
}
