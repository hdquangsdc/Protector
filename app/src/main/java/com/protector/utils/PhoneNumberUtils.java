package com.protector.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.telephony.TelephonyManager;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.protector.ProtectorApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vgershman on 04.02.14.
 */
public class PhoneNumberUtils {

    public static String[] getPhoneNumber(Context ctx, String rawNumber) throws NumberParseException {
        String[] number = new String[2];
        if (rawNumber == null) {
            rawNumber = "123456";
        }
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        TelephonyManager telMgr = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
        String curLocale = telMgr.getNetworkCountryIso().toUpperCase();

        Phonenumber.PhoneNumber phoneNumberProto = phoneUtil.parse(rawNumber, curLocale);
        String fixedNumber = phoneUtil.format(phoneNumberProto,
                PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        String fixedNumbera = phoneUtil.format(phoneNumberProto,
                PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        number[0] = fixedNumber.replace(" ", "");
        number[1] = fixedNumbera.replace(" ", "");
        return number;

    }

    public static String getDCode(Context context) {
        TelephonyManager telMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String curLocale = telMgr.getNetworkCountryIso().toUpperCase();
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        return String
                .format("%d", phoneUtil.getCountryCodeForRegion(curLocale));
    }
}
