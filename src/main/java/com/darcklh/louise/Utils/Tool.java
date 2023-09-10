package com.darcklh.louise.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * UniqueCode生成器
 */
public class Tool {
    public static String[] chars = new String[]{"a", "b", "c", "d", "e", "f", "g", "h",
            "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
            "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D",
            "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
            "T", "U", "V", "W", "X", "Y", "Z"};

    /**
     * @param format 格式化 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String uniqueDateID(String format) {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    public static int[] range(int end) {
        return range(0, end);
    }

    public static int[] range(int start, int end) {
        if (start >= end) {
            return new int[1];
        }
        int index = 0;
        int[] rangeArray = new int[end - start];
        while (start < end) {
            rangeArray[index] = start;
            index++;
            start++;
        }
        return rangeArray;
    }

    public static int[] random(int end, int count) {
        return random(0, end, count);
    }

    public static int[] random(int start, int end, int count) {
        if (start >= end) {
            return new int[1];
        }
        Random random = new Random();
        int[] randomArray = new int[count];
        int index = 0;
        while (index != count) {
            int randomNumber = random.nextInt(end);
            if (randomNumber < start)
                continue;
            randomArray[index] = randomNumber;
            index++;
        }
        return randomArray;
    }

    /**
     * @param name 线程基本名
     * @return
     */
    public static String uniqueThreadName(String name, String detail) {
        String threadName = name + "-" + detail;
        return threadName;
    }

    /**
     * 短UUID生成器
     *
     * @return
     */
    public synchronized static String generateShortUuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 4; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();
    }

}
