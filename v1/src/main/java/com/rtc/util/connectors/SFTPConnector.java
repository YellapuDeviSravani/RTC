package com.rtc.util.connectors;

import com.jcraft.jsch.*;
import com.rtc.ws.entity.Rtcownsftpsrvr;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import java.io.InputStream;
import java.util.Vector;
import java.util.logging.Logger;

public class SFTPConnector {
    public final static java.util.logging.Logger LOG = Logger.getLogger(SFTPConnector.class.getName());

    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    Session session = null;
    Channel channel = null;
    ChannelSftp sftpChannel = null;

    public void connect(Rtcownsftpsrvr config) throws Exception {
        JSch jsch = null;
        String path = config.getFolder();
        String newFileName = null;
        int _status = -1;
        InputStream stream = null;
        LOG.info("Connecting to the SFTP server...");
        // connect to the SFTP server
        jsch = new JSch();
        session = jsch.getSession(config.getUsername(), config.getHostname(), config.getPort());
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(config.getPassword());
        session.connect();
        LOG.info("Connected to the SFTP server");
    }

    public boolean checkIfFolderWritable(String path) throws SftpException {
        SftpATTRS attrs = sftpChannel.lstat(path);
        return attrs != null && ((attrs.getPermissions() & 00200) != 0);
    }

    public void moveToFolder(String path) throws JSchException, SftpException {
        channel = session.openChannel("sftp");
        channel.connect();
        sftpChannel = (ChannelSftp) channel;
        sftpChannel.cd(path);
    }

    private String createConnectionString(Rtcownsftpsrvr config, String path) {
        return "sftp://" + config.getUsername() + ":" + config.getPassword() + "@" + config.getHostname() + ":" + config.getPort() + "/" + path;
    }

    private FileSystemOptions createDefaultOptions() throws FileSystemException {
        // Create SFTP options
        FileSystemOptions opts = new FileSystemOptions();
        // SSH Key checking
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
                opts, "no");
        // Root directory set to user home
        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
        // Timeout is count by Milliseconds
        SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);
        return opts;
    }

    public void createFolder(String name) throws SftpException {
        sftpChannel.mkdir(name);
        LOG.info("Current path: " + sftpChannel.pwd());
    }

    public void deleteFile(String name) throws SftpException {
        sftpChannel.rm(name);
        LOG.info("File: " + name + " is successfully removed");
    }

    public void deleteFolder(String name) throws SftpException {
        sftpChannel.cd(name);
        Vector<ChannelSftp.LsEntry> directoryEntries = sftpChannel.ls(name);
        for (ChannelSftp.LsEntry file : directoryEntries) {
            if (file.getFilename().startsWith(".")) continue;
            deleteFile(file.getFilename());
        }
        sftpChannel.rmdir(name);
        LOG.info("Folder: " + name + " is successfully removed");
    }

    public void copyFile(String fileName, InputStream uploadedInputStream) throws SftpException {
        sftpChannel.put(uploadedInputStream, fileName);
    }

    public void renameFile(String oldFilename, String newFileName) throws SftpException {
        sftpChannel.rename(oldFilename, newFileName);
    }

    public void disconnect() {
        try {
            sftpChannel.getSession().disconnect();
        } catch (JSchException e) {
            LOG.severe("Error in disconnecting session to RTC's own sftp server");
        }
        sftpChannel.disconnect();
    }

    public enum STATE {
        WIP(".copying"), COMPLETED(".copied"), FAILED(".failed"), UNKNOWN("Unknown");
        private final String ext;

        STATE(String ext) {
            this.ext = ext;
        }

        @Override
        public String toString() {
            return ext;
        }

    }
}