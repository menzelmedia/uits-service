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
import java.math.BigInteger;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

/**
 *
 * @author Mathias Menzel-Nielsen <Mathias.Menzel-Nielsen@arvato-mobile.de>
 */
public class Mp3HasherTest extends TestCase {

    Logger logger = Logger.getLogger(this.getClass());

    /**
     * 
     * @throws Exception
     */
    public void testHashVBR() throws Exception {
        File file = new File("src/test/sample/vbr.mp3");

        assertTrue(file.exists());
        assertTrue(file.canRead());

        InputStream in = new FileInputStream(file);

        Mp3Stream mp3 = new Mp3Stream(in);

        byte hash[] = Mp3Hasher.hashAudioFrames(mp3, "SHA-256");
        String hex = (new BigInteger(1, hash)).toString(16);

        logger.debug(hex);

        assertEquals("ec5cbb2372503cd14f9b11c12ac19d2d7dc0a34e4ab3d7c4d246792dc899b174", hex); //FIXME: dont know if its correct -- needs validation from sony
        assertEquals(hex, Mp3Hasher.hashAudioFramesHex(new Mp3Stream(new FileInputStream(file)), "SHA-256"));
    }
    
    
    public void testID3Header() throws Exception{
        byte[] header = new byte[10];
        header[0] = 'I';
        header[1] = 'D';
        header[2] = '3';
        header[3] = 0x03;
        header[4] = 0x00;
        header[5] = 0x00;
        header[6] = 0x00;
        header[7] = 0x01;
        header[8] = 0x0d;
        header[9] = 0x55;
        
        
        long size = Mp3Stream.calculateID3V2Length(header);
        
        assertEquals(0x46d5, size);
    }
    
    
    /**
     * 
     * @throws Exception
     */
    
    
    
    public void testHashID3() throws Exception {
        //different file (more tags etcpp) -- first frame starts later (@0x800) -- must be SAME checksum as vbr.mp3
        File file = new File("src/test/sample/vbr-tagged.mp3");

        assertTrue(file.exists());
        assertTrue(file.canRead());

        InputStream in = new FileInputStream(file);

        Mp3Stream mp3 = new Mp3Stream(in);

        String hex = Mp3Hasher.hashAudioFramesHex(mp3, "SHA-256");
        
        logger.debug(hex);

        assertEquals("ec5cbb2372503cd14f9b11c12ac19d2d7dc0a34e4ab3d7c4d246792dc899b174", hex); //FIXME: dont know if its correct -- needs validation from sony
    }
    
    /**
     * 
     * @throws Exception
     */
    public void testProblemkind() throws Exception {
        //different file (padding) -- first frame starts much later 
        File file = new File("src/test/sample/problemkind.mp3");

        assertTrue(file.exists());
        assertTrue(file.canRead());

        InputStream in = new FileInputStream(file);

        Mp3Stream mp3 = new Mp3Stream(in);

        String hex = Mp3Hasher.hashAudioFramesHex(mp3, "SHA-256");
        
        logger.debug(hex);

        assertEquals("03fd3ed4452ab11cb766c14490edb84635336d0e7d42cd5af1e377319281fce3", hex);
        
        //0758428c8945d83ed07060fb2f4ce8e07f2473dd34cc0c5ba6332aa5204692e3
        assertEquals(hex, Mp3Hasher.hashAudioFramesHex(new Mp3Stream(new FileInputStream(file)), "SHA-256"));
    }
    
    public void testHeavyTagged() throws Exception {
        //different file (padding) -- first frame starts much later 
        File file = new File("src/test/sample/vbr-heavy.mp3");

        assertTrue(file.exists());
        assertTrue(file.canRead());

        InputStream in = new FileInputStream(file);

        Mp3Stream mp3 = new Mp3Stream(in);

        String hex = Mp3Hasher.hashAudioFramesHex(mp3, "SHA-256");
        
        logger.debug(hex);

        assertEquals("ec5cbb2372503cd14f9b11c12ac19d2d7dc0a34e4ab3d7c4d246792dc899b174", hex);
        
        assertEquals(hex, Mp3Hasher.hashAudioFramesHex(new Mp3Stream(new FileInputStream(file)), "SHA-256"));
    }
    
}
