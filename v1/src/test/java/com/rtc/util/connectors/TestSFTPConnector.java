package com.rtc.util.connectors;

import com.rtc.ws.entity.Rtcownsftpsrvr;

public class TestSFTPConnector {
    public static void main(String[] a){
        Rtcownsftpsrvr config = new Rtcownsftpsrvr();
        config.setHostname("localhost");
        config.setPort(22);
        config.setUsername("rajarajeswaridevi");
        config.setPassword("KrishnaMalla#6");
        config.setFolder("/Users/rajarajeswaridevi/Downloads/RTCTest");
        config.setRtcwssftppath("/Users/rajarajeswaridevi");
        config.setRtcwsffmpefpath("/usr/local/bin/");
        try {
            SFTPConnector.connect(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
