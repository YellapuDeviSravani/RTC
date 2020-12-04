package com.rtcapp.ws.impl;

import org.jaudiotagger.audio.asf.data.AsfHeader;
import org.jaudiotagger.audio.asf.data.FileHeader;
import org.jaudiotagger.audio.asf.io.*;
import org.jaudiotagger.audio.asf.util.Utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WMVProcessor {
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
    public long getAudioProcessor(InputStream stream, String fileName) throws Exception {

        LOG.log(Level.INFO, "***********Fetching the ASF header from the audio file " + fileName);
        final AsfHeader result = FULL_READER.read(Utils.readGUID(stream),
                stream, 0);
        stream.close();

        LOG.log(Level.INFO, "***********Fetching the file header from ASF header of the audio file" + fileName);
        FileHeader header = result.getFileHeader();
        if(null != header) {
            long duration = (long) header.getPreciseDuration() * 1000;
            LOG.log(Level.INFO, "***********Calculated Duration of WMV file: " + fileName+ " is " + duration + " milliSeconds");
            return duration;
        }
        else
        {
            LOG.severe("***********No file header is found. " + fileName + " is an invalid WMA file");
            throw new Exception("Invalid WMA file.");

        }

    }
}
