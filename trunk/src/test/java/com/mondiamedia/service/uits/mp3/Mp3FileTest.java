/*
 * Copyright 2011 Mondia Media GmbH. All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Mondia Media GmbH ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement
 * you entered into with Mondia Media GmbH.

 */
package com.mondiamedia.service.uits.mp3;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 *
 * @author Mathias Menzel-Nielsen <Mathias.Menzel-Nielsen@arvato-mobile.de>
 */
public class Mp3FileTest extends TestCase {

    Logger logger = Logger.getLogger(this.getClass());

    /**
     * 
     */
    public Mp3FileTest() {
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testGetNextFrame() throws Exception {
        File file = new File("src/test/sample/test.mp3");

        assertTrue(file.exists());
        assertTrue(file.canRead());

        InputStream in = new FileInputStream(file);

        Mp3Stream mp3 = new Mp3Stream(in);



        byte[] framedata = mp3.findNextFrame(0);
        logger.debug(framedata.length);

        Frame frame = new Frame(framedata); // parse

        assertNotNull(frame);


        logger.debug(frame);

        assertTrue(frame.isValid());

        logger.debug("Framelength: " + frame.calculateFrameLength());

        int length = frame.calculateFrameLength();


        assertEquals(1, frame.getVersion());
        assertEquals(3, frame.getLayer());
        assertEquals(192, frame.getBitrate());

        assertEquals(626, length);
        assertTrue(frame.isInfoTag());



        //or use the new convinience method
        while ((frame = mp3.getNextFrame()) != null) {
            logger.debug(frame);
            assertFalse(frame.isInfoTag());
        }


    }

    /**
     * 
     * @throws Exception
     */
    public void testVbrParsing() throws Exception {
        File file = new File("src/test/sample/vbr.mp3");

        assertTrue(file.exists());
        assertTrue(file.canRead());

        InputStream in = new FileInputStream(file);

        Mp3Stream mp3 = new Mp3Stream(in);

        Frame frame = mp3.getNextFrame();
        logger.debug("Frame: " + frame);

        assertEquals(417, frame.calculateFrameLength());
        assertEquals(frame.calculateFrameLength(), frame.getData().length);
        assertEquals(44100, frame.getSamplingrate());
        assertEquals(128, frame.getBitrate());
        assertTrue(frame.isInfoTag());

        while ((frame = mp3.getNextFrame()) != null) {
            logger.debug(frame);
            assertFalse(frame.isInfoTag());
        }
    }
}
