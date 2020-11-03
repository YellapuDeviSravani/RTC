package com.rtc.ws.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.rtc.ws.bo.ConfigurationBO;
import com.rtc.ws.manager.Manager;
import com.rtc.ws.utils.ConfigurationHelper;
import com.rtc.ws.utils.ImmortalThread;
@Path("/srv")
public class MainService {

	static {
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
	}

	public final static Logger LOG = Logger.getLogger(MainService.class.getName());

	@POST
	@Path("/setParameters")
	@Consumes(MediaType.APPLICATION_JSON)
	public void save(final String values) throws ServletException{

		LOG.info("Values received:" + values);

		LOG.info("Starting a thread to store values....");

		boolean found = false;

		Gson gson = new Gson();
		if(values == null || values.isEmpty()) {
			throw new ServletException("No input parameters are passed");
		}
		ConfigurationBO config = gson.fromJson(values, ConfigurationBO.class);


		for (Thread t : Thread.getAllStackTraces().keySet()) {
			if( t instanceof ImmortalThread) {
				found = true;
				LOG.info("Found an existing thread and updating the values");
				ImmortalThread mt = (ImmortalThread) t;
				mt.setConfig(config);
				// parse values string to json object


				LOG.info("Updated the values");
			}
		}

		if(!found) {
			ImmortalThread mt = new ImmortalThread();
			mt.setName("RTC_PARAMETER_VALUES");
			mt.setConfig(config);
			mt.start();

			LOG.info("Started the thread: " + mt.getName());
			try {
				mt.join();
			} catch (InterruptedException e) {
				LOG.severe("Unexpectedly closed the thread: " + mt.getName());
			}

			LOG.info("Closed the thread: " + mt.getName());
		}
	}


	@GET
	@Path("/getParameters")
	public String get(){

		LOG.info("Looking for the stored values....");

		String values = null;

		ConfigurationBO config = ConfigurationHelper.getConfiguration();
		JsonObject ele = new JsonObject();

		if(null == config) {
			ele.addProperty("status", -1);
			ele.addProperty("error", "No configuration parameters set");
		}else {
			ele.addProperty("status", 0);
			ele.addProperty("p_dest_hostname", config.getP_dest_hostname());
			ele.addProperty("p_dest_port", config.getP_dest_port());
			ele.addProperty("p_dest_username", config.getP_dest_username());
			ele.addProperty("p_dest_password", config.getP_dest_password());
			ele.addProperty("p_dest_Folder", config.getP_dest_Folder());
			ele.addProperty("p_dest_Path_to_Root", config.getP_dest_Path_to_Root());
			ele.addProperty("path_to_ffmpeg_bin", config.getPath_to_ffmpeg_bin());
		}
		Gson gson = new Gson();

		values = gson.toJson(ele);

		LOG.info("Returning the stored values: " + values);

		return values;

	}

	@GET
	@Path("/diagnostics")
	public String getDiagnostics(){

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
						File f = File.createTempFile("random"+LocalDateTime.now().toString().replaceAll(":", "_"), ".tmp");

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
							LsEntry entry = (LsEntry) filelist.get(i);
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