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

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.log4j.Logger;

/**
 * 
 * A Hashing tool to be able to hash mp3 files without metadata.
 * e.g. ONLY the audio frames, without id3 tags, info frames etcpp
 *
 * @author Mathias Menzel-Nielsen <Mathias.Menzel-Nielsen@arvato-mobile.de>
 */
public class Mp3Hasher {

    private static Logger logger = Logger.getLogger(Mp3Hasher.class);

    /**
     * <h1>hash mp3 files, frame by frame, without metadata</h1>
     * Usage is really simple:
     * hand over a mp3 stream, and a hashing algorithm and get the hashcode as bytearray
     * @param mp3 the mp3 stream
     * @param algorithm  a algorithm description (e.g: "SHA-256" or "MD5" etcpp)
     * @return the hash as byte array
     * @throws IOException on io stuff while reading mp3 frames
     * @throws NoSuchAlgorithmException  if the java platform does not support the algorithm
     */
    public static byte[] hashAudioFrames(Mp3Stream mp3, String algorithm) throws IOException, NoSuchAlgorithmException {
        long i = 0;
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        Frame frame;
        
        //if Stream starts with ID3V2, skip it
        if(mp3.isAtID3V2())
            mp3.skipID3V2();
        
        while ((frame = mp3.getNextFrame()) != null) {
            if (!frame.isValid()) {
                logger.warn("Invalid Frame (ignoring!) :" + frame);
                continue;
            }
            if (!frame.isInfoTag()) {
                digest.update(frame.getData());
                i++;
            } else {
            }
        }
        logger.debug("Hashed " + i + " Frames");
        return digest.digest();
    }

    /**
     * <h1>hash mp3 files, frame by frame, without metadata</h1>
     * convienience method to get directly the hexstring
     * Usage is really simple:
     * hand over a mp3 stream, and a hashing algorithm and get the hashcode as hexString
     * @param mp3 the mp3 stream
     * @param algorithm  a algorithm description (e.g: "SHA-256" or "MD5" etcpp)
     * @return the hash as hex String (radix 1, base 16)
     * @throws IOException on io stuff while reading mp3 frames
     * @throws NoSuchAlgorithmException  if the java platform does not support the algorithm
     */
    public static String hashAudioFramesHex(Mp3Stream mp3, String algorithm) throws IOException, NoSuchAlgorithmException {
        byte[] bytes = hashAudioFrames(mp3, algorithm);
        BigInteger bi = new BigInteger(1, bytes);
        String hex =  bi.toString(16);
        //pad
        while(hex.length()<64)
            hex = "0"+hex;
        return hex;
    }
}
