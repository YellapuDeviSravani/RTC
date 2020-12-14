package com.rtc.util.connectors;

import com.jcraft.jsch.*;
import com.rtc.ws.entity.Rtcownsftpsrvr;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import com.google.gson.JsonObject;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

public class SFTPConnector {
    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    public final static java.util.logging.Logger LOG = Logger.getLogger(SFTPConnector.class.getName());

    public static void connect(Rtcownsftpsrvr config) throws Exception {
        JSch jsch = null;
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpChannel = null;
        String path = config.getFolder();
        String newFileName = null;
        int _status = -1;
        InputStream stream = null;
        String isSFTPSrvrRunning_Err;
        try {
            LOG.info("Connecting to the SFTP server...");
            // connect to the SFTP server
            jsch = new JSch();
            session = jsch.getSession(config.getUsername(), config.getHostname(), config.getPort());
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(config.getPassword());
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            LOG.info("Connected to the SFTP server");
            sftpChannel = (ChannelSftp) channel;
            boolean isSFTPSrvrReachable = true;
            sftpChannel.cd(config.getFolder());
            // copy the file
            boolean isSFTPSrvrDestFolderReachable = true;
            LOG.info("Navigated to the path: " + sftpChannel.pwd());
            SftpATTRS attrs = sftpChannel.lstat(config.getFolder());
            boolean isSFTPSrvrDestFolderWritable = attrs != null && ((attrs.getPermissions() & 00200) != 0);

            LOG.info("Exiting upload service");
            // 5. check if sftp server is running
            StandardFileSystemManager manager = new StandardFileSystemManager();

//                manager.init();
//                manager.resolveFile(
//                        createConnectionString(config,""), createDefaultOptions());
//                boolean isSFTPSrvrRunning = true;

            if(isSFTPSrvrReachable & isSFTPSrvrDestFolderReachable && isSFTPSrvrDestFolderWritable) {
                File destinationFolder = new File(config.getRtcwssftppath().concat(config.getFolder()));
                // 6. if copy is working
                File f = File.createTempFile("random"+ LocalDateTime.now().toString().replaceAll(":", "_"), ".tmp");


                copy(session,config,f.getName(),"randomorg", "randomacc", "randomproj","", new FileInputStream(f));
                String newFile = config.getFolder().concat("/randomorg/randomacc/randomproj/"+f.getName();
                String status = getStatus();
                if(status.contains("\".copied\"")) {
                 boolean   isWriteableWorking = true;

                    String newFilename1 = f.getName()+".copied";
                    // 7. if rename is working
                    status = mgr.renameFile(f.getName());
                    if(status.contains(":1")) {
                        isRenameWorking = true;
                        // 8. if delete is working
                        mgr = new Manager(null,"/randomorg/randomacc/randomproj/",f.getName());
                        status = mgr.delete();
                        if(status.contains(":1")) {
                            isDeleteWorking = true;
                        }
                    }else {
                        // 8. if delete is working
                        mgr = new Manager(null,"/randomorg/randomacc/randomproj/",f.getName()+".copied");
                        status = mgr.delete();
                        if(status.contains(":1")) {
                            boolean isDeleteWorking = true;
                        }
                    }
                }
                List<String> list = new ArrayList<String>();

                Vector filelist = sftpChannel.ls(config.getP_dest_Folder());
                for(int i=0; i<filelist.size();i++){
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) filelist.get(i);
                    if(!entry.getFilename().startsWith("."))
                        list.add(entry.getFilename());
                }

                if(list.size() > 0) {
                    Gson gson = new Gson();
                    ele.addProperty("files", gson.toJson(list));
                }
            }
        } catch (Exception e) {
            isSFTPSrvrRunning_Err = e.getMessage();
            LOG.severe("Exception while checking if the sftp server is running: " + e.getMessage());
        }


    }
    private static String createConnectionString(Rtcownsftpsrvr config,String path) {
        return "sftp://" + config.getUsername() + ":" + config.getPassword() + "@" + config.getHostname() +":" + config.getPort() + "/"  + path;
    }

