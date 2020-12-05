package com.rtc.ws.utils;

import com.rtcapp.ws.impl.MP4Processor1;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

public class M4ADuration {
    private static final Logger LOG = Logger.getLogger(M4ADuration.class.getName());
    public static void main(String[] args){
        try{
            String str = "D:\\Travelling.m4a";
            MP4Processor1 p = new MP4Processor1();
            p.getAudioProcessor(new FileInputStream(new File(str)),str);
        }catch(Exception e){
            System.out.println("cannot open input file");
            e.printStackTrace();
            return;
        }
    }
}
