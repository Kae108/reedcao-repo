package util;

import java.io.FileInputStream;
import java.security.MessageDigest;

public class FileMD5Checksum {

    public static String calculateMD5(String filePath) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        FileInputStream fileInputStream = new FileInputStream(filePath);
        byte[] buffer = new byte[8192];
        int length;
        while ((length = fileInputStream.read(buffer)) != -1) {
            //往摘要添加数据
            md5.update(buffer, 0, length);
        }
        fileInputStream.close();

        //计算md5
        byte[] md5Bytes = md5.digest();
        StringBuilder md5StringBuilder = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            String hex = Integer.toHexString(md5Byte & 0xFF);
            if (hex.length() == 1) {
                md5StringBuilder.append('0');
            }
            md5StringBuilder.append(hex);
        }
        return md5StringBuilder.toString();
    }

    public static boolean checkMD5(String typicalMD5String, String md5String) {
//        System.out.println("typicalMD5String:"+typicalMD5String+"__________"+"md5String:"+md5String);
        boolean b = false;
        if (typicalMD5String.equals(md5String)) {
            b = true;
        }
        return b;
    }
}
