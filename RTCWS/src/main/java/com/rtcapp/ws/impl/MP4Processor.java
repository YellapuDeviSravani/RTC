package com.rtcapp.ws.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;

import com.rtc.ws.bo.ConfigurationBO;
import com.rtc.ws.utils.ConfigurationHelper;
import com.rtcapp.ws.AudioProcessorFactory;

public class MP4Processor extends AudioProcessorFactory {

	public final static Logger LOG = Logger.getLogger(MP4Processor.class.getName());

	@Override
	public long getAudioProcessor(FileObject remoteFile) throws Exception {

		try {

			LOG.info("***********Fetching the actual path for " + remoteFile.getPublicURIString());

			String path = AudioProcessorFactory.getPath(remoteFile);

			ConfigurationBO config = ConfigurationHelper.getConfiguration();

			LOG.info("***********Calculating the duration for  " + path);

			LOG.log(Level.INFO, "***********FFMPEG library is configured at " + config.getPath_to_ffmpeg_bin());

			String[] cmd = {config.getPath_to_ffmpeg_bin()+"ffprobe","-v","error","-show_entries","format=duration","-of","default=noprint_wrappers=1:nokey=1",path};
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			long duration = 0;
			String line;
			while ((line = in.readLine()) != null) {
				duration = Math.round(Double.valueOf(line)) * 1000;
				LOG.info((long)duration +"");
			}

			LOG.log(Level.INFO, "***********Calculated Duration of MP4 file: " + path + " is " + duration + " milliSeconds");

			return duration;
		} catch (Exception e) {
			LOG.severe("***********Exception while calculatin the audio length of " +  remoteFile.getPublicURIString());
			throw e;
		} 

	}

	@Override
	public long getAudioProcessor(InputStream stream, String fileName) throws Exception {
		File file = new File(fileName);
		

		try {
			OutputStream outputStream = new FileOutputStream(file);
			IOUtils.copy(stream, outputStream);
			outputStream.close();

			LOG.info("***********Fetching the actual path for " + file.getAbsolutePath());

			String path = file.getAbsolutePath();

			ConfigurationBO config = ConfigurationHelper.getConfiguration();

			LOG.info("***********Calculating the duration for  " + path);

			LOG.log(Level.INFO, "***********FFMPEG library is configured at " + config.getPath_to_ffmpeg_bin());

			String[] cmd = {config.getPath_to_ffmpeg_bin()+"ffprobe","-v","error","-show_entries","format=duration","-of","default=noprint_wrappers=1:nokey=1",path};
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			long duration = 0;
			String line;
			while ((line = in.readLine()) != null) {
				duration = Math.round(Double.valueOf(line)) * 1000;
				LOG.info((long)duration +"");
			}

			LOG.log(Level.INFO, "***********Calculated Duration of MP4 file: " + path + " is " + duration + " milliSeconds");

			return duration;
		} catch (Exception e) {
			LOG.severe("***********Exception while calculatin the audio length of " +  file.getAbsolutePath());
			throw e;
		} finally {
			// delete file
			file.delete();
		}
	}

}
