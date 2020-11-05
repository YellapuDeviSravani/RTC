package com.rtc.ws.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rtc.ws.manager.Manager;
import com.rtcapp.ws.AudioProcessorFactory;
import com.rtcapp.ws.impl.MP3Processor;
import com.rtcapp.ws.impl.WAVProcessor;
import com.rtcapp.ws.impl.WMAProcessor;

public class ExtractZip {

	static {
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
	}

	ZipFile zipFile = null;

	public final static Logger LOG = Logger.getLogger(ExtractZip.class.getName());

	public long getDurationOfZip(String path) throws Exception{

		LOG.info("**************" + "Looking for files @ " + path);
		FileSystemManager fsManager = VFS.getManager();
		LOG.info("**************" + "opened file system manager");
		Manager manager;
		// Open the zip file

		AudioProcessorFactory processor = null;
		long totalDuration = 0;
		List<String> durationList = new ArrayList<String>();
		path = Constants.p_dest_Path_to_Root.concat(path);
		zipFile = new ZipFile(path);
		LOG.info("**************" + "read zip file " + zipFile.getName() + " from " + path);
		Enumeration<?> enu = zipFile.entries();
		LOG.info("**************" + "read entries from zip file " + zipFile.getName() + " from " + path);

		while (enu.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) enu.nextElement();

			String name = zipEntry.getName();

			LOG.info("**************" + "found " + name);
			long duration = 0;
			if(name.toLowerCase().endsWith(".aac")) {

				final String uri = "zip:file:" + path + "!/" + name;

				FileObject resolvedFile = fsManager.resolveFile(uri);
				manager = new Manager(null,null,resolvedFile.getURL().toString());
				String result = manager.getDuration();

				JsonParser parser = new JsonParser();
				JsonElement ele = parser.parse(result);
				JsonObject jsonOb = ele.getAsJsonObject();
				duration = jsonOb.get("fileLength").getAsLong();
				if(duration == -1) {
					throw new Exception(jsonOb.get("error").getAsString());
				}
			}
			else {

				if(name.toLowerCase().endsWith(".wma")) {
					processor = new WMAProcessor();
				}else if(name.toLowerCase().endsWith(".wav")) {
					processor = new WAVProcessor();
				}else if(name.toLowerCase().endsWith(".mp3")) {
					processor = new MP3Processor();
				}else {
					continue;
				}
				duration = processor.getAudioProcessor(zipFile.getInputStream(zipEntry),name);
			}
			durationList.add("Duration for " + name + " is " + duration);
			totalDuration += duration;
		}


		for(String l:durationList) {
			LOG.info("**************" + l);
		}
		return totalDuration;

	}

	public static void main(String[] a) {
		ExtractZip ez = new ExtractZip();
		try {
			long totalDuration = ez.getDurationOfZip("/org/acc/proj/all.zip.copied");
			//
			LOG.info("**************Total duration of zip is " + totalDuration);
		} catch (Exception e) {
			LOG.severe("**************Exception occured while calculating the audio length for the zip file: " + e.getMessage());
		}finally {
			if(null != ez.zipFile) {
				try {
					ez.zipFile.close();
				} catch (IOException e) {
					LOG.severe("**************Exception occured while closing zip file: " + e.getMessage());
				}
			}
		}
	}


}
