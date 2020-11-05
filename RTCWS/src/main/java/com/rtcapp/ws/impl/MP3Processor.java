package com.rtcapp.ws.impl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.vfs2.FileObject;
import org.jaudiotagger.audio.asf.io.AsfExtHeaderReader;
import org.jaudiotagger.audio.asf.io.AsfHeaderReader;
import org.jaudiotagger.audio.asf.io.ChunkReader;
import org.jaudiotagger.audio.asf.io.ContentBrandingReader;
import org.jaudiotagger.audio.asf.io.ContentDescriptionReader;
import org.jaudiotagger.audio.asf.io.FileHeaderReader;
import org.jaudiotagger.audio.asf.io.LanguageListReader;
import org.jaudiotagger.audio.asf.io.MetadataReader;
import org.jaudiotagger.audio.asf.io.StreamChunkReader;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import com.rtc.ws.utils.MpegAudioFileReader;

//import org.tritonus.share.sampled.file.TAudioFileFormat;

import com.rtcapp.ws.AudioProcessorFactory;

public class MP3Processor extends AudioProcessorFactory{

	public final static Logger LOG = Logger.getLogger(MP3Processor.class.getName());
	private final static AsfHeaderReader TAG_READER;

	static {
		final List<Class<? extends ChunkReader>> readers = new ArrayList<Class<? extends ChunkReader>>();
		readers.add(FileHeaderReader.class);
		readers.add(StreamChunkReader.class);
		readers.clear();
		readers.add(ContentDescriptionReader.class);
		readers.add(ContentBrandingReader.class);
		readers.add(LanguageListReader.class);
		readers.add(MetadataReader.class);
		/*
		 * Create the header extension object readers with just content
		 * description reader, extended content description reader, language
		 * list reader and both metadata object readers.
		 */
		final AsfExtHeaderReader extReader = new AsfExtHeaderReader(readers,
				true);
		TAG_READER = new AsfHeaderReader(readers, true);
		TAG_READER.setExtendedHeaderReader(extReader);
	}

	@Override
	public long getAudioProcessor(FileObject remoteFile) throws Exception{

		LOG.log(Level.INFO, "***********Casting the SFTP File: " + remoteFile.getURL().toString() + " to normal File");

		String path = AudioProcessorFactory.getPath(remoteFile);
		
		File file = new File(path);

		LOG.log(Level.INFO, "***********Normal file is successfully casted from the SFTP file.");

		LOG.log(Level.INFO, "***********Fetching file format");

		AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
		if (fileFormat instanceof TAudioFileFormat) {
			LOG.log(Level.INFO, "***********Found valid audio file format(MP3)");

			Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
			String key = "duration";

			if(null == properties.get(key)){
				LOG.log(Level.SEVERE, "***********No duration property is found");
				throw new Exception("No duration property is found for " + file.getName());
			}

			Long microseconds = (Long) properties.get(key);
			long milli = (long)microseconds / 1000;

			LOG.log(Level.INFO, "***********Calculated Duration of MP3 file: " + file.getAbsolutePath() + " is " + milli + " milliSeconds");
			return milli;
		} else {
			LOG.log(Level.SEVERE, "***********Unsupported MP3 file: " + file.getAbsolutePath());
			throw new UnsupportedAudioFileException();
		}

	}

	public long getAudioProcessor(InputStream stream,String fileName) throws Exception {
		LOG.severe("***********Fetching the audio file format for the inputstream for " + fileName);
		
		AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(stream);
		
		LOG.severe("***********Fetching the duration from the audio file format fetched for " + fileName);;
		
		Long duration = (Long) baseFileFormat.properties().get("duration");
		
		LOG.log(Level.INFO, "***********Calculated Duration of MP3 file: " + fileName+ " is " + duration + " milliSeconds");
		return duration;

	}
}

