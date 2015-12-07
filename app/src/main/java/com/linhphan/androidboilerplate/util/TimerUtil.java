package com.linhphan.androidboilerplate.util;

/**
 * Created by linhphan on 11/17/15.
 */
public class TimerUtil {
    /**
     * converters miliseconds to string format
     * @param ms
     * @return a string
     */
    public static String convertTime2String(long ms){
        int hour = (int) (ms / (1000*60*60));
        int minute = (int) ((ms % (1000*60*60)) / (1000*60));
        int second = (int) ((ms % (1000*60*60) % (1000*60)) / 1000);
        String result, h="", m="", s="";


        if (minute<10)
            m = "0"+minute;
        else m = minute+"";


        if (second<10)
            s = "0"+second;
        else s = second +"";


        if(hour <= 0) {
            result = m +":"+ s;
        }else if(hour < 10) {
            h = "0" + hour;
            result = h +":"+ m +":"+ s;
        }else{
            result = h +":"+ m +":"+ s;
        }


        return result;
    }
}
