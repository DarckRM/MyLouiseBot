package com.darcklh.louise.Utils;

import org.springframework.util.DigestUtils;


import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptUtils {

    public static String MD5(String text) {
        return DigestUtils.md5DigestAsHex(text.getBytes());
    }

    public static String checkSumMD5(String path) throws IOException, NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(path);
        byte[] dataBytes = new byte[1024];

        int nread = 0;

        while ((nread = fis.read(dataBytes)) != -1)
            md.update(dataBytes, 0, nread);

        byte[] mdbytes = md.digest();

        //convert the byte to hex format
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < mdbytes.length; i++)
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));

        return sb.toString();
    }

    //二行制转字符串
    public static String byte2hex(byte[] b)
    {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b!=null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toUpperCase();
    }

}
