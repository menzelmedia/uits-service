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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import org.apache.log4j.Logger;

/**
 * 
 * <h1>A Mp3 Stream. </h1>
 * Actually a managed PushbackInputStream which is able to find mp3 frames and seek them and seperate them
 *
 * @author Mathias Menzel-Nielsen <Mathias.Menzel-Nielsen@arvato-mobile.de>
 */
public class Mp3Stream {

    private Logger logger = Logger.getLogger(this.getClass());
    private PushbackInputStream in;
    private long pos = 0;
    private long mark = 0;

    /**
     * Wraps an InputStream of mp3 data, so that MP3 Frames can be found and parsed
     * @param in a InputStream of raw mp3 data (e.g. full mp3 file)
     */
    public Mp3Stream(final InputStream in) {
        this.in = new PushbackInputStream(in, 1024);
        pos = 0;
    }

    
    
   
    
    /**
     * seeks to beginning of next mp3 frame (identified by <tt>11-bit syncword (<b>11111111111</b>)</tt> )
     * -- reads it in fully, and returns a header parsed frame .
     * 
     * <p>streampointer will be positioned right at next frame afterwards.</p>
     * <p>Bulk data is contained in Frame object</p>
     * @return The next Frame in this MP3 Stream. If the next Frame is not Valid (no valid header) null is returned (should never happen). If there is no Frame left in the stream (e.g. end of stream) null is returned,
     * @throws IOException on io stuff
     */
    public Frame getNextFrame() throws IOException {
        try {
            while(!seekToNextPossibleFrame());
            //logger.debug("Pos: "+pos);
            byte[] headerData = new byte[4]; // header
            in.read(headerData);
            //logger.debug("bytes: "+ headerData[0] + "," +  headerData[1] + "," + headerData[2] + "," + headerData[3]) ;
            Frame header = new Frame(headerData); //decode
            if (header.isValid()) {
                int length = header.calculateFrameLength(); //FIXME: sanitycheck?
                in.unread(headerData);
                byte[] frameData = new byte[length];
                in.read(frameData);
                return new Frame(frameData); // full frame &z
            }
            return null; // no valid header
        } catch (EOFException ex) {
            return null;
        }
    }

    private int read() throws IOException {
        pos++;
        int i = in.read();
        if (i == -1) // intwise not bytewise (e.g. 0xffffffffffffffff)
        {
            throw new EOFException();
        }
        return i;
    }

    private long getCurrentPos() {
        return pos;
    }

    private long getMarkedPos() {
        return mark;
    }

    private void mark() {
        mark = pos;
    }

    private void unread(int b) throws IOException {
        in.unread(b);
        pos--;
    }

    /**
     * <h2>sync to next 11-bit syncword, position stream just at first byte of syncword. </h2>
     * <p>
     * <b>Note that this must not necessary be the beginning of a frame, 11 bits can also occur in frame</b>
     * One will need a header check also to be sure.</p><p> On mp3 files this should be no problem, if read from beginning, 
     * but in streaming mp3 you may need a few attempts to get the first real frame.</p>
     * @return true - if stream is positioned on next syncword. false if you need more tries to sync
     * @throws IOException 
     */
    private boolean seekToNextPossibleFrame() throws IOException {
        while (((byte) read() != (byte) 0xff));  //seek   
        byte b = (byte) read();
        if ((b & Integer.parseInt("11100000", 2)) == Integer.parseInt("11100000", 2)) { // First eleven bits must be 1
            //found
            //back to start
            unread(b);
            unread(0xff);
            return true;
        }
        return false;
    }

    
    /**
     * @see http://www.id3.org/id3v2.4.0-structure
     * @return
     * @throws IOException 
     */
    public boolean isAtID3V2()throws IOException{
        byte[] header = new byte[10];
        in.read(header);
        in.unread(header);
        if( ! "ID3".equals(new String(header, 0, 3)))
            return false;
        if( header[3] == 0xff )
            return false;
        if( header[4] == 0xff )
            return false;
        if( header[6] >= 0x80 )
            return false;
        if( header[7] >= 0x80 )
            return false;
        if( header[8] >= 0x80 )
            return false;
        if( header[9] >= 0x80 )
            return false;
        
        return true;
    }
    
    public static long calculateID3V2Length(byte header[]){
        long size = header[9];
        size += header[8] << 7;
        size += header[7] << 14;
        size += header[6] << 21;
        return size;
    }
    
    public void skipID3V2()throws IOException, IllegalStateException{
        if(!isAtID3V2())
            throw new IllegalStateException("Stream is not positioned at the beginning a ID3V2 Tag Header");
        byte[] header = new byte[10];
        in.read(header);
        long size = calculateID3V2Length(header);
        logger.debug("Skipping "+size+" bytes of ID3V2 Header data...");
        in.skip(size);
    }
    
    
    /**
     * Old frame finder. kept in for documenting purposes. <b>Do not use!</b>
     * @param minLength
     * @return
     * @throws IOException
     * @deprecated
     */
    @Deprecated
    public byte[] findNextFrame(long minLength) throws IOException {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            long marked = 0;
            while (((byte) read() != (byte) 0xff));  //seek   
            byte b = (byte) read();
            if ((b & Integer.parseInt("11100000", 2)) == Integer.parseInt("11100000", 2)) { // First eleven bits must be 1
                marked = getCurrentPos() - 2;

                bout.write(0xff);
                bout.write(b);
                logger.debug("Found Frame! @" + marked);

                while (true) { //until exception on end of stream
                    while ((b = (byte) read()) != (byte) 0xff) {
                        bout.write(b); //find end
                    }
                    b = (byte) read();
                    if (((b & Integer.parseInt("11100000", 2)) == Integer.parseInt("11100000", 2)) && (getCurrentPos() - marked) >= minLength) {
                        //found end
                        unread(b);
                        unread(0xff);
                        logger.debug("Found End! @" + getCurrentPos());
                        long length = getCurrentPos() - marked;
                        logger.debug("length: " + length);
                        return bout.toByteArray();
                    } else {
                        unread(0xff); // unnessesary
                    }
                }

            }
            return null;
        } catch (EOFException ex) {
            return null;
        }
    }

}
