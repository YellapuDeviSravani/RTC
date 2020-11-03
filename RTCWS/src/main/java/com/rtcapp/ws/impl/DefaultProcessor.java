package com.rtcapp.ws.impl;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;

import com.rtcapp.ws.AudioProcessorFactory;

/**
 * This class is used to process the files that are not yet supported
 * This class will be finally removed when the support is added to the audio files
 * @author rjrjswrdvimalla
 *
 */
public class DefaultProcessor extends AudioProcessorFactory{

	@Override
	public long getAudioProcessor(FileObject remoteFile) throws Exception{
		return (long)(IOUtils.toByteArray(remoteFile.getContent().getInputStream()).length / 10000);
	}

	@Override
	public long getAudioProcessor(InputStream stream,String fileName) throws Exception {
		return (long)(IOUtils.toByteArray(stream).length / 10000);
	}

	 
}
