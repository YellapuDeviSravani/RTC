package com.rtcapp.ws.impl;

import java.io.InputStream;
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

public class ZipProcessor extends AudioProcessorFactory {

	public final static Logger LOG = Logger.getLogger(ZipProcessor.class.getName());



	@Override
	public long getAudioProcessor(FileObject file) throws Exception {


		ZipFile zipFile = null;

		String path = AudioProcessorFactory.getPath(file);

		LOG.info("**************" + "Looking for files @ " + path);
		FileSystemManager fsManager = VFS.getManager();
		LOG.info("**************" + "opened file system manager");
		Manager manager;
		// Open the zip file

		AudioProcessorFactory processor = null;
		long totalDuration = 0;
		List<String> durationList = new ArrayList<String>();

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
				manager = new Manager(null,null,resolvedFile.getPublicURIString());
				String result = manager.getDuration();

				JsonParser parser = new JsonParser();
				JsonElement ele = parser.parse(result);
				JsonObject jsonOb = ele.getAsJsonObject();
				duration = jsonOb.get("fileLength").getAsLong();
				if(duration == -1) {
					LOG.severe(jsonOb.get("error").getAsString());
					duration = 0;
				}
			}
			else {

				if(name.toLowerCase().endsWith(".wma")) {
					processor = new WMAProcessor();
				}else if(name.toLowerCase().endsWith(".wav")) {
					processor = new WAVProcessor();
				}else if(name.toLowerCase().endsWith(".mp3")) {
					processor = new MP3Processor();
				}else if(name.toLowerCase().endsWith(".mp4")) {
					processor = new MP4Processor();
					name = path.replace("/" + file.getName().getBaseName(), "/" + Thread.currentThread().getId()+ "_" + name + ".zipentry");
					}
				else if(name.toLowerCase().endsWith(".wmv")) {
					processor = new MP4Processor();
					name = path.replace("/" + file.getName().getBaseName(), "/" + Thread.currentThread().getId()+ "_" + name + ".zipentry");
				}
				else {
					continue;
				}
				try {
					duration = processor.getAudioProcessor(zipFile.getInputStream(zipEntry),name);
				}catch(Exception e) {
					duration = 0;
				}
			}
			durationList.add("Duration for " + name + " is " + duration);
			
			totalDuration += duration;
		}


		for(String l:durationList) {
			LOG.info("**************" + l);
		}

		zipFile.close();
		return totalDuration;
	}

	@Override
	public long getAudioProcessor(InputStream stream, String filename) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

}
