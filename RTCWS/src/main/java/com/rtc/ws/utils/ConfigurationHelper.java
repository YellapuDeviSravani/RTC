package com.rtc.ws.utils;

import java.util.logging.Logger;

import com.rtc.ws.bo.ConfigurationBO;

public class ConfigurationHelper {

	public final static Logger LOG = Logger.getLogger(ConfigurationHelper.class.getName());

	public static ConfigurationBO  getConfiguration() {

		ConfigurationBO config = null;
		for (Thread t : Thread.getAllStackTraces().keySet()) {
			if( t instanceof ImmortalThread) {
				LOG.info("Found thread: " + t.getName());
				ImmortalThread mt = (ImmortalThread) t;
				config = mt.getConfig();
			}
		}
		return config;
	}
}
