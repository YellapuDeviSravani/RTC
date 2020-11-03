package com.rtc.ws.utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rtcapp.ws.impl.MP3Processor;

public class TestMP4ToMP3 {
	// MP3Converter.java

	private static final Logger LOG = Logger.getLogger(TestMP4ToMP3.class.getName());

	public static void main(String[] args) {

		try {
			String line;
			//String mp4File = "D:\\sftproot\\org\\acc\\proj\\3mg.mp4.copied";
			String mp4File = "C:\\Users\\Public\\Videos\\Sample Videos\\Wildlife.wmv";
			String mp3File = "C:\\Users\\Public\\Videos\\Sample Videos\\3mg-123.mp3";
			File file = new File(mp3File);
			if(file.exists())
				file.delete();

			// ffmpeg -i input.mp4 output.avi as it's on www.ffmpeg.org
			// -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 
			String[] cmd = {Constants.path_to_ffmpeg_bin+"ffprobe","-v","error","-show_entries","format=duration","-of","default=noprint_wrappers=1:nokey=1",mp4File};
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			double duration = 0;
			 
			while ((line = in.readLine()) != null) {
					duration = Double.valueOf(line) * 1000;
					LOG.info((long)duration +"");
			}
			p.waitFor();
			System.out.println("Video converted successfully!");
			in.close();

			MP3Processor processor = new MP3Processor();
			
			processor.getAudioProcessor(new FileInputStream(new File(mp3File)), mp3File);
			
		} catch (IOException | InterruptedException e) {
			LOG.log(Level.SEVERE, null, e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
