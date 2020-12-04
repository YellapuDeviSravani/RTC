package com.rtc.ws.utils;

import com.rtcapp.ws.impl.WMVProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

public class WMVDuration {
    private static final Logger LOG = Logger.getLogger(WMVDuration.class.getName());
    public static void main(String[] args){
        try{
            String str = "D:\\sample.wmv";
            WMVProcessor wma = new WMVProcessor();
            wma.getAudioProcessor(new FileInputStream(new File(str)),str);
            //System.out.println("Calculated Duration of WMV file: is " + bytesPerFrame + " milliSeconds");
        }catch(Exception e){
            System.out.println("cannot open input file");
            e.printStackTrace();
            return;
        }
    }
}
