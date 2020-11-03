package com.rtc.ws.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import com.google.gson.JsonObject;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.rtc.ws.bo.ConfigurationBO;
import com.rtc.ws.service.Service.STATE;
import com.rtc.ws.utils.ConfigurationHelper;
import com.rtcapp.ws.AudioProcessorFactory;
import com.rtcapp.ws.impl.AACProcessor;
import com.rtcapp.ws.impl.DefaultProcessor;
import com.rtcapp.ws.impl.MP3Processor;
import com.rtcapp.ws.impl.MP4Processor;
import com.rtcapp.ws.impl.WAVProcessor;
import com.rtcapp.ws.impl.WMAProcessor;
import com.rtcapp.ws.impl.ZipProcessor;

public class Manager{

	final InputStream inputstream;
	String path;
	String fileName;
	public final static Logger LOG = Logger.getLogger(Manager.class.getName());


	public Manager(InputStream inputstream,String path,String fileName){
		this.inputstream = inputstream;
		this.path = path;
		this.fileName = fileName;
	}

	public void run() {
		LOG.info("Renaming the file to be copied");

		String remoteFilePath =  path +"/"+ this.fileName;
		String originalFilePath = remoteFilePath.concat(STATE.WIP.toString());
		String renamedFilePath = remoteFilePath.concat(STATE.COMPLETED.toString());
		ChannelSftp sftp = null;
		try {
			// Get a reusable channel if the session is not auto closed.
			LOG.info("Connecting to the RTC Own SFTP server...");
			sftp = openConnectedChannel();
			LOG.info("Renaming file from " + originalFilePath + " to " +  renamedFilePath);
			sftp.rename(originalFilePath, renamedFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				sftp.getSession().disconnect();
			} catch (JSchException e) {
				LOG.severe("Error in disconnecting session to RTC's own sftp server");
			}
			if(null != sftp)
				sftp.disconnect();
		}
	}

