package iphonex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    public static String getValue(String tarStr, String startStr, String stopStr) {
        if (!tarStr.contains(startStr))
            return "";
        tarStr = tarStr.substring(tarStr.indexOf(startStr) + startStr.length());
        if (!tarStr.contains(stopStr))
            return "";
        return tarStr.substring(0, tarStr.indexOf(stopStr));
    }

    public static String getMd5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

    }
}
