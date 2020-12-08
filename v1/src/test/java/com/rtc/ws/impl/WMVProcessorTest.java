package com.rtc.ws.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.*;

class WMVProcessorTest {

    @Test
    @DisplayName("Compare WMV File Duration")
    void CompareWMVDuration() throws  Exception {
        String str = "src/test/resources/TravellingSoldier.wmv";
        WMVProcessor p = new WMVProcessor();
        Assertions.assertEquals(p.getAudioProcessor(new FileInputStream(new File(str)), str), 268000);
    }

    @Test
    @DisplayName("Compare WMA File Duration")
    void CompareWMADuration() throws  Exception {
        String str = "src/test/resources/TravellingSoldier.wma";
        WMVProcessor p = new WMVProcessor();
        Assertions.assertEquals(p.getAudioProcessor(new FileInputStream(new File(str)), str), 268000);
    }
}