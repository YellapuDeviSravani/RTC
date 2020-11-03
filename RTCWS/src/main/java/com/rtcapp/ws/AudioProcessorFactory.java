package com.rtcapp.ws;

import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;

import com.rtc.ws.bo.ConfigurationBO;
import com.rtc.ws.utils.ConfigurationHelper;

public abstract class AudioProcessorFactory {
	
	public abstract long getAudioProcessor(FileObject file) throws Exception;
	public abstract long getAudioProcessor(InputStream stream,String filename) throws Exception;

	static ConfigurationBO config = ConfigurationHelper.getConfiguration();
	public static String getPath(FileObject file) {
		
		return config.getP_dest_Path_to_Root().concat(file.getName().getPath());
	}
	
	public static String getPath(String path) {
		return config.getP_dest_Path_to_Root().concat(path);
	}
}


	
