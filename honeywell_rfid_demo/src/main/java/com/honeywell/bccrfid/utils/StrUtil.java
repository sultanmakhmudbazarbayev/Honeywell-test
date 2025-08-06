package com.honeywell.bccrfid.utils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class StrUtil {
    private final static String TAG = "StrUtil";

    public static String toHexString(byte[] bArray) {
        StringBuilder sb = new StringBuilder();
        int c;

        for (byte b : bArray) {
            c = b & 0xff;

            if (c < 0x10) {
                sb.append("0");
            }

            sb.append(Integer.toHexString(c));
        }

        return sb.toString();
    }

    public static String toHexString(byte[] bArray, int start, int len) {
        return toHexString(bArray, start, len, null);
    }

    public static String toHexString(byte[] bArray, int start, int len,
                                     String separator) {
        if (bArray.length < start + len) {
            android.util.Log.e(TAG, "toHexString() overflow,    bArray.length="
                    + bArray.length + ", start=" + start + ", len=" + len);
            return null;
        }

        StringBuffer sb = new StringBuffer();
        int c;

        for (int i = start; i < len; i++) {
            c = bArray[i] & 0xff;

            if (c < 0x10) {
                sb.append("0");
            }

            sb.append(Integer.toHexString(c));

            if (separator != null) {
                sb.append(separator);
            }
        }

        return sb.toString();
    }

    public static String urlDecode(String str) {
        return urlDecode(str, "UTF-8");
    }

    public static String urlDecode(String str, String encode) {
        String result = "";

        if (null == str) {
            return null;
        }

        try {
            result = java.net.URLDecoder.decode(str, encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String urlEncode(String str) {
        return urlEncode(str, "UTF-8");
    }

    public static String urlEncode(String str, String encode) {
        String result = "";

        if (null == str) {
            return null;
        }

        try {
            result = java.net.URLEncoder.encode(str, encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String getFileNameFromUrl(String urlStr) {
        if (urlStr == null) {
            return null;
        }

        String[] strs = urlStr.split("/");

        if (strs.length < 2) {
            return null;
        }

        String fileName = strs[strs.length - 1];
        return fileName;
    }

    public static String getTimeString(int type, Date date) {
        if (type == 0) {
            return getTimeString("HH:mm:ss.SSS", date);
        }

        return null;
    }

    public static String getTimeString(String formatStr, Date date) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        return format.format(calendar.getTime());
    }

    public static String getUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}

