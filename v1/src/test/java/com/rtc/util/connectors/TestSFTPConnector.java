package com.rtc.util.connectors;

import com.rtc.ws.entity.Rtcownsftpsrvr;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;

public class TestSFTPConnector {
    public static void main(String[] a) {
        String SFTP_ROOT = "/Users/rajarajeswaridevi";
        String RTC_FOLDER = "/Users/rajarajeswaridevi/Downloads/RTCTest";
        Rtcownsftpsrvr config = new Rtcownsftpsrvr();
        config.setHostname("localhost");
        config.setPort(22);
        config.setUsername("rajarajeswaridevi");
        config.setPassword("KrishnaMalla#6");
        config.setFolder(RTC_FOLDER);
        config.setRtcwssftppath(SFTP_ROOT);
        config.setRtcwsffmpefpath("/usr/local/bin/");
        try {
            SFTPConnector connector = new SFTPConnector();
            connector.connect(config);
            connector.moveToFolder(RTC_FOLDER);
            connector.createFolder("randomorg");
            connector.moveToFolder(RTC_FOLDER + "/" + "randomorg");
            connector.createFolder("randomacc");
            connector.moveToFolder(RTC_FOLDER + "/" + "randomorg" + "/" + "randomacc");
            connector.createFolder("randomproj");
            connector.moveToFolder(RTC_FOLDER + "/" + "randomorg" + "/" + "randomacc" + "/" + "randomproj");
            connector.checkIfFolderWritable(RTC_FOLDER + "/" + "randomorg" + "/" + "randomacc" + "/" + "randomproj");
            File f = File.createTempFile("random" + LocalDateTime.now().toString().replaceAll(":", "_"), ".tmp");
            connector.copyFile(f.getName(), new FileInputStream(f));
            connector.renameFile(RTC_FOLDER + "/" + "randomorg" + "/" + "randomacc" + "/" + "randomproj" + "/" + f.getName(), "tmp.copied");
            connector.deleteFile("tmp.copied");
            connector.deleteFolder(RTC_FOLDER + "/" + "randomorg" + "/" + "randomacc" + "/" + "randomproj");
            connector.deleteFolder(RTC_FOLDER + "/" + "randomorg" + "/" + "randomacc");
            connector.deleteFolder(RTC_FOLDER + "/" + "randomorg");
            connector.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}