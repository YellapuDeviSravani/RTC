package com.rtc.ws.utils;


import com.rtcapp.ws.impl.MP4Processor1;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

public class mp4Duration {
    private static final Logger LOG = Logger.getLogger(mp4Duration.class.getName());
    public static void main(String[] args) {

            try {
                String str = "D:\\Soldier.mp4";
                MP4Processor1 mp4 = new MP4Processor1();
                mp4.getAudioProcessor(new FileInputStream(new File(str)),str);

            } catch (Exception e) {
                e.printStackTrace();
            }


    }
}