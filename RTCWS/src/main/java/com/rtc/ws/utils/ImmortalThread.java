package com.rtc.ws.utils;

import java.util.logging.Logger;

import com.rtc.ws.bo.ConfigurationBO;

public  class ImmortalThread extends Thread
{

	public final static Logger LOG = Logger.getLogger(ImmortalThread.class.getName());

	ConfigurationBO config;
	public ConfigurationBO getConfig() {
		return config;
	}
	public void setConfig(ConfigurationBO config) {
		this.config = config;
	}
	public void run()
	{
		LOG.info("Holding the values: ");
		LOG.info("Hostname: " + config.getP_dest_hostname());
		LOG.info("Port: " + config.getP_dest_port());
		LOG.info("Username: " + config.getP_dest_username());
		LOG.info("Password: " + "*********");
		LOG.info("Destination Folder: " + config.getP_dest_Folder());
		LOG.info("Destination Path to root: " + config.getP_dest_Path_to_Root());
		LOG.info("Path to FFMPEG bin folder: " + config.getPath_to_ffmpeg_bin());
		try {
			LOG.info("Starting to wait");
			while(true) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			LOG.severe("Unexpectedly closed the thread: " + this.getName());
		}
	}
}