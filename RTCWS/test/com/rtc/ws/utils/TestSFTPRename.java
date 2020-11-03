package com.rtc.ws.utils;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class TestSFTPRename {

	public static void main(String[] a) {
		
		TestSFTPRename ob = new TestSFTPRename();
		ob.renameFile("2.PNG.copying", "2.PNG.copied");
		
	}
	
	public boolean renameFile(String fromFilePath, String toFilePath) {
        ChannelSftp sftp = null;
        try {
            // Get a reusable channel if the session is not auto closed.
            sftp = openConnectedChannel();
            sftp.cd("org");
            sftp.cd("acc");
            sftp.cd("proj");
            sftp.rename(fromFilePath, toFilePath);
            return true;
        } catch (Exception e) {
        	e.printStackTrace();
            return false;
        } finally {
        	try {
				sftp.getSession().disconnect();
			} catch (JSchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if(null != sftp)
        	sftp.disconnect();
        }
    } 
	
	protected Session openSession() throws IOException {
       return statelessConnect();
    }
	
	protected ChannelSftp openConnectedChannel() throws JSchException, IOException {
        Session session = openSession();
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        return channel;
    }
	
	 protected Session statelessConnect() throws IOException {
	        JSch jsch=new JSch();
	        Session session = null;
	        try {
	          
	            session = jsch.getSession(Constants.p_dest_username, Constants.p_dest_hostname, Constants.p_dest_port);
	            if (StringUtils.isNotEmpty(Constants.p_dest_password)) {
	                session.setPassword(Constants.p_dest_password.getBytes());
	            }
	            java.util.Properties config = new java.util.Properties();
	            config.put("StrictHostKeyChecking", "no");
	            config.put("PreferredAuthentications", "password");
	            session.setConfig(config);
	            session.connect(5000);
	            return session;
	        } catch (JSchException e) {
	            throw new IOException(e);
			}   
	    }
}
