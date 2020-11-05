package com.rtcapp.ws.impl;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;

import com.rtc.ws.service.Service.STATE;
import com.rtcapp.ws.AudioProcessorFactory;
import com.sun.media.sound.WaveFileReader;




public class WAVProcessor extends AudioProcessorFactory{

	public final static Logger LOG = Logger.getLogger(WAVProcessor.class.getName());
	@Override
	public long getAudioProcessor(FileObject remoteFile) throws Exception{

		AudioFile f;
		try {
			LOG.log(Level.INFO, "***********Casting the SFTP File: " + remoteFile.getURL().toString() + " to normal File");

			String path = AudioProcessorFactory.getPath(remoteFile);
			
			File file = new File(path);
			
			LOG.log(Level.INFO, "***********Normal file is successfully casted from the SFTP file.");

			String ext = FilenameUtils.getExtension(remoteFile.getName().getPath().replace(STATE.COMPLETED.toString(),""));

			LOG.log(Level.INFO, "***********Converting normal file to audio file with extension " + ext);

			f = AudioFileIO.read(file);

			LOG.log(Level.INFO, "***********Fetching the header from the audio file");

			long duration = f.getAudioHeader().getTrackLength() * 1000;
			
			LOG.log(Level.INFO, "***********Calculated Duration of WAV file: " +  remoteFile.getURL().toString() + " is " + duration + " milliSeconds");
			
			return duration;
		} catch (Exception e) {
			LOG.severe("***********Exception while calculatin the audio length of " +  remoteFile.getURL().toString());
			throw e;
		} 

	}

	public long getAudioProcessor(InputStream stream,String fileName) throws Exception {
		WaveFileReader reader = new WaveFileReader();
		
		LOG.severe("***********Fetching the audio file inputstream for the inputstream for " + fileName);
		
		AudioInputStream ais = reader.getAudioInputStream(stream);
		
		LOG.severe("***********Fetching the audio file format for the audio file inputstream for " + fileName);
		
		AudioFormat format = ais.getFormat();

		LOG.severe("***********Fetching the duration from the audio file format fetched for " + fileName);;
		
		long frames = ais.getFrameLength();
		double durationInSeconds = (frames+0.0) / format.getFrameRate(); 
		long duration =  (long) durationInSeconds * 1000;
		
		LOG.log(Level.INFO, "***********Calculated Duration of WAV file: " + fileName+ " is " + duration + " milliSeconds");
		
		return duration;
	}
}