	public void copy(String p_dest_organization_name, String p_dest_account_name, String p_dest_project_name,String p_folder_name, InputStream uploadedInputStream) {
		String _error = null;

		JSch jsch = null;
		Session session = null;
		Channel channel = null;
		ChannelSftp sftpChannel = null;

		ConfigurationBO config = ConfigurationHelper.getConfiguration();

		String path = config.getP_dest_Folder();
		String newFileName = null;

		int _status = -1;

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

			sftpChannel.cd(config.getP_dest_Folder());
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
			newFileName = fileName.concat(STATE.WIP.toString());
			sftpChannel.put(uploadedInputStream, newFileName );

			LOG.info("Copied the file " + fileName + " successfully");

			_status = 1;
			this.path = path + p_folder_name;

			LOG.info("Starting the Manager to rename file");
			run();
		} catch (Exception e) {
			// handle error
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			_error = "error:" + sw.toString();
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
			jsch = null;
		}
	}

	public static FileSystemOptions createDefaultOptions()
			throws FileSystemException, org.apache.commons.vfs2.FileSystemException {
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

	public static String createConnectionString(String path) {

		ConfigurationBO config = ConfigurationHelper.getConfiguration();

		return "sftp://" + config.getP_dest_username() + ":" + config.getP_dest_password() + "@" + config.getP_dest_hostname() +":" + config.getP_dest_port() + "/"  + path;

	}

	public String getStatus(){
		StandardFileSystemManager manager = new StandardFileSystemManager();
		JsonObject ele = new JsonObject();
		FileObject originalFileWithTMP = null;
		try {
			manager.init();
			originalFileWithTMP = manager.resolveFile(
					createConnectionString(
							fileName + STATE.WIP), createDefaultOptions());
			if(originalFileWithTMP.exists()){
				ele.addProperty("state", STATE.WIP.toString());
			}else{
				originalFileWithTMP.close();
				originalFileWithTMP = manager.resolveFile(
						createConnectionString(
								fileName + STATE.COMPLETED), createDefaultOptions());
				if(originalFileWithTMP.exists()){
					ele.addProperty("state", STATE.COMPLETED.toString());
				}else{
					originalFileWithTMP.close();
					originalFileWithTMP = manager.resolveFile(
							createConnectionString(
									fileName + STATE.FAILED), createDefaultOptions());
					if(originalFileWithTMP.exists()){
						ele.addProperty("state", STATE.FAILED.toString());
					}else{
						ele.addProperty("state", STATE.UNKNOWN.toString());
					}
				}
			}

		} catch (FileSystemException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			ele.addProperty("error",  sw.toString());
			LOG.severe("Failed while checking status: " + fileName);
		}finally{
			if(null != manager){
				manager.close();
			}

			if(null != originalFileWithTMP){
				ele.addProperty("url",originalFileWithTMP.getPublicURIString()); 
				try {
					originalFileWithTMP.close();
				} catch (FileSystemException e) {
					LOG.severe("Failed while closing the file:" + originalFileWithTMP.getName() + e.getMessage());
				}
			}
		}
		String response = ele.toString();
		return response;
	}

	public String getDuration() {
		LOG.info("Entering into the getDuration() for " + this.fileName);
		String sftpURL = this.fileName;

		LOG.log(Level.INFO, "***********Input SFTP URL: " + sftpURL);

		AudioProcessorFactory audioProcessor = null;

		StandardFileSystemManager manager = new StandardFileSystemManager();

		String result = null;

		String _url = null;
		int _status = -1;
		long _size = -1;
		String _error = null;
		long duration = -1;
		String fileName = null;
		try{
			// initialise the manager object
			manager.init();

			LOG.log(Level.INFO, "***********Connecting to SFTP Server to read file: " + sftpURL);

			ConfigurationBO config = ConfigurationHelper.getConfiguration();

			sftpURL = sftpURL.replace("***", config.getP_dest_password());
			// Create remote file object
			FileObject remoteFile = manager.resolveFile(sftpURL, createDefaultOptions());

			LOG.log(Level.INFO, "***********SFTP File is located successfully @ " + sftpURL);

			fileName = remoteFile.getName().getBaseName().replace(STATE.COMPLETED.toString(), "");
			if(fileName.toLowerCase().endsWith(".zip")) {
				audioProcessor = new ZipProcessor();
			}else if(fileName.toLowerCase().endsWith(".mp3"))
				audioProcessor = new MP3Processor();
			else if(fileName.toLowerCase().endsWith(".wav"))
				audioProcessor = new WAVProcessor();
			else if(fileName.toLowerCase().endsWith(".wma"))
				audioProcessor = new WMAProcessor();
			else if(fileName.toLowerCase().endsWith(".aac"))
				audioProcessor = new AACProcessor();
			else if(fileName.toLowerCase().endsWith(".mp4"))
				audioProcessor = new MP4Processor();
			else if(fileName.toLowerCase().endsWith(".wmv"))
				audioProcessor = new MP4Processor();
			else
				audioProcessor = new DefaultProcessor();

			duration = audioProcessor.getAudioProcessor(remoteFile);

			LOG.log(Level.INFO, "***********Audio duration for " + fileName + " is " + duration + " milliseconds");

			_url = remoteFile.getPublicURIString().replace(STATE.COMPLETED.toString(), "");
			_status = 1;
			_size = remoteFile.getContent().getSize();
			remoteFile.close();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			_error = sw.toString();
			LOG.severe("Error in fetching the file duration for " + sftpURL);
			LOG.severe("Error: " + _error);
		} finally {
			// close the manager object
			manager.close();
			JsonObject ele = new JsonObject();

			ele.addProperty("url", _url);
			ele.addProperty("status", _status);
			ele.addProperty("size", _size);
			ele.addProperty("error", _error);
			ele.addProperty("name", fileName);
			ele.addProperty("ts", LocalDateTime.now().toString());
			ele.addProperty("fileLength", duration);
			result = ele.toString();
			LOG.info("Result " + result);
		}

		LOG.info("Exiting from the getDuration() for " + this.fileName);
		return result;
	}


	private Session openSession() throws IOException {
		return statelessConnect();
	}

	private ChannelSftp openConnectedChannel() throws JSchException, IOException {
		LOG.info("Opening the session");
		Session session = openSession();
		LOG.info("creating sftp channel from the session");
		ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
		LOG.info("connecting to the sftp channel");
		channel.connect();
		return channel;
	}

	private Session statelessConnect() throws IOException {
		JSch jsch=new JSch();
		Session session = null;
		try {

			ConfigurationBO config = ConfigurationHelper.getConfiguration();

			LOG.info("Creating session with username: {} hostname: {} and port: {}" + " " + config.getP_dest_username() + "," + config.getP_dest_hostname() + " " + config.getP_dest_port());
			session = jsch.getSession(config.getP_dest_username(), config.getP_dest_hostname(), config.getP_dest_port());
			if (StringUtils.isNotEmpty(config.getP_dest_password())) {
				session.setPassword(config.getP_dest_password().getBytes());
			}
			java.util.Properties pconfig = new java.util.Properties();
			pconfig.put("StrictHostKeyChecking", "no");
			//config.put("PreferredAuthentications", "password");
			session.setConfig(pconfig);
			session.connect(5000);
			return session;
		} catch (JSchException e) {
			LOG.severe("Error in creating session: " + e.getMessage());
			throw new IOException(e);
		}   
	}


	public FileObject getRemoteFileObject() throws FileSystemException {

		StandardFileSystemManager manager = new StandardFileSystemManager();

		// initialise the manager object
		manager.init();

		// Create remote file object
		FileObject remoteFile = manager.resolveFile(
				createConnectionString(
						this.fileName
						), createDefaultOptions());

		LOG.log(Level.INFO, "***********SFTP File is located successfully @ " + remoteFile.getPublicURIString());

		return remoteFile;
	}

	public void copyFolder(String destPath) {

		String _error = null;

		JSch jsch = null;
		Session session = null;
		Channel channel = null;
		ChannelSftp sftpChannel = null;

		ConfigurationBO config = ConfigurationHelper.getConfiguration();

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

			sftpChannel.cd(config.getP_dest_Folder());
			sftpChannel.cd(this.path);
			String[] folders = destPath.split("/");
			try {
				String folderName = folders[folders.length-1];
				sftpChannel.cd(destPath.replace(folderName, ""));
				sftpChannel.mkdir(folderName);
			}catch(Exception e) {
				// check how to handle this
			}
			sftpChannel.cd(this.path);
			Vector<LsEntry> directoryEntries = sftpChannel.ls(this.path);
			for (LsEntry file : directoryEntries) {
				if(file.getFilename().startsWith("."))continue;
				sftpChannel.put(this.path + "/" + file.getFilename(),destPath);
			}
			LOG.info("Current am @ " + sftpChannel.pwd());

		} catch (Exception e) {
			// handle error
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			_error = "error:" + sw.toString();
			LOG.severe("Error occured: " + _error);
		} finally {

			if (null != sftpChannel)
				sftpChannel.exit();
			if (null != channel)
				channel.disconnect();
			if (null != session)
				session.disconnect();
			jsch = null;
		}

	}

	public String deleteFolder() {
		String _error = null;
		int _status;
		JSch jsch = null;
		Session session = null;
		Channel channel = null;
		ChannelSftp sftpChannel = null;

		ConfigurationBO config = ConfigurationHelper.getConfiguration();

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
			String fullPath = config.getP_dest_Folder().concat("/").concat(this.path);
			Vector<LsEntry> directoryEntries = sftpChannel.ls(fullPath);
			sftpChannel.cd(fullPath);
			for (LsEntry file : directoryEntries) {
				if(file.getFilename().startsWith("."))continue;
				LOG.info("File: " + file.getFilename() + " is sucessfully removed");
				sftpChannel.rm(file.getFilename());
			}
			sftpChannel.rmdir(fullPath);
			LOG.info("Directory: " + fullPath + " is sucessfully removed");
			_status = 1;

		} catch (Exception e) {
			// handle error
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			_error = "error:" + sw.toString();
			LOG.severe("Error occured: " + _error);
			_status = -1;
		} finally {

			if (null != sftpChannel)
				sftpChannel.exit();
			if (null != channel)
				channel.disconnect();
			if (null != session)
				session.disconnect();
			jsch = null;
		}

		JsonObject ele = new JsonObject();

		ele.addProperty("status", _status);
		ele.addProperty("error", _error);
		String result = ele.toString();
		LOG.info("Result " + result);
		return result;

	}

	public String delete() {
		String _error = null;

		JSch jsch = null;
		Session session = null;
		Channel channel = null;
		ChannelSftp sftpChannel = null;
		int _status;
		ConfigurationBO config = ConfigurationHelper.getConfiguration();

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
			sftpChannel.rm(config.getP_dest_Folder().concat("/").concat(this.path).concat("/").concat(this.fileName));
			_status = 1;

		} catch (Exception e) {
			// handle error
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			_error = "error:" + sw.toString();
			LOG.severe("Error occured: " + _error);
			_status = -1;
		} finally {

			if (null != sftpChannel)
				sftpChannel.exit();
			if (null != channel)
				channel.disconnect();
			if (null != session)
				session.disconnect();
			jsch = null;
		}

		JsonObject ele = new JsonObject();

		ele.addProperty("status", _status);
		ele.addProperty("error", _error);
		String result = ele.toString();
		LOG.info("Result " + result);
		return result;

	}

	public String renameFile(String destFile) {

		String response = null;

		LOG.info("Renaming the file to be copied");

		
		ChannelSftp sftp = null;
		String _error = null;

		JSch jsch = null;
		Session session = null;
		Channel channel = null;
		ChannelSftp sftpChannel = null;

		ConfigurationBO config = ConfigurationHelper.getConfiguration();

		String newFileName = null;

		int _status = -1;

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

			String remoteFilePath =  config.getP_dest_Folder() + path +"/"+ this.fileName;
			String originalFilePath = remoteFilePath;
			String renamedFilePath = config.getP_dest_Folder() +  path + "/" + destFile;
			
			LOG.info("Current Path: " + sftpChannel.pwd());
			LOG.info("Renaming file from " + originalFilePath + " to " +  renamedFilePath);
			LOG.info("Current Path: " + sftpChannel.pwd());
			sftpChannel.rename(originalFilePath, renamedFilePath);
			_status = 1;
		} catch (Exception e) {
			e.printStackTrace();
			_status = -1;
			_error = e.getMessage();
		} finally {
			try {
				sftpChannel.getSession().disconnect();
			} catch (JSchException e) {
				LOG.severe("Error in disconnecting session to RTC's own sftp server");
			}
			if(null != sftp)
				sftpChannel.disconnect();
			
			JsonObject ele = new JsonObject();

			ele.addProperty("status", _status);
			ele.addProperty("error", _error);
			response = ele.toString();
		}
		return response;
	}


}
