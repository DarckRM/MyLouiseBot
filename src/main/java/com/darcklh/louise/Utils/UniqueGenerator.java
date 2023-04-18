package com.darcklh.louise.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * UniqueCode生成器
 */
public class UniqueGenerator {

    public static Integer randomInt() {
        return 0;
    }

    public static String[] chars = new String[] { "a", "b", "c", "d", "e", "f", "g", "h",
            "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
            "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D",
            "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
            "T", "U", "V", "W", "X", "Y", "Z" };

    /**
     *
     * @param format 格式化 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String uniqueDateID(String format) {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    /**
     *
     * @param name 线程基本名
     * @return
     */
    public static String uniqueThreadName(String name, String detail) {
        String threadName = name + "-" + detail;
        return threadName;
    }

    /**
     * 短UUID生成器
     * @return
     */
    public synchronized static String generateShortUuid(){
        StringBuffer shortBuffer=new StringBuffer();
        String uuid= UUID.randomUUID().toString().replace("-","");
        for(int i=0;i< 4;i++){
            String str=uuid.substring(i*4,i*4+4);
            int x=Integer.parseInt(str,16);
            shortBuffer.append(chars[x%0x3E]);
        }
        return shortBuffer.toString();
    }

}
