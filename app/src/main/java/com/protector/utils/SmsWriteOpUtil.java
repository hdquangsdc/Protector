package com.protector.utils;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@TargetApi(Build.VERSION_CODES.KITKAT)
public final class SmsWriteOpUtil {
    private static final int OP_WRITE_SMS = 15;

    public static boolean isWriteEnabled(Context context) {
        int uid = getUid(context);
        Object opRes = checkOp(context, OP_WRITE_SMS, uid);
        return opRes instanceof Integer && (Integer) opRes == AppOpsManager.MODE_ALLOWED;
    }


    public static boolean setWriteEnabled(Context context, boolean enabled) {
        int uid = getUid(context);
        int mode = enabled ? AppOpsManager.MODE_ALLOWED
                : AppOpsManager.MODE_IGNORED;

        return setMode(context, OP_WRITE_SMS, uid, mode);
    }


    private static Object checkOp(Context context, int code, int uid) {
        try {
            AppOpsManager appOpsManager = (AppOpsManager) context
                    .getSystemService(Context.APP_OPS_SERVICE);
            Class appOpsManagerClass = appOpsManager.getClass();

            try {
                Class[] types = new Class[3];
                types[0] = Integer.TYPE;
                types[1] = Integer.TYPE;
                types[2] = String.class;
                Method checkOpMethod = appOpsManagerClass.getMethod("checkOp",
                        types);

                Object[] args = new Object[3];
                args[0] = code;
                args[1] = uid;
                args[2] = context.getPackageName();

                return checkOpMethod.invoke(appOpsManager, args);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoClassDefFoundError e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean setMode(Context context, int code, int uid, int mode) {
        try {
            AppOpsManager appOpsManager = (AppOpsManager) context
                    .getSystemService(Context.APP_OPS_SERVICE);
            Class appOpsManagerClass = appOpsManager.getClass();

            try {
                Class[] types = new Class[4];
                types[0] = Integer.TYPE;
                types[1] = Integer.TYPE;
                types[2] = String.class;
                types[3] = Integer.TYPE;
                Method setModeMethod = appOpsManagerClass.getMethod("setMode",
                        types);

                Object[] args = new Object[4];
                args[0] = code;
                args[1] = uid;
                args[2] = context.getPackageName();
                args[3] = mode;
                setModeMethod.invoke(appOpsManager, args);

                return true;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoClassDefFoundError e) {
                e.printStackTrace();
            }
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static int getUid(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_ACTIVITIES).uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
