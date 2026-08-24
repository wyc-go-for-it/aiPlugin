package com.wyc.hzaiplugin.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Administrator on 2018-03-06.
 */

public final class Utils {
    public static String getDeviceId(Context context) {
        String deviceId = getLocalMac(context).replace(":", "") + getAndroidId(context);
         if ("".equals(deviceId)) {
            UUID uuid = UUID.randomUUID();
            deviceId = uuid.toString().replace("-", "");
        }
        deviceId = getMD5(deviceId.getBytes());
        return deviceId.substring(0,16);
    }

    public  static  String getIMIE_string(Context context){
        String deviceId = getMD5(getIMIEStatus(context).getBytes());
        return deviceId.substring(0,16);
    }

    public static String getFirstSpell(String chinese) {
        StringBuilder pybf = new StringBuilder();
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char curchar : arr) {
            if (curchar > 128) {
                try {
                    if (curchar == 38271){
                        pybf.append('c');
                    }else {
                        String[] temp = PinyinHelper.toHanyuPinyinStringArray(curchar, defaultFormat);
                        if (temp != null) {
                            pybf.append(temp[0].charAt(0));
                        }
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pybf.append(curchar);
            }
        }
        return pybf.toString().replaceAll("\\W", "").trim().toUpperCase();
    }

    @NonNull
    public static String getMD5(byte[] data) {
        MessageDigest mdTemp = null;
        try {
            mdTemp = MessageDigest.getInstance("MD5" );
            mdTemp.update(data);
            byte [] md = mdTemp.digest();
            return byteToHex(md);
        } catch (NoSuchAlgorithmException | NullPointerException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String byteToHex(byte[] data){
        StringBuilder hexstr = new StringBuilder();
        char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9','A', 'B', 'C', 'D', 'E', 'F' };
        for (byte datum : data) {
            hexstr.append(hexDigits[datum >>> 4 & 0x0f]);
            hexstr.append(hexDigits[datum & 0x0f]);
        }
        return hexstr.toString();
    }

    // IMEI码
    private static String getIMIEStatus(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm)
            return tm.getDeviceId();
        else
            return "";
    }

    // Mac地址
    private static String getLocalMac(Context context) {
        final WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null)
            return wifi.getConnectionInfo().getMacAddress();
        else
            return "";
    }

    // Android Id
    private static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static boolean JsonIsNotEmpty(final JSONObject json){
        return json != null && !json.isEmpty();
    }
    public static boolean JsonIsNotEmpty(final JSONArray jsons){
        return jsons != null && !jsons.isEmpty();
    }
    public static JSONObject JsondeepCopy(@Nullable final JSONObject jsonObject){
        if (jsonObject == null)return new JSONObject();
        return JSON.parseObject(jsonObject.toJSONString());
    }
    public static JSONArray JsondeepCopy(@Nullable final JSONArray jsons){
        if (jsons == null)return new JSONArray();
        final JSONArray array = JSON.parseArray(jsons.toJSONString());
        return array == null ? new JSONArray() : array;
    }

    public static String getNullOrEmptyStringAsDefault(@Nullable final JSONObject object,final String key, final String default_v){
        if (object != null){
            final String value = object.getString(key);
            if (isNotEmpty(value)){
                return value;
            }
        }
        return default_v;
    }
    public static double getNotKeyAsNumberDefault(@Nullable final JSONObject object,final String key, final double default_v){
        if (object != null){
            final Double obj = object.getDouble(key);
            if (null != obj)return obj;
        }
        return default_v;
    }
    public static int getNotKeyAsNumberDefault(@Nullable final JSONObject object,final String key, final int default_v){
        if (object != null){
            final Integer obj = object.getInteger(key);
            if (null != obj)return obj;
        }
        return default_v;
    }

    public static long getNotKeyAsNumberDefault(@Nullable final JSONObject object, final String key, final long default_v){
        if (object != null){
            final Long obj = object.getLong(key);
            if (null != obj)return obj;
        }
        return default_v;
    }

    public static String getNullStringAsEmpty(@Nullable final JSONObject object,final String key){
        if (null != object){
            final String value = object.getString(key);
            return value == null ? "" :value;
        }
        return "";
    }

    public static JSONObject getNullObjectAsEmptyJson(final JSONObject object,final String key){
        if (object != null){
            final Object obj = object.get(key);
            if (obj instanceof JSONObject){
                return (JSONObject) obj;
            }else if (obj instanceof String){
                final String sz = (String)obj;
                if (sz.startsWith("{") && sz.endsWith("}")){
                    return JSONObject.parseObject(sz);
                }
            }
        }
        return new JSONObject();
    }
    public static JSONArray getNullObjectAsEmptyJsonArray(final JSONObject object,final String key){
        if (object != null){
            final Object obj = object.get(key);
            if (obj instanceof JSONArray){
                return (JSONArray) obj;
            }else if (obj instanceof String){
                final String sz = (String)obj;
                if (sz.startsWith("[") && sz.endsWith("]")){
                    return JSONArray.parseArray(sz);
                }
            }
        }
        return new JSONArray();
    }

    public static int getViewTagValue(final View view,int default_V){
        Object tag;
        if (view != null && (tag = view.getTag()) != null){
            if (tag instanceof Integer){
                default_V = (int)tag;
            }else if(tag instanceof String){
                try {
                    default_V = Integer.parseInt((String) tag);
                }catch (NumberFormatException ignore){
                }
            }
        }
        return default_V;
    }
    public static String getViewTagValue(final View view,String default_V){
        Object tag;
        if (view != null && (tag = view.getTag()) != null){
            if (tag instanceof String){
                return  (String) tag;
            }
        }
        return default_V;
    }

    public static JSONObject getViewTagValue(final View view){
        Object tag;
        if (view != null && (tag = view.getTag()) != null){
            if (tag instanceof JSONObject){
                return (JSONObject) tag;
            }
        }
        return new JSONObject();
    }


    public static boolean equalDouble(double a,double b){
        return Math.abs(a - b) < 0.00001;
    }
    public static boolean equalDouble(float a,float b){
        return Math.abs(a - b) < 0.00001;
    }
    public static boolean notLessDouble(double a,double b){
        return equalDouble(a,b) || greaterDouble(a,b);
    }
    public static boolean notGreaterDouble(double a,double b){
        return equalDouble(a,b) || lessDouble(a,b);
    }
    public static boolean greaterDouble(double a,double b){
        return a - b > 0.00001;
    }
    public static boolean lessDouble(double a,double b){
        return a - b < -0.00001;
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dpToPx(final Context context, final float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static float dpToPxF(final Context context, final float dp) {
        return   (dp * context.getResources().getDisplayMetrics().density);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(final Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static int sp2px(final Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int px2sp(final Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static long factorial(int n){
        if (n < 0)return 0;
        if (n == 0) return 1;
        return n * factorial(n - 1);
    }

    public static boolean isNotEmpty(final String sz){
        return sz != null && !sz.isEmpty() && !"null".equals(sz.trim().toLowerCase(Locale.ROOT));
    }
}
