package com.rtc.ws.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.rtc.ws.bo.ConfigurationBO;
import com.rtc.ws.manager.Manager;
import com.rtc.ws.utils.ConfigurationHelper;
import com.rtc.ws.utils.ZipUtils;
@Path("/srv")
public class Service {

	static {
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
	}

	public final static Logger LOG = Logger.getLogger(Service.class.getName());

	public static final String SFTP_PWD = "";

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

	@POST
	@Path("/upload")
	public void upload(
			@FormDataParam("file") final InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("organization") String p_dest_organization_name,
			@FormDataParam("account") String p_dest_account_name,
			@FormDataParam("project") String p_dest_project_name,
			@FormDataParam("folder")  @DefaultValue("")  String p_folder_name){

		LOG.info("Started to copy the file:" + fileDetail.getFileName());

		String fileName = fileDetail.getFileName();

		Manager manager = new Manager(null,null,fileName);
		manager.copy(p_dest_organization_name, p_dest_account_name, p_dest_project_name,p_folder_name, uploadedInputStream);

		LOG.info("Copied the file " + fileName + " successfully");

		LOG.info("Exiting upload service");

	}

	@POST
	@Path("/copy")
	public void copy(
			@FormDataParam("srcUsername") final String srcUserName,
			@FormDataParam("srcHostname") final String srcHostName,
			@FormDataParam("srcPort") final int srcPort,
			@FormDataParam("srcPassword") final String srcPassword,
			@FormDataParam("srcFolder") final String srcFolder,
			@FormDataParam("srcFile") final String srcFile,
			@FormDataParam("organization") final String p_dest_organization_name,
			@FormDataParam("account") final String p_dest_account_name,
			@FormDataParam("project") final String p_dest_project_name,
			@FormDataParam("folder") @DefaultValue("") final String p_folder_name){

		LOG.info("Started to copy the file:" + srcFile);

		String fileName = srcFile;

		String _error = null;

		JSch jsch = null;
		Session session = null;
		Channel channel = null;
		ChannelSftp sftpChannel = null;

		ConfigurationBO config = ConfigurationHelper.getConfiguration();

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

			sftpChannel.cd(config.getP_dest_Folder());
			// copy the file

			LOG.info("Navigated to the path: " + sftpChannel.pwd());

			LOG.info("trying to copy the file " + fileName);
			newFileName = fileName.concat(STATE.WIP.toString());
			stream = sftpChannel.get(srcFolder.concat("/").concat(fileName));
			final Manager manager  = new Manager(null,path, fileName);

			manager.copy(p_dest_organization_name, p_dest_account_name, p_dest_project_name,p_folder_name, stream);

			LOG.info("Copied the file " + fileName + " successfully");

			_status = 1;

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
				if(null != stream)
					stream.close();
			} catch (IOException e) {
				LOG.severe("Error ocured while closing the uploadedInputStream");
			}
			jsch = null;
		}
		LOG.info("Exiting upload service");

	}

	@GET
	@Path("/poll")
	public String poll(
			@QueryParam("fileName") final String fileName ){
		ConfigurationBO config = ConfigurationHelper.getConfiguration();
		Manager manager = new Manager(null,null,config.getP_dest_Folder().concat(fileName));
		return manager.getStatus();
	}

	@GET
	@Path("/getDuration")
	public String getDuration(@QueryParam("url") String sftpURL) {
		Manager manager = new Manager(null,null,sftpURL);
		return manager.getDuration();
	}

	@PUT
	@Path("/copyfolder")
	public void copyFolder(@FormDataParam("source") final String sourcePath,
			@FormDataParam("destination") final String destPath) {
		Manager manager = new Manager(null,sourcePath,null);
		manager.copyFolder(destPath);
	}

	@DELETE
	@Path("/deletefolder")
	public String deleteFolder(@FormDataParam("folder") final String sourcePath) {
		Manager manager = new Manager(null,sourcePath,null);
		return manager.deleteFolder();
	}

	@DELETE
	@Path("/delete")
	public String deleteFile(@FormDataParam("path") final String path,@FormDataParam("file") final String file) {
		Manager manager = new Manager(null,path,file);
		return manager.delete();
	}

	@GET
	@Path("/rename")
	public String renameFile(@QueryParam("path") final String path,
			@QueryParam("sourceFile") final String sourceFile,
			@QueryParam("destFile") final String destFile) {
		Manager manager = new Manager(null,path,sourceFile);
		return manager.renameFile(destFile);
	}

	@GET
	@Path("/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response download(@QueryParam("path") final String filePath,@DefaultValue("") @QueryParam("folder") final String folder) {
		ConfigurationBO config = ConfigurationHelper.getConfiguration();

		String path = config.getP_dest_Folder();
		File file = new File(config.getP_dest_Path_to_Root() + "/"+ path + "/" + filePath);

		LOG.info("Checking for folder: " + folder);
		
		if(!file.exists()) {
			return Response.noContent().build();
		}else if(file.isDirectory()){
			// zip all the entries of the directory and export
			UUID uuid = UUID.randomUUID();

			ZipUtils appZip = new ZipUtils();
			appZip.setSOURCE_FOLDER(file.getAbsolutePath());
			appZip.setOUTPUT_ZIP_FILE(file.getAbsolutePath()+"/" +uuid.toString().concat(".zip"));
			appZip.generateFileList(new File(appZip.getSOURCE_FOLDER()),folder);
			appZip.zipIt(appZip.getOUTPUT_ZIP_FILE());



			String[] folders = filePath.split("/");
			ResponseBuilder response = Response.ok().entity(new StreamingOutput() {
				@Override
				public void write(final OutputStream output) throws IOException, WebApplicationException {
					try {
						Files.copy(Paths.get(file.getAbsolutePath()+"/" +uuid.toString().concat(".zip")), output);
					} finally {
						Files.delete(Paths.get(file.getAbsolutePath()+"/" +uuid.toString().concat(".zip")));
					}
				}
			});
			response.header("Content-Disposition", "attachment;filename=" + folders[folders.length-1].concat(".zip"));
			return response.build();

		}else {
			ResponseBuilder response = Response.ok((Object) file);
			
			response.header("Content-Disposition", "attachment;filename=" + FilenameUtils.getBaseName(filePath) + '.' + FilenameUtils.getExtension(filePath));
			return response.build();
		}
	}

} 