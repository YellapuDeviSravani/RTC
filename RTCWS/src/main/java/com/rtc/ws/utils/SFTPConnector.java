package com.rtc.ws.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jcraft.jsch.*;
import com.rtc.ws.bo.ConfigurationBO;
import com.rtc.ws.manager.Manager;
import com.rtc.ws.service.MainService;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

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

    public final static java.util.logging.Logger LOG = Logger.getLogger(MainService.class.getName());

    public static String getDiagnostics(){
        {

            LOG.info("running the diagnostics...");

            String values = null;

            // running the diagnostics..
		/*
		 - is FFMPEG reachable
		 - is SFTP server reachable
		 - is SFTP server running
		 - is SFTP root  writable
		 */

            String isFFMPEG_Reachable_Err = null;
            String isSFTPSrvrRunning_Err = null;

            boolean isFFMPEG_Reachable = false;
            boolean isSFTPSrvrReachable = false;
            boolean isSFTPSrvrDestFolderReachable = false;
            boolean isSFTPSrvrDestFolderWritable = false;
            boolean isSFTPSrvrRunning = false;
            boolean isWriteableWorking = false;
            boolean isRenameWorking = false;
            boolean isDeleteWorking = false;

            // 1. check if the ffmpeg is reachable
            ConfigurationBO config = ConfigurationHelper.getConfiguration();
            JsonObject ele = new JsonObject();
            if(null == config) {
                ele.addProperty("status", -1);
                ele.addProperty("error", "No configuration parameters set");
            }else {

                try {
                    ele.addProperty("status", 0);

                    String[] cmd = {config.getPath_to_ffmpeg_bin()+"ffprobe","–version"};

                    try {
                        Runtime.getRuntime().exec(cmd);
                        isFFMPEG_Reachable = true;
                    } catch (IOException e) {
                        isFFMPEG_Reachable_Err = e.getMessage();
                        LOG.severe("Exception while checking if the ffmpeg is reachable: " + e.getMessage());
                    }

                    // 2. check if the sftp server location is reachable

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

					/*
					File sftpServer = new File(config.getP_dest_Path_to_Root());
					isSFTPSrvrReachable = sftpServer.exists();


					// 3. check if the sftp destination folder is reachable

					if(isSFTPSrvrReachable) {
						File destinationFolder = new File(config.getP_dest_Path_to_Root().concat(config.getP_dest_Folder()));
						isSFTPSrvrDestFolderReachable = destinationFolder.exists();
						// 4. is sftp server root writable
						isSFTPSrvrDestFolderWritable = Files.isWritable(Paths.get(destinationFolder.getAbsolutePath()));
					}

					 */
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

                        if(isSFTPSrvrRunning && isSFTPSrvrReachable & isSFTPSrvrDestFolderReachable && isSFTPSrvrDestFolderWritable) {
                            File destinationFolder = new File(config.getP_dest_Path_to_Root().concat(config.getP_dest_Folder()));
                            // 6. if copy is working
                            File f = File.createTempFile("random"+ LocalDateTime.now().toString().replaceAll(":", "_"), ".tmp");

                            Manager mgr = new Manager(null,null,f.getName());
                            mgr.copy("randomorg", "randomacc", "randomproj","", new FileInputStream(f));
                            mgr = new Manager(null,null,config.getP_dest_Folder().concat("/randomorg/randomacc/randomproj/"+f.getName()));
                            String status = mgr.getStatus();
                            if(status.contains("\".copied\"")) {
                                isWriteableWorking = true;

                                mgr = new Manager(null,"/randomorg/randomacc/randomproj/",f.getName()+".copied");
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
                                        isDeleteWorking = true;
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
                            if(null != stream)
                                stream.close();
                        } catch (IOException e) {
                            LOG.severe("Error ocured while closing the uploadedInputStream");
                        }
                        jsch = null;
                    }
                    ele.addProperty("isFFMPEG_Reachable", isFFMPEG_Reachable);
                    ele.addProperty("isFFMPEG_Reachable_Err", isFFMPEG_Reachable_Err);
                    ele.addProperty("isSFTPSrvrReachable", isSFTPSrvrReachable);
                    ele.addProperty("isSFTPSrvrDestFolderReachable",isSFTPSrvrDestFolderReachable);
                    ele.addProperty("isSFTPSrvrDestFolderWritable",isSFTPSrvrDestFolderWritable);
                    ele.addProperty("isSFTPSrvrRunning",isSFTPSrvrRunning);
                    ele.addProperty("isSFTPSrvrRunning_Err",isSFTPSrvrRunning_Err);

                    ele.addProperty("isWriteableWorking",isWriteableWorking);
                    ele.addProperty("isRenameWorking",isRenameWorking);
                    ele.addProperty("isDeleteWorking",isDeleteWorking);
                }catch(Exception e) {
                    ele.addProperty("status", -1);
                    ele.addProperty("error", e.getMessage());
                    LOG.severe("Exception while running the diagnostics:" + e.getMessage());
                }

            }
            Gson gson = new Gson();

            values = gson.toJson(ele);

            LOG.info("Returning diagnostics...: " + values);

            return values;

        }
    }
}