    private static FileSystemOptions createDefaultOptions()
            throws org.apache.commons.vfs2.FileSystemException, org.apache.commons.vfs2.FileSystemException {
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

    public static void copy(Session session, Rtcownsftpsrvr config, String fileName, String p_dest_organization_name, String p_dest_account_name, String p_dest_project_name, String p_folder_name, InputStream uploadedInputStream) {

        Channel channel = null;
        ChannelSftp sftpChannel = null;

        String path = config.getFolder();


        try {

            LOG.info("Connecting to the SFTP server...");
            // connect to the SFTP server

            channel = session.openChannel("sftp");
            channel.connect();
            LOG.info("Connected to the SFTP server");

            sftpChannel = (ChannelSftp) channel;

            sftpChannel.cd(config.getFolder());
            if(!p_dest_organization_name.isEmpty()){
                path +=  "/"  + p_dest_organization_name;
                try {
                    // create the destination folder path if not existing
                    sftpChannel.mkdir(p_dest_organization_name);
                } catch (Exception ee) {}

                // set the path to the destination folder
                sftpChannel.cd(p_dest_organization_name);
                String[] folders  = p_folder_name.split("/");
                if(!p_dest_account_name.isEmpty()){
                    path +=  "/" + p_dest_account_name;
                    try {
                        // create the destination folder path if not existing
                        sftpChannel.mkdir(p_dest_account_name);
                    } catch (Exception ee) {}
                    // set the path to the destination folder
                    sftpChannel.cd(p_dest_account_name);
                    if(!p_dest_project_name.isEmpty()){
                        path += "/" + p_dest_project_name;
                        try {
                            // create the destination folder path if not existing
                            sftpChannel.mkdir(p_dest_project_name);
                        } catch (Exception ee) {}
                        // set the path to the destination folder
                        sftpChannel.cd(p_dest_project_name);

                        for(String subfolder:folders) {
                            if(subfolder.isEmpty())continue;
                            try {
                                // create the destination folder path if not existing
                                sftpChannel.mkdir(StringEscapeUtils.escapeJava(subfolder));
                            } catch (Exception ee) {}
                            // set the path to the destination folder
                            sftpChannel.cd(subfolder);
                        }
                    }
                }
            }
            // copy the file

            LOG.info("Navigated to the path: " + sftpChannel.pwd());

            LOG.info("trying to copy the file " + fileName);
           String newFileName = fileName.concat(STATE.WIP.toString());
            sftpChannel.put(uploadedInputStream, newFileName );

            LOG.info("Copied the file " + fileName + " successfully");

            int _status = 1;



        } catch (Exception e) {
            // handle error
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
           String _error = "error:" + sw.toString();
            LOG.severe("Error occured: " + _error);
        } finally {

            if (null != sftpChannel)
                sftpChannel.exit();
            if (null != channel)
                channel.disconnect();
            if (null != session)
                session.disconnect();
            try {
                uploadedInputStream.close();
            } catch (IOException e) {
                LOG.severe("Error ocured while closing the uploadedInputStream");
            }

        }
    }

    public enum STATE{
        WIP(".copying"),COMPLETED(".copied"),FAILED(".failed"),UNKNOWN("Unknown");
        private final String ext;

        STATE(String ext){
            this.ext = ext;
        }

        @Override
        public String toString() {
            return ext;
        }

    }

    public static String getStatus(Rtcownsftpsrvr config,String fileName) {
        StandardFileSystemManager manager = new StandardFileSystemManager();
        JsonObject ele = new JsonObject();
        FileObject originalFileWithTMP = null;
        try {
            manager.init();
            originalFileWithTMP = manager.resolveFile(
                    createConnectionString(config,
                            fileName + STATE.WIP), createDefaultOptions());
            if(originalFileWithTMP.exists()){
                ele.addProperty("state", STATE.WIP.toString());
            }else{
                originalFileWithTMP.close();
                originalFileWithTMP = manager.resolveFile(
                        createConnectionString(config,
                                fileName + STATE.COMPLETED), createDefaultOptions());
                if(originalFileWithTMP.exists()){
                    ele.addProperty("state", STATE.COMPLETED.toString());
                }else{
                    originalFileWithTMP.close();
                    originalFileWithTMP = manager.resolveFile(
                            createConnectionString(config,
                                    fileName + STATE.FAILED), createDefaultOptions());
                    if(originalFileWithTMP.exists()){
                        ele.addProperty("state", STATE.FAILED.toString());
                    }else{
                        ele.addProperty("state", STATE.UNKNOWN.toString());
                    }
                }
            }

        } catch (org.apache.commons.vfs2.FileSystemException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            ele.addProperty("error",  sw.toString());
            LOG.severe("Failed while checking status: " + fileName);
        }finally{
            if(null != manager){
                manager.close();
            }

            if(null != originalFileWithTMP){
                try {
                    ele.addProperty("url",originalFileWithTMP.getURL().toString());
                    originalFileWithTMP.close();
                } catch (FileSystemException e) {
                    LOG.severe("Failed while closing the file:" + originalFileWithTMP.getName() + e.getMessage());
                }
            }
        }
        String response = ele.toString();
        return response;
    }
}
