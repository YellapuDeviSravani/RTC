package com.rtc.ws.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;

public class MP4ProcessorTest {

    @Test
    @DisplayName("Compare MP4 File Duration")
    void CompareMP4Duration() throws  Exception{
        String str = "src/test/resources/Soldier.mp4";
        MP4Processor p = new MP4Processor();
        Assertions.assertEquals(p.getAudioProcessor(new FileInputStream(new File(str)), str), 153184);

    }

    @Test
    @DisplayName("Compare M4A File Duration")
    void CompareM4ADuration() throws  Exception{
        String str = "src/test/resources/Travelling.m4a";
        MP4Processor p = new MP4Processor();
        Assertions.assertEquals(p.getAudioProcessor(new FileInputStream(new File(str)), str), 153232);
    }

    @Test
    @DisplayName("Compare MP3 File Duration")
    void CompareMP3Duration() throws  Exception{
        String str = "src/test/resources/TravellingSoldier.mp3";
        MP4Processor p = new MP4Processor();
        Assertions.assertEquals(p.getAudioProcessor(new FileInputStream(new File(str)), str), 269140);
    }
}