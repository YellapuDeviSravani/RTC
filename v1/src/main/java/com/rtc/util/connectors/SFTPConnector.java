package com.rtc.util.connectors;

import com.jcraft.jsch.*;

import java.io.InputStream;
import java.util.logging.Logger;

public class SFTPConnector {
    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    public final static java.util.logging.Logger LOG = Logger.getLogger(SFTPConnector.class.getName());
/*
    public static void connect(Configuration config) throws Exception{

        JSch jsch = null;
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpChannel = null;

        String path = config.getP_dest_Folder();
        String newFileName = null;

        int _status = -1;

        InputStream stream = null;
        try {

            LOG.info("Connecting to the SFTP server...");
            // connect to the SFTP server
            jsch = new JSch();
            session = jsch.getSession(config.getP_dest_username(), config.getP_dest_hostname(), config.getP_dest_port());
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(config.getP_dest_password());
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            LOG.info("Connected to the SFTP server");

            sftpChannel = (ChannelSftp) channel;

            isSFTPSrvrReachable = true;

            sftpChannel.cd(config.getP_dest_Folder());
            // copy the file
            isSFTPSrvrDestFolderReachable = true;

            LOG.info("Navigated to the path: " + sftpChannel.pwd());

            SftpATTRS attrs = sftpChannel.lstat(config.getP_dest_Folder());
            isSFTPSrvrDestFolderWritable = attrs != null && ((attrs.getPermissions() & 00200) != 0);


            LOG.info("Exiting upload service");


            // 5. check if sftp server is running

            StandardFileSystemManager manager = new StandardFileSystemManager();
            try {
                manager.init();
                manager.resolveFile(
                        Manager.createConnectionString(""), Manager.createDefaultOptions());
                isSFTPSrvrRunning = true;
            } catch (FileSystemException e) {
                isSFTPSrvrRunning_Err = e.getMessage();
                LOG.severe("Exception while checking if the sftp server is running: " + e.getMessage());
            }
    }
    */
}
