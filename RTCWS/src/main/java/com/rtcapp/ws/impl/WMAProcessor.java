package com.rtcapp.ws.impl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.asf.data.AsfHeader;
import org.jaudiotagger.audio.asf.data.FileHeader;
import org.jaudiotagger.audio.asf.io.AsfExtHeaderReader;
import org.jaudiotagger.audio.asf.io.AsfHeaderReader;
import org.jaudiotagger.audio.asf.io.ChunkReader;
import org.jaudiotagger.audio.asf.io.ContentBrandingReader;
import org.jaudiotagger.audio.asf.io.ContentDescriptionReader;
import org.jaudiotagger.audio.asf.io.FileHeaderReader;
import org.jaudiotagger.audio.asf.io.LanguageListReader;
import org.jaudiotagger.audio.asf.io.MetadataReader;
import org.jaudiotagger.audio.asf.io.StreamBitratePropertiesReader;
import org.jaudiotagger.audio.asf.io.StreamChunkReader;
import org.jaudiotagger.audio.asf.util.Utils;

import com.rtc.ws.service.Service.STATE;
import com.rtcapp.ws.AudioProcessorFactory;
/**
 * This class fetches the duration of the WMA audio file
 * 
 * Source of dependancy jar: http://www.jthink.net/jaudiotagger/index.jsp
 * 
 * @author rjrjswrdvimalla
 *
 */
public class WMAProcessor extends AudioProcessorFactory {

	public final static Logger LOG = Logger.getLogger(WMAProcessor.class.getName());
	private final static AsfHeaderReader FULL_READER;

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
		final AsfExtHeaderReader extReader2 = new AsfExtHeaderReader(readers,
				true);
		readers.add(FileHeaderReader.class);
		readers.add(StreamChunkReader.class);

		readers.add(StreamBitratePropertiesReader.class);
		FULL_READER = new AsfHeaderReader(readers, false);
		FULL_READER.setExtendedHeaderReader(extReader2);
	}
	@Override
	public long getAudioProcessor(FileObject remoteFile) throws Exception {
		AudioFile f;
		try {
			LOG.log(Level.INFO, "***********Casting the SFTP File: " + remoteFile.getPublicURIString() + " to normal File");

			String path = AudioProcessorFactory.getPath(remoteFile);
			
			File file = new File(path);
			
			LOG.log(Level.INFO, "***********Normal file is successfully casted from the SFTP file.");

			String ext = FilenameUtils.getExtension(remoteFile.getName().getPath().replace(STATE.COMPLETED.toString(),""));

			LOG.log(Level.INFO, "***********Converting normal file to audio file with extension " + ext);

			f = AudioFileIO.readAs(file, ext);

			LOG.log(Level.INFO, "***********Fetching the header from the audio file");
			long duration = (long) f.getAudioHeader().getPreciseTrackLength() * 1000;
			LOG.log(Level.INFO, "***********Calculated Duration of WMA file: " + remoteFile.getPublicURIString() + " is " + duration + " milliSeconds");
			return duration;
		} catch (Exception e) {
			LOG.severe("***********Exception while calculatin the audio length of " +  remoteFile.getPublicURIString());
			throw e;
		} 
	}

	public long getAudioProcessor(InputStream stream,String fileName) throws Exception {

		LOG.log(Level.INFO, "***********Fetching the ASF header from the audio file " + fileName);
		final AsfHeader result = FULL_READER.read(Utils.readGUID(stream),
				stream, 0);
		stream.close();

		LOG.log(Level.INFO, "***********Fetching the file header from ASF header of the audio file" + fileName);
		FileHeader header = result.getFileHeader();
		if(null != header) {
			long duration = (long) header.getPreciseDuration() * 1000;
			LOG.log(Level.INFO, "***********Calculated Duration of WMA file: " + fileName+ " is " + duration + " milliSeconds");
			return duration;
		}
		else
		{
			LOG.severe("***********No file header is found. " + fileName + " is an invalid WMA file");
			throw new Exception("Invalid WMA file.");

		}

	}


}
