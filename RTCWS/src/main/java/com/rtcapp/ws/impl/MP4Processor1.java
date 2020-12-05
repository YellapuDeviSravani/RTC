package com.rtcapp.ws.impl;

import com.rtc.ws.utils.MpegAudioFileReader;
import org.jaudiotagger.audio.asf.io.*;

import javax.sound.sampled.AudioFileFormat;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MP4Processor1 {
    public final static Logger LOG = Logger.getLogger(MP4Processor1.class.getName());
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

    public long getAudioProcessor(InputStream stream,String fileName) throws Exception {
        LOG.severe("***********Fetching the audio file format for the inputstream for " + fileName);

        AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(stream);

        LOG.severe("***********Fetching the duration from the audio file format fetched for " + fileName);;

        Long duration = (Long) baseFileFormat.properties().get("duration");
        if(fileName.contains("mp4")) {
            LOG.log(Level.INFO, "***********Calculated Duration of MP4 file: " + fileName + " is " + duration + " milliSeconds");

        }
        if(fileName.contains("m4a")){
            LOG.log(Level.INFO, "***********Calculated Duration of M4A file: " + fileName + " is " + duration + " milliSeconds");


        }
        return duration;
    }
}
