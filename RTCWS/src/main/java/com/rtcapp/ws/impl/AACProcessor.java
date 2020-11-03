package com.rtcapp.ws.impl;

import java.io.EOFException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.vfs2.FileObject;

import com.rtcapp.ws.AudioProcessorFactory;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;

/**
 * This class is used to process the files that are not yet supported
 * This class will be finally removed when the support is added to the audio files
 * @author rjrjswrdvimalla
 *
 */
public class AACProcessor<ADTSDemultiplexer> extends AudioProcessorFactory{

	public final static Logger LOG = Logger.getLogger(AACProcessor.class.getName());

	@SuppressWarnings("finally")
	public long getAudioProcessor(FileObject remoteFile) throws Exception{

		LOG.log(Level.INFO, "***********Fetching the input stream fromt the SFTP file: " + remoteFile.getPublicURIString() + " to normal File");

		InputStream stream = remoteFile.getContent().getInputStream();

		byte[]          b;              // array for the actual audio Data during the playback
		SourceDataLine  line;           // the line we'll use the get our audio to the speaker's
		Decoder         dec;            // decoder to get the audio bytes
		SampleBuffer    buf;            //

		LOG.log(Level.INFO, "***********Calculating the duration of the " + remoteFile.getName().getBaseName());

		ADTSDemultiplexer adts = new ADTSDemultiplexer(stream);

		LOG.log(Level.INFO, "***********Fetching the decoder information");

		dec = new Decoder(adts.getDecoderSpecificInfo());
		buf = new SampleBuffer();
		line = null;
		long frames = 0;
		AudioFormat aufmt = null;
		float sampleRate = 0;

		LOG.log(Level.INFO, "***********Decoding the audio frame by frame");

		try{
			while ((b = adts.readNextFrame()) != null ) {
				dec.decodeFrame(b, buf);
				if (line == null) {
					aufmt = new AudioFormat(buf.getSampleRate(),
							buf.getBitsPerSample(), buf.getChannels(), true,
							true);
					sampleRate = aufmt.getSampleRate();
				}
				b = buf.getData();
				frames++;

			}
			LOG.log(Level.INFO, "***********Frame count is " + frames + " and the sample rate is " + sampleRate);

		}catch(ArrayIndexOutOfBoundsException ex) {
			LOG.severe("Caught ArrayIndexOutOfBoundsException :" + ex.getMessage());
		}catch(EOFException e){
			LOG.severe("Caught EOFException :" + e.getMessage());
		}finally{
			final long duration = (long)( 1000 / sampleRate * 1024  * frames );

			LOG.log(Level.INFO, "***********Calculated Duration of AAC file: " + remoteFile.getPublicURIString() + " is " + duration + " milliSeconds");

			return duration;
		}

	}

	@Override
	public long getAudioProcessor(InputStream stream,String fileName) throws Exception {
		LOG.severe("Handling input stream for AAC files is not handled. Use the method getAudioProcessor(FileObject remoteFile) instead.");
		return 0;
		//throw new Exception("Not supported");
	}

}
